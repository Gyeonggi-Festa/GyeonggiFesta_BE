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
import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.chat.entity.ChatRoomMember;
import gyeonggi.gyeonggifesta.chat.enums.ChatRole;
import gyeonggi.gyeonggifesta.chat.enums.ChatRoomMemberStatus;
import gyeonggi.gyeonggifesta.chat.enums.ChatRoomType;
import gyeonggi.gyeonggifesta.chat.repository.ChatRoomRepository;
import gyeonggi.gyeonggifesta.chat.service.chatroom.ChatRoomMembershipService;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

	private static final String COMPANION_BOARD_NAME = "동행찾기";
	// 게시글로부터 생성된 채팅방 출처 구분값
	private static final String FROM_TYPE_POST = "POST";

	private final SecurityUtil securityUtil;
	private final PostMediaService postMediaService;
	private final BoardRepository boardRepository;
	private final PostRepository postRepository;
	private final EventRepository eventRepository;

	// 채팅 관련 의존성
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomMembershipService chatRoomMembershipService;

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

		initAvailableDates(post, request.getVisitDates());

		postMediaService.createPostMedia(post, request.getKeyList());

		// 저장 후 엔티티 반환
		Post savedPost = postRepository.save(post);

		// 게시글 전용 채팅방 생성 (방장 = 게시글 작성자)
		createPostChatRoom(savedPost, currentMember);
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
			syncAvailableDatesForUpdate(post, request.getVisitDates());
		}

		if (request.getRecruitmentTotal() != null || request.getRecruitmentPeriodDays() != null) {
			Integer total = request.getRecruitmentTotal() != null
				? request.getRecruitmentTotal()
				: post.getRecruitmentTotal();
			Integer period = request.getRecruitmentPeriodDays() != null
				? request.getRecruitmentPeriodDays()
				: post.getRecruitmentPeriodDays();
			validateRecruitmentInfo(total, period);
			post.setRecruitmentTotal(total);
			post.setRecruitmentPeriodDays(period);
		}

		if (request.getPreferredGender() != null) {
			post.setPreferredGender(parsePreferredGender(request.getPreferredGender()));
		}

		if (request.getPreferredMinAge() != null || request.getPreferredMaxAge() != null) {
			Integer minAge = request.getPreferredMinAge() != null
				? request.getPreferredMinAge()
				: post.getPreferredMinAge();
			Integer maxAge = request.getPreferredMaxAge() != null
				? request.getPreferredMaxAge()
				: post.getPreferredMaxAge();
			validateAgeRange(minAge, maxAge);
			post.setPreferredMinAge(minAge);
			post.setPreferredMaxAge(maxAge);
		}

		postMediaService.updatePostMedia(post, request.getKeyList());
	}

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
			.collect(Collectors.toMap(Event::getId, Function.identity(), (existing, duplicate) -> existing,
				LinkedHashMap::new));

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

	private void initAvailableDates(Post post, List<LocalDate> visitDates) {
		if (visitDates == null || visitDates.isEmpty()) {
			throw new BusinessException(BoardErrorCode.INVALID_VISIT_DATE);
		}

		visitDates.stream()
			.filter(Objects::nonNull)
			.distinct()
			.sorted()
			.forEach(date -> {
				PostAvailableDate availableDate = PostAvailableDate.builder()
					.post(post)
					.visitDate(date)
					.build();
				post.addAvailableDate(availableDate);
			});
	}

	private void syncAvailableDatesForUpdate(Post post, List<LocalDate> visitDates) {
		if (visitDates == null) {
			return;
		}

		Set<LocalDate> uniqueDates = visitDates.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toCollection(TreeSet::new));

		Map<LocalDate, PostAvailableDate> existing = post.getAvailableDates().stream()
			.collect(Collectors.toMap(PostAvailableDate::getVisitDate, Function.identity(), (first, second) -> first));

		// 제거 대상
		new ArrayList<>(post.getAvailableDates()).forEach(availableDate -> {
			if (!uniqueDates.contains(availableDate.getVisitDate())) {
				post.removeAvailableDate(availableDate);
			}
		});

		// 신규 추가
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
			.viewCount(post.getViewCount())   // ✅ long → long
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

		// 게시글에서 생성된 전용 채팅방 찾기 (없으면 null)
		Long chatRoomId = chatRoomRepository
			.findFirstByFromTypeAndFromId(FROM_TYPE_POST, post.getId())
			.map(ChatRoom::getId)
			.orElse(null);

		return PostRes.builder()
			.postId(post.getId())
			.title(post.getTitle())
			.content(post.getContent())
			.writer(post.getMember().getUsername())
			.viewCount(post.getViewCount())   // ✅ long → long
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
			.chatRoomId(chatRoomId)
			.build();
	}

	private List<LocalDate> extractVisitDates(Post post) {
		return post.getAvailableDates().stream()
			.map(PostAvailableDate::getVisitDate)
			.sorted()
			.collect(Collectors.toList());
	}

	/**
	 * 게시글 전용 채팅방 생성
	 * - 채팅방 이름: 행사명이 있으면 행사명, 없으면 게시글 제목
	 * - 설명: 게시글 내용 앞부분 50자
	 * - category: 필요 시 추후 확장
	 */
	private void createPostChatRoom(Post post, Member owner) {

		Event event = post.getEvent();

		// 채팅방 이름: 행사명 우선, 없으면 게시글 제목
		String roomName = (event != null && event.getTitle() != null && !event.getTitle().isBlank())
			? event.getTitle()
			: post.getTitle();

		// 설명: 게시글 내용 앞부분
		String info = (post.getContent() != null && !post.getContent().isBlank())
			? truncate(post.getContent(), 50)
			: "이 게시글을 위한 동행 채팅방입니다.";

		ChatRoom chatRoom = ChatRoom.builder()
			.name(roomName)
			.information(info)
			.category(event.getCodename())
			.type(ChatRoomType.GROUP)
			.fromType(FROM_TYPE_POST)
			.fromId(post.getId())
			.owner(owner)
			.build();

		// 방장을 채팅방 멤버로 등록 (저장 X, 연관관계만 세팅)
		ChatRoomMember ownerMember = ChatRoomMember.builder()
			.chatRoom(chatRoom)
			.member(owner)
			.role(ChatRole.OWNER)
			.joinedAt(LocalDateTime.now())
			.status(ChatRoomMemberStatus.ACTIVE)
			.build();

		chatRoom.addChatRoomMember(ownerMember);
		chatRoomRepository.save(chatRoom);
	}

	/**
	 * 문자열을 최대 길이만큼 잘라주고, 잘렸으면 "..."를 붙임
	 */
	private String truncate(String text, int maxLength) {
		if (text == null)
			return null;
		if (text.length() <= maxLength)
			return text;
		return text.substring(0, maxLength) + "...";
	}
}
