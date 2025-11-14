package gyeonggi.gyeonggifesta.board.service.post;

import gyeonggi.gyeonggifesta.board.dto.post.request.CreatePostReq;
import gyeonggi.gyeonggifesta.board.dto.post.request.UpdatePostReq;
import gyeonggi.gyeonggifesta.board.dto.post.response.EventOptionRes;
import gyeonggi.gyeonggifesta.board.dto.post.response.PostListRes;
import gyeonggi.gyeonggifesta.board.dto.post.response.PostRes;
import gyeonggi.gyeonggifesta.board.entity.Board;
import gyeonggi.gyeonggifesta.board.entity.Post;
import gyeonggi.gyeonggifesta.board.entity.PostAvailableDate;
import gyeonggi.gyeonggifesta.board.enums.PreferredGender;
import gyeonggi.gyeonggifesta.board.exception.BoardErrorCode;
import gyeonggi.gyeonggifesta.board.repository.BoardRepository;
import gyeonggi.gyeonggifesta.board.repository.PostRepository;
import gyeonggi.gyeonggifesta.board.service.media.PostMediaService;
import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.enums.Status;
import gyeonggi.gyeonggifesta.event.exception.EventErrorCode;
import gyeonggi.gyeonggifesta.event.repository.EventRepository;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

        private static final String COMPANION_BOARD_NAME = "동행찾기";

        private final SecurityUtil securityUtil;
        private final PostMediaService postMediaService;
        private final BoardRepository boardRepository;
        private final PostRepository postRepository;
        private final EventRepository eventRepository;

        @Override
        @Transactional
        public void createPost(CreatePostReq request) {

                Member currentMember = securityUtil.getCurrentMember();

                Board board = getOrCreateCompanionBoard();
                Event event = validateEventForPost(request.getEventId());

                PreferredGender preferredGender = parsePreferredGender(request.getPreferredGender());
                validateRecruitmentInfo(request.getRecruitmentTotal(), request.getRecruitmentPeriodDays());
                validateAgeRange(request.getPreferredMinAge(), request.getPreferredMaxAge());
                validateVisitDates(request.getVisitDates(), event);

                Post post = Post.builder()
                        .board(board)
                        .member(currentMember)
                        .event(event)
                        .title(request.getTitle())
                        .content(request.getContent())
                        .recruitmentTotal(request.getRecruitmentTotal())
                        .recruitmentPeriodDays(request.getRecruitmentPeriodDays())
                        .preferredGender(preferredGender)
                        .preferredMinAge(request.getPreferredMinAge())
                        .preferredMaxAge(request.getPreferredMaxAge())
                        .build();

                syncAvailableDates(post, request.getVisitDates());

                currentMember.addPost(post);
                board.addPost(post);

                postMediaService.createPostMedia(post, request.getKeyList());
                postRepository.save(post);

        }

        @Override
        @Transactional(readOnly = true)
        public Page<PostListRes> getPosts(Long eventId, Pageable pageable) {

                Optional<Board> boardOptional = boardRepository.findByName(COMPANION_BOARD_NAME);
                if (boardOptional.isEmpty()) {
                        return Page.empty(pageable);
                }

                Long boardId = boardOptional.get().getId();

                if (eventId != null) {
                        getEventOrThrow(eventId);
                }

                Page<Post> postsPage;
                if (pageable.getSort() != null && pageable.getSort().isSorted()) {
                        String property = pageable.getSort().iterator().next().getProperty();
                        Pageable basePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

                        if ("likesCount".equals(property)) {
                                postsPage = eventId == null
                                        ? postRepository.findByBoardIdOrderByLikesCountDesc(boardId, basePageable)
                                        : postRepository.findByBoardIdAndEventIdOrderByLikesCountDesc(boardId, eventId, basePageable);
                        } else if ("commentsCount".equals(property)) {
                                postsPage = eventId == null
                                        ? postRepository.findByBoardIdOrderByCommentsCountDesc(boardId, basePageable)
                                        : postRepository.findByBoardIdAndEventIdOrderByCommentsCountDesc(boardId, eventId, basePageable);
                        } else {
                                postsPage = eventId == null
                                        ? postRepository.findByBoardId(boardId, pageable)
                                        : postRepository.findByBoardIdAndEventId(boardId, eventId, pageable);
                        }
                } else {
                        postsPage = eventId == null
                                ? postRepository.findByBoardId(boardId, pageable)
                                : postRepository.findByBoardIdAndEventId(boardId, eventId, pageable);
                }

                return postsPage.map(this::toPostListRes);
        }

        @Override
        @Transactional
        public PostRes getPost(Long postId) {
                Post post = validatePost(postId);

                post.increaseViewCount();

                return toPostRes(post);
        }

        /**
         * 업데이트 시:
         * - 게시글의 제목/내용 업데이트 후,
         * - 전달받은 keyList와 기존 DB에 저장된 미디어의 S3Key를 비교하여
         * - DB에는 있었으나 새 keyList에 없는 미디어는 S3에서 삭제 후 DB에서도 제거
         * - 새롭게 추가된 key는 PostMedia 엔티티를 생성하여 추가
         */
        @Override
        @Transactional
        public void updatePost(Long postId, UpdatePostReq request) {
                Post post = validatePost(postId);
                validateMember(post);

                if (request.getTitle() != null) {
                        post.setTitle(request.getTitle());
                }
                if (request.getContent() != null) {
                        post.setContent(request.getContent());
                }

                if (request.getVisitDates() != null) {
                        validateVisitDates(request.getVisitDates(), post.getEvent());
                        syncAvailableDates(post, request.getVisitDates());
                }

                if (request.getRecruitmentTotal() != null || request.getRecruitmentPeriodDays() != null) {
                        Integer total = request.getRecruitmentTotal() != null ? request.getRecruitmentTotal() : post.getRecruitmentTotal();
                        Integer period = request.getRecruitmentPeriodDays() != null ? request.getRecruitmentPeriodDays() : post.getRecruitmentPeriodDays();
                        validateRecruitmentInfo(total, period);
                        post.setRecruitmentTotal(total);
                        post.setRecruitmentPeriodDays(period);
                }

                if (request.getPreferredGender() != null) {
                        post.setPreferredGender(parsePreferredGender(request.getPreferredGender()));
                }

                if (request.getPreferredMinAge() != null || request.getPreferredMaxAge() != null) {
                        Integer minAge = request.getPreferredMinAge() != null ? request.getPreferredMinAge() : post.getPreferredMinAge();
                        Integer maxAge = request.getPreferredMaxAge() != null ? request.getPreferredMaxAge() : post.getPreferredMaxAge();
                        validateAgeRange(minAge, maxAge);
                        post.setPreferredMinAge(minAge);
                        post.setPreferredMaxAge(maxAge);
                }

                postMediaService.updatePostMedia(post, request.getKeyList());

        }

        /**
         * 게시글 삭제 시:
         * - 게시글에 연결된 모든 미디어의 S3Key를 이용해 S3 파일을 삭제한 후,
         * - DB에서 게시글과 관련된 미디어도 삭제합니다.
         */
        @Override
        @Transactional
        public void deletePost(Long postId) {

                Post post = validatePost(postId);
                Member member = validateMember(post);
                Board board = post.getBoard();

                postMediaService.removePostMedia(post);

                member.removePost(post);
                board.removePost(post);

                postRepository.delete(post);
        }

        @Override
        @Transactional(readOnly = true)
        public List<EventOptionRes> getActiveEvents() {
                LocalDate today = LocalDate.now();
                List<Event> activeEvents = new ArrayList<>();
                activeEvents.addAll(eventRepository.findAllByStatus(Status.PROGRESS));
                activeEvents.addAll(eventRepository.findAllByStatus(Status.NOT_STARTED));

                Map<Long, Event> filtered = activeEvents.stream()
                        .filter(event -> event.getEndDate() == null || !event.getEndDate().isBefore(today))
                        .collect(Collectors.toMap(Event::getId, Function.identity(), (existing, duplicate) -> existing, LinkedHashMap::new));

                return filtered.values().stream()
                        .sorted(Comparator.comparing(Event::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(EventOptionRes::from)
                        .collect(Collectors.toList());
        }

        private Board getOrCreateCompanionBoard() {
                return boardRepository.findByName(COMPANION_BOARD_NAME)
                        .orElseGet(() -> boardRepository.save(Board.builder()
                                .name(COMPANION_BOARD_NAME)
                                .build()));
        }

        private Post validatePost(Long postId) {
                return postRepository.findById(postId)
                        .orElseThrow(() -> new BusinessException(BoardErrorCode.NOT_EXIST_POST));
        }

        private Member validateMember(Post post) {
                Member currentMember = securityUtil.getCurrentMember();

                if (!post.getMember().equals(currentMember)) {
                        throw new BusinessException(BoardErrorCode.NOT_WRITER);
                }

                return currentMember;
        }

        private Event validateEventForPost(Long eventId) {
                if (eventId == null) {
                        throw new BusinessException(BoardErrorCode.INVALID_EVENT_FOR_POST);
                }

                Event event = getEventOrThrow(eventId);

                LocalDate today = LocalDate.now();
                if (event.getStatus() == Status.END || (event.getEndDate() != null && event.getEndDate().isBefore(today))) {
                        throw new BusinessException(BoardErrorCode.INVALID_EVENT_FOR_POST);
                }
                return event;
        }

        private Event getEventOrThrow(Long eventId) {
                return eventRepository.findById(eventId)
                        .orElseThrow(() -> new BusinessException(EventErrorCode.NOT_EXIST_EVENT));
        }

        private void validateVisitDates(List<LocalDate> visitDates, Event event) {
                if (visitDates == null || visitDates.isEmpty()) {
                        throw new BusinessException(BoardErrorCode.INVALID_VISIT_DATE);
                }

                LocalDate today = LocalDate.now();
                for (LocalDate visitDate : visitDates) {
                        if (visitDate == null) {
                                throw new BusinessException(BoardErrorCode.INVALID_VISIT_DATE);
                        }
                        if (visitDate.isBefore(today)) {
                                throw new BusinessException(BoardErrorCode.INVALID_VISIT_DATE);
                        }
                        if (event.getStartDate() != null && visitDate.isBefore(event.getStartDate())) {
                                throw new BusinessException(BoardErrorCode.INVALID_VISIT_DATE);
                        }
                        if (event.getEndDate() != null && visitDate.isAfter(event.getEndDate())) {
                                throw new BusinessException(BoardErrorCode.INVALID_VISIT_DATE);
                        }
                }
        }

        private void validateRecruitmentInfo(Integer recruitmentTotal, Integer recruitmentPeriodDays) {
                if (recruitmentTotal == null || recruitmentPeriodDays == null) {
                        throw new BusinessException(BoardErrorCode.INVALID_RECRUITMENT_INFO);
                }

                if (recruitmentTotal <= 0 || recruitmentPeriodDays <= 0) {
                        throw new BusinessException(BoardErrorCode.INVALID_RECRUITMENT_INFO);
                }
        }

        private void validateAgeRange(Integer minAge, Integer maxAge) {
                if (minAge == null && maxAge == null) {
                        return;
                }

                if (minAge == null || maxAge == null) {
                        throw new BusinessException(BoardErrorCode.INVALID_AGE_RANGE);
                }

                if (minAge < 0 || maxAge < 0 || minAge > maxAge) {
                        throw new BusinessException(BoardErrorCode.INVALID_AGE_RANGE);
                }
        }

        private PreferredGender parsePreferredGender(String preferredGender) {
                if (preferredGender == null || preferredGender.isBlank()) {
                        return PreferredGender.ANY;
                }

                String normalized = preferredGender.trim().toUpperCase(Locale.ROOT);

                switch (normalized) {
                        case "MALE":
                        case "남":
                        case "남자":
                        case "남성":
                                return PreferredGender.MALE;
                        case "FEMALE":
                        case "여":
                        case "여자":
                        case "여성":
                                return PreferredGender.FEMALE;
                        case "ANY":
                        case "무관":
                        case "상관없음":
                        case "ANYONE":
                                return PreferredGender.ANY;
                        default:
                                throw new BusinessException(BoardErrorCode.INVALID_PREFERRED_GENDER);
                }
        }

        private void syncAvailableDates(Post post, List<LocalDate> visitDates) {
                if (visitDates == null) {
                        return;
                }

                Set<LocalDate> uniqueDates = visitDates.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(TreeSet::new));

                Map<LocalDate, PostAvailableDate> existing = post.getAvailableDates().stream()
                        .collect(Collectors.toMap(PostAvailableDate::getVisitDate, Function.identity(), (first, second) -> first));

                new ArrayList<>(post.getAvailableDates()).forEach(availableDate -> {
                        if (!uniqueDates.contains(availableDate.getVisitDate())) {
                                post.removeAvailableDate(availableDate);
                        }
                });

                uniqueDates.forEach(date -> {
                        if (!existing.containsKey(date)) {
                                PostAvailableDate availableDate = PostAvailableDate.builder()
                                        .post(post)
                                        .visitDate(date)
                                        .build();
                                post.addAvailableDate(availableDate);
                        }
                });
        }

        private PostListRes toPostListRes(Post post) {
                Event event = post.getEvent();
                return PostListRes.builder()
                        .postId(post.getId())
                        .title(post.getTitle())
                        .writer(post.getMember().getUsername())
                        .viewCount(post.getViewCount())
                        .likes(post.getPostLikes().size())
                        .comments(post.getPostComments().size())
                        .updatedAt(post.getUpdatedAt().toLocalDate())
                        .eventId(event.getId())
                        .eventTitle(event.getTitle())
                        .eventMainImage(event.getMainImg())
                        .eventStartDate(event.getStartDate())
                        .eventEndDate(event.getEndDate())
                        .visitDates(extractVisitDates(post))
                        .recruitmentTotal(post.getRecruitmentTotal())
                        .recruitmentPeriodDays(post.getRecruitmentPeriodDays())
                        .preferredGender(post.getPreferredGender() != null ? post.getPreferredGender().name() : null)
                        .preferredMinAge(post.getPreferredMinAge())
                        .preferredMaxAge(post.getPreferredMaxAge())
                        .build();
        }

        private PostRes toPostRes(Post post) {
                Event event = post.getEvent();
                return PostRes.builder()
                        .postId(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .writer(post.getMember().getUsername())
                        .viewCount(post.getViewCount())
                        .likes(post.getPostLikes().size())
                        .comments(post.getPostComments().size())
                        .updatedAt(post.getUpdatedAt())
                        .eventId(event.getId())
                        .eventTitle(event.getTitle())
                        .eventMainImage(event.getMainImg())
                        .eventStartDate(event.getStartDate())
                        .eventEndDate(event.getEndDate())
                        .visitDates(extractVisitDates(post))
                        .recruitmentTotal(post.getRecruitmentTotal())
                        .recruitmentPeriodDays(post.getRecruitmentPeriodDays())
                        .preferredGender(post.getPreferredGender() != null ? post.getPreferredGender().name() : null)
                        .preferredMinAge(post.getPreferredMinAge())
                        .preferredMaxAge(post.getPreferredMaxAge())
                        .build();
        }

        private List<LocalDate> extractVisitDates(Post post) {
                return post.getAvailableDates().stream()
                        .map(PostAvailableDate::getVisitDate)
                        .sorted()
                        .collect(Collectors.toList());
        }
}
