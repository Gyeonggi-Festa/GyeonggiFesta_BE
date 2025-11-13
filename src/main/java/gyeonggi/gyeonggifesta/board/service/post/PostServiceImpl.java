package gyeonggi.gyeonggifesta.board.service.post;

import gyeonggi.gyeonggifesta.board.dto.post.request.CreatePostReq;
import gyeonggi.gyeonggifesta.board.dto.post.request.UpdatePostReq;
import gyeonggi.gyeonggifesta.board.dto.post.response.PostEventOptionRes;
import gyeonggi.gyeonggifesta.board.dto.post.response.PostListRes;
import gyeonggi.gyeonggifesta.board.dto.post.response.PostRes;
import gyeonggi.gyeonggifesta.board.entity.Board;
import gyeonggi.gyeonggifesta.board.entity.Post;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final SecurityUtil securityUtil;
    private final PostMediaService postMediaService;
    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public void createPost(CreatePostReq request) {

        Member currentMember = securityUtil.getCurrentMember();

        Event event = validateEvent(request.getEventId());
        Board board = getOrCreateBoard(event);
        validateVisitDates(request.getVisitDates());

        Post post = Post.builder()
                .board(board)
                .member(currentMember)
                .event(event)
                .title(request.getTitle())
                .content(request.getContent())
                .visitDates(request.getVisitDates())
                .recruitPeople(request.getRecruitPeople())
                .recruitPeriod(request.getRecruitPeriod())
                .genderPreference(request.getGenderPreference())
                .ageRange(request.getAgeRange())
                .build();

        currentMember.addPost(post);
        board.addPost(post);

        postMediaService.createPostMedia(post, request.getKeyList());
        postRepository.save(post);

    }

    @Override
    public Page<PostListRes> getPosts(Long eventId, Pageable pageable, String sortType) {

        Event event = validateEvent(eventId);
        Board board = boardRepository.findByEventId(event.getId()).orElse(null);

        if (board == null) {
            return Page.empty(pageable);
        }

        Long boardId = board.getId();

        Page<Post> postsPage;
        if ("likes".equals(sortType)) {
            postsPage = postRepository.findByBoardIdOrderByLikesCountDesc(boardId, pageable);
        } else if ("comments".equals(sortType)) {
            postsPage = postRepository.findByBoardIdOrderByCommentsCountDesc(boardId, pageable);
        } else {
            postsPage = postRepository.findByBoardId(boardId, pageable);
        }

        return postsPage.map(post -> PostListRes.builder()
                .postId(post.getId())
                .eventId(post.getEvent() != null ? post.getEvent().getId() : null)
                .eventTitle(post.getEvent() != null ? post.getEvent().getTitle() : null)
                .eventMainImage(post.getEvent() != null ? post.getEvent().getMainImg() : null)
                .eventStartDate(post.getEvent() != null ? post.getEvent().getStartDate() : null)
                .eventEndDate(post.getEvent() != null ? post.getEvent().getEndDate() : null)
                .visitDates(List.copyOf(post.getVisitDates()))
                .recruitPeople(post.getRecruitPeople())
                .recruitPeriod(post.getRecruitPeriod())
                .genderPreference(post.getGenderPreference())
                .ageRange(post.getAgeRange())
                .title(post.getTitle())
                .writer(post.getMember().getUsername())
                .viewCount(post.getViewCount())
                .likes(post.getPostLikes().size())
                .comments(post.getPostComments().size())
                .updatedAt(post.getUpdatedAt().toLocalDate())
                .build());
    }

    @Override
    @Transactional
    public PostRes getPost(Long postId) {
        Post post = validatePost(postId);

        post.increaseViewCount();

        return PostRes.builder()
                .postId(post.getId())
                .eventId(post.getEvent() != null ? post.getEvent().getId() : null)
                .eventTitle(post.getEvent() != null ? post.getEvent().getTitle() : null)
                .eventMainImage(post.getEvent() != null ? post.getEvent().getMainImg() : null)
                .eventStartDate(post.getEvent() != null ? post.getEvent().getStartDate() : null)
                .eventEndDate(post.getEvent() != null ? post.getEvent().getEndDate() : null)
                .visitDates(List.copyOf(post.getVisitDates()))
                .recruitPeople(post.getRecruitPeople())
                .recruitPeriod(post.getRecruitPeriod())
                .genderPreference(post.getGenderPreference())
                .ageRange(post.getAgeRange())
                .title(post.getTitle())
                .content(post.getContent())
                .writer(post.getMember().getUsername())
                .viewCount(post.getViewCount())
                .likes(post.getPostLikes().size())
                .comments(post.getPostComments().size())
                .updatedAt(post.getUpdatedAt())
                .build();
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
        if (request.getEventId() != null) {
            Event event = validateEvent(request.getEventId());
            Board newBoard = getOrCreateBoard(event);
            Board previousBoard = post.getBoard();

            if (previousBoard != null && !previousBoard.equals(newBoard)) {
                previousBoard.removePost(post);
            }

            post.setEvent(event);

            if (previousBoard == null || !previousBoard.equals(newBoard)) {
                newBoard.addPost(post);
            }
        }
        if (request.getVisitDates() != null) {
            validateVisitDates(request.getVisitDates());
            post.updateVisitDates(request.getVisitDates());
        }
        if (request.getRecruitPeople() != null) {
            post.setRecruitPeople(request.getRecruitPeople());
        }
        if (request.getRecruitPeriod() != null) {
            post.setRecruitPeriod(request.getRecruitPeriod());
        }
        if (request.getGenderPreference() != null) {
            post.setGenderPreference(request.getGenderPreference());
        }
        if (request.getAgeRange() != null) {
            post.setAgeRange(request.getAgeRange());
        }

        if (request.getKeyList() != null) {
            postMediaService.updatePostMedia(post, request.getKeyList());
        }

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
        if (board != null) {
            board.removePost(post);
        }

        postRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostEventOptionRes> getAvailableEvents(Status status) {
        List<Event> events;

        if (status != null) {
            events = eventRepository.findAllByStatus(status);
        } else {
            events = eventRepository.findAll();
        }

        LocalDate today = LocalDate.now();

        return events.stream()
                .filter(Objects::nonNull)
                .filter(event -> status != null || event.getStatus() == null || event.getStatus() != Status.END)
                .filter(event -> status != null || event.getEndDate() == null || !event.getEndDate().isBefore(today))
                .sorted(Comparator.comparing(
                        Event::getStartDate,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .map(event -> PostEventOptionRes.builder()
                        .eventId(event.getId())
                        .title(event.getTitle())
                        .mainImage(event.getMainImg())
                        .startDate(event.getStartDate())
                        .endDate(event.getEndDate())
                        .build())
                .toList();
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

    private Event validateEvent(Long eventId) {
        if (eventId == null) {
            throw new BusinessException(EventErrorCode.NOT_EXIST_EVENT);
        }

        return eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(EventErrorCode.NOT_EXIST_EVENT));
    }

    private void validateVisitDates(List<LocalDate> visitDates) {
        if (visitDates == null || visitDates.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();

        boolean hasInvalidDate = visitDates.stream()
                .anyMatch(date -> Objects.isNull(date) || date.isBefore(today));

        if (hasInvalidDate) {
            throw new BusinessException(BoardErrorCode.INVALID_VISIT_DATE);
        }
    }

    private Board getOrCreateBoard(Event event) {
        return boardRepository.findByEventId(event.getId())
                .orElseGet(() -> boardRepository.save(Board.builder()
                        .name(event.getTitle())
                        .event(event)
                        .build()));
    }
}
