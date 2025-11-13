package gyeonggi.gyeonggifesta.chat.service.chatroom;

import gyeonggi.gyeonggifesta.chat.dto.request.chatroom.CreateCompanionChatRoomReq;
import gyeonggi.gyeonggifesta.chat.dto.response.CompanionChatRoomRes;
import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.chat.entity.ChatRoomMember;
import gyeonggi.gyeonggifesta.chat.entity.CompanionChatRoom;
import gyeonggi.gyeonggifesta.chat.enums.ChatRole;
import gyeonggi.gyeonggifesta.chat.enums.ChatRoomType;
import gyeonggi.gyeonggifesta.chat.repository.ChatRoomRepository;
import gyeonggi.gyeonggifesta.chat.repository.CompanionChatRoomRepository;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.schedule.service.ScheduleService;
import gyeonggi.gyeonggifesta.util.response.error_code.GeneralErrorCode;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CompanionChatRoomService {

	private final SecurityUtil securityUtil;
	private final ChatRoomValidator chatRoomValidator;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomMembershipService chatRoomMembershipService;
	private final CompanionChatRoomRepository companionChatRoomRepository;
	private final ScheduleService scheduleService;

	/**
	 * 동행찾기 채팅방 생성
	 * - 내부적으로는 GROUP 타입 ChatRoom을 하나 만들고
	 * - CompanionChatRoom에 eventDate를 연결해서 별도로 관리
	 * - 방장에 대해서는 바로 일정도 자동 생성
	 */
	@Transactional
	public void createCompanionChatRoom(CreateCompanionChatRoomReq request) {
		Member currentMember = securityUtil.getCurrentMember();

		// 공통 검증
		chatRoomValidator.validateChatRoomName(request.getName());

		if (request.getInformation() == null || request.getInformation().isBlank()) {
			throw new BusinessException(GeneralErrorCode.INVALID_INPUT_VALUE);
		}
		if (request.getCategory() == null || request.getCategory().isBlank()) {
			throw new BusinessException(GeneralErrorCode.INVALID_INPUT_VALUE);
		}
		if (request.getEventDate() == null) {
			throw new BusinessException(GeneralErrorCode.INVALID_INPUT_VALUE);
		}

		// 과거 날짜 방지
		LocalDate today = LocalDate.now();
		if (request.getEventDate().isBefore(today)) {
			throw new BusinessException(GeneralErrorCode.INVALID_INPUT_VALUE);
		}

		// 1) 실제 채팅방 생성 (GROUP)
		ChatRoom chatRoom = ChatRoom.builder()
				.name(request.getName())
				.information(request.getInformation())
				.category(request.getCategory())
				.type(ChatRoomType.GROUP)
				.fromType(null)
				.fromId(null)
				.owner(currentMember)
				.build();

		chatRoom = chatRoomRepository.save(chatRoom);

		// 2) 방장을 멤버로 등록
		ChatRoomMember ownerMember =
				chatRoomMembershipService.createChatRoomMember(chatRoom, currentMember, ChatRole.OWNER);
		chatRoom.addChatRoomMember(ownerMember);

		// 3) 동행 메타 정보 저장
		CompanionChatRoom companionChatRoom = CompanionChatRoom.builder()
				.chatRoom(chatRoom)
				.eventDate(request.getEventDate())
				.build();

		companionChatRoomRepository.save(companionChatRoom);

		// 4) 방장 일정 자동 등록 (예외는 내부에서 처리)
		scheduleService.createScheduleForCompanion(
				currentMember, chatRoom, request.getEventDate()
		);
	}

	/**
	 * 동행찾기 채팅방 목록 조회
	 * - category 필터 옵션
	 * - eventDate 기준 오름차순 정렬
	 */
	@Transactional(readOnly = true)
	public Page<CompanionChatRoomRes> listCompanionChatRooms(int page, int size, String category) {

		String categoryFilter = (category == null || category.isBlank()) ? null : category;

		PageRequest pageable = PageRequest.of(
				page - 1,
				size,
				Sort.by(Sort.Direction.ASC, "eventDate")
						.and(Sort.by(Sort.Direction.DESC, "id"))
		);

		Page<CompanionChatRoom> roomPage =
				companionChatRoomRepository.findCompanionRooms(ChatRoomType.GROUP, categoryFilter, pageable);

		return roomPage.map(this::toCompanionChatRoomRes);
	}

	private CompanionChatRoomRes toCompanionChatRoomRes(CompanionChatRoom companionChatRoom) {
		ChatRoom chatRoom = companionChatRoom.getChatRoom();

		return CompanionChatRoomRes.builder()
				.chatRoomId(chatRoom.getId())
				.name(chatRoom.getName())
				.participation(chatRoom.getChatRoomMembers().size())
				.information(chatRoom.getInformation())
				.category(chatRoom.getCategory())
				.eventDate(companionChatRoom.getEventDate())
				.build();
	}
}
