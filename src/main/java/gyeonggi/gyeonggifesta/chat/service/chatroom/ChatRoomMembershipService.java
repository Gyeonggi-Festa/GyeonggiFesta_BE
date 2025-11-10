package gyeonggi.gyeonggifesta.chat.service.chatroom;

import gyeonggi.gyeonggifesta.auth.exception.AuthErrorCode;
import gyeonggi.gyeonggifesta.chat.dto.request.chatroom.InviteChatRoomReq;
import gyeonggi.gyeonggifesta.chat.dto.request.chatting.response.ChatMessageResponse;
import gyeonggi.gyeonggifesta.chat.dto.request.chatting.response.ChatUserStatusEvent;
import gyeonggi.gyeonggifesta.chat.entity.ChatMessage;
import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.chat.entity.ChatRoomMember;
import gyeonggi.gyeonggifesta.chat.enums.ChatRole;
import gyeonggi.gyeonggifesta.chat.enums.ChatRoomMemberStatus;
import gyeonggi.gyeonggifesta.chat.exception.ChatErrorCode;
import gyeonggi.gyeonggifesta.chat.repository.ChatRoomMemberRepository;
import gyeonggi.gyeonggifesta.chat.repository.ChatRoomRepository;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.member.repository.MemberRepository;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 채팅방 멤버십 관련 서비스
 * - 채팅방 참여, 초대, 탈퇴 등 멤버십 관련 기능을 담당
 */
@Service
@RequiredArgsConstructor
public class ChatRoomMembershipService {

	private final SecurityUtil securityUtil;
	private final ChatRoomMemberRepository chatRoomMemberRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final MemberRepository memberRepository;
	private final ChatRoomValidator validator;

	private final ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider;

	private SimpMessagingTemplate getMessagingTemplate() {
		return messagingTemplateProvider.getObject();
	}

	/**
	 * 채팅방 탈퇴
	 * - 방장은 탈퇴할 수 없음
	 */
	@Transactional
	public void exitChatRoom(Long chatRoomId, String verifyId) {
		ChatRoom chatRoom = validator.validateAndGetChatRoom(chatRoomId);
		Member currentMember = securityUtil.getCurrentMember(verifyId);

		ChatRoomMember chatRoomMember = validator.validateAndGetChatRoomMember(chatRoom, currentMember);

		if (chatRoomMember.getRole().equals(ChatRole.OWNER)) {
			throw new BusinessException(ChatErrorCode.OWNER_CANNOT_EXIT);
		}

		chatRoomMember.setLastReadAt(LocalDateTime.now());
		chatRoomMember.setStatus(ChatRoomMemberStatus.EXIT);
		sendExitEvent(chatRoom.getId(), currentMember);
	}


	/**
	 * 채팅방 나가기 이벤트 발송
	 */
	private void sendExitEvent(Long chatRoomId, Member member) {
		// 상태 이벤트 생성
		ChatUserStatusEvent event = ChatUserStatusEvent.builder()
			.chatRoomId(chatRoomId)
			.memberId(member.getId())
			.memberName(member.getUsername())
			.eventType("EXIT") // 'LEAVE'와 구분하기 위해 'EXIT' 사용
			.timestamp(LocalDateTime.now())
			.build();

		// WebSocket을 통해 이벤트 발송
		getMessagingTemplate().convertAndSend(
			"/topic/chat/room/" + chatRoomId + "/status",
			event
		);

		ChatMessage systemMessage = ChatMessage.builder()
			.chatRoom(chatRoomRepository.getReferenceById(chatRoomId))
			.content(member.getUsername() + "님이 채팅방을 나갔습니다.")
			.type("SYSTEM")
			.build();

		ChatMessageResponse messageResponse = ChatMessageResponse.builder()
			.messageId(systemMessage.getId())
			.chatRoomId(chatRoomId)
			.senderId(null)
			.senderName("SYSTEM")
			.content(systemMessage.getContent())
			.type(systemMessage.getType())
			.createdAt(LocalDateTime.now())
			.isDeleted(false)
			.build();

		getMessagingTemplate().convertAndSend(
			"/topic/chat/room/" + chatRoomId,
			messageResponse
		);
	}

	/**
	 * 채팅방 참여 (join)
	 */
	@Transactional
	public void joinChatRoom(Long chatRoomId) {
		ChatRoom chatRoom = validator.validateAndGetChatRoom(chatRoomId);
		Member currentMember = securityUtil.getCurrentMember();

		// 이미 참여한 멤버인지 확인
		validator.validateChatRoomMemberNotExists(chatRoom, currentMember);
		validator.validateChatRoomMemberKicked(chatRoom, currentMember);

		// 채팅방 멤버로 등록
		ChatRoomMember chatRoomMember = createChatRoomMember(chatRoom, currentMember, ChatRole.USER);
		chatRoom.addChatRoomMember(chatRoomMember);
	}

	/**
	 * 채팅방 초대 (이메일, verifyId)
	 */
	@Transactional
	public void inviteChatRoom(InviteChatRoomReq request) {
		ChatRoom chatRoom = validator.validateAndGetChatRoom(request.getChatRoomId());

		// 초대할 멤버 조회
		Member member = validateInviteChatRoomReq(request);

		// 이미 참여한 멤버인지 확인
		validator.validateChatRoomMemberNotExists(chatRoom, member);

		// 채팅방 멤버로 등록
		ChatRoomMember chatRoomMember = createChatRoomMember(chatRoom, member, ChatRole.USER);
		chatRoom.addChatRoomMember(chatRoomMember);
	}

	private Member validateInviteChatRoomReq(InviteChatRoomReq request) {
		return request.getVerifyId() != null ? memberRepository.findByVerifyId(request.getVerifyId()).orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND))
			: memberRepository.findByEmail(request.getEmail()).orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));
	}

	/**
	 * 채팅방 멤버 생성 및 저장
	 */
	@Transactional
	public ChatRoomMember createChatRoomMember(ChatRoom chatRoom, Member member, ChatRole role) {
		ChatRoomMember crm = ChatRoomMember.builder()
			.chatRoom(chatRoom)
			.member(member)
			.role(role)
			.joinedAt(LocalDateTime.now())
			.status(ChatRoomMemberStatus.ACTIVE)
			.build();

		return chatRoomMemberRepository.save(crm);
	}

	@Transactional
	public void kickChatRoomMember(ChatRoom chatRoom, Member member) {
		ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, member)
			.orElseThrow(() -> new BusinessException(ChatErrorCode.NOT_EXIST_CHATROOM_MEMBER));

		chatRoomMember.setStatus(ChatRoomMemberStatus.KICKED);
		chatRoomMember.setKickedAt(LocalDateTime.now());
	}
}