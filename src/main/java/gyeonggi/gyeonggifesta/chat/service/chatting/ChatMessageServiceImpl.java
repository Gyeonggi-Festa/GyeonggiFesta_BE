package gyeonggi.gyeonggifesta.chat.service.chatting;

import gyeonggi.gyeonggifesta.chat.dto.request.chatting.request.ChatMessageRequest;
import gyeonggi.gyeonggifesta.chat.dto.request.chatting.response.ChatMessageResponse;
import gyeonggi.gyeonggifesta.chat.dto.request.chatting.response.ChatUserStatusEvent;
import gyeonggi.gyeonggifesta.chat.entity.ChatMedia;
import gyeonggi.gyeonggifesta.chat.entity.ChatMessage;
import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.chat.entity.ChatRoomMember;
import gyeonggi.gyeonggifesta.chat.exception.ChatErrorCode;
import gyeonggi.gyeonggifesta.chat.repository.ChatMediaRepository;
import gyeonggi.gyeonggifesta.chat.repository.ChatMessageRepository;
import gyeonggi.gyeonggifesta.chat.repository.ChatRoomMemberRepository;
import gyeonggi.gyeonggifesta.chat.repository.ChatRoomRepository;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;
	private final ChatMediaRepository chatMediaRepository;
	private final SecurityUtil securityUtil;

	// WebSocket 메시지 발송을 위한 템플릿
	private final SimpMessagingTemplate messagingTemplate;

	private final String MEDIA_URL = "https://gyeonggifesta.s3.ap-northeast-2.amazonaws.com/";

	@Override
	@Transactional
	public ChatMessageResponse sendMessage(ChatMessageRequest messageRequest, String verifyId) {
		ChatRoom chatRoom = validateAndGetChatRoom(messageRequest.getChatRoomId());
		Member sender = securityUtil.getCurrentMember(verifyId);

		validateChatRoomMember(chatRoom, sender);

		ChatMessage chatMessage = saveChatMessage(chatRoom, sender, messageRequest);

		String mediaUrl = null;
		if (messageRequest.getTempS3Key() != null && !messageRequest.getTempS3Key().isEmpty()) {
			String s3Key = messageRequest.getTempS3Key();
			ChatMedia chatMedia = saveChatMedia(chatMessage, s3Key);

			mediaUrl = MEDIA_URL + s3Key;
		}

		ChatMessageResponse response = createChatMessageResponse(chatMessage, sender, mediaUrl);

		// WebSocket을 통해 채팅방 구독자에게 메시지 발송
		messagingTemplate.convertAndSend(
			"/topic/chat/room/" + chatRoom.getId(),
			response
		);

		return response;
	}

	/**
	 * 채팅방 메시지 목록 조회
	 */
	@Override
	public Page<ChatMessageResponse> getMessages(Long chatRoomId, String verifyId, Pageable pageable) {
		ChatRoom chatRoom = validateAndGetChatRoom(chatRoomId);
		Member member = securityUtil.getCurrentMember(verifyId);

		validateChatRoomMember(chatRoom, member);

		Page<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable);

		return messages.map(message -> {
			// 미디어 URL 조회
			String mediaUrl = null;
			List<ChatMedia> mediaList = chatMediaRepository.findByChatMessage(message);
			if (!mediaList.isEmpty()) {
				mediaUrl = MEDIA_URL + mediaList.get(0).getS3Key();
			}

			return createChatMessageResponse(message, message.getSender(), mediaUrl);
		});
	}

	/**
	 * 메시지 삭제 (soft delete)
	 */
	@Override
	@Transactional
	public void deleteMessage(Long messageId, String verifyId) {

		ChatMessage message = chatMessageRepository.findById(messageId)
			.orElseThrow(() -> new BusinessException(ChatErrorCode.NOT_EXIST_MESSAGE));

		Member member = securityUtil.getCurrentMember(verifyId);

		if (!message.getSender().getId().equals(member.getId())) {
			throw new BusinessException(ChatErrorCode.NOT_MY_MESSAGE);
		}

		// 삭제 처리
		message.markAsDeleted();
		ChatMessageResponse response = createChatMessageResponse(message, member, null);

		if (message.getDeletedAt() != null) {
			response.setContent("삭제된 메시지입니다.");
			response.setMediaUrl(null); // 첨부 미디어도 표시하지 않음
		}

		messagingTemplate.convertAndSend(
			"/topic/chat/room/" + message.getChatRoom().getId(),
			response
		);
	}

	/**
	 * 채팅방 입장 처리
	 */
	@Override
	@Transactional
	public void enterChatRoom(Long roomId, String verifyId) {
		ChatRoom chatRoom = validateAndGetChatRoom(roomId);
		Member member = securityUtil.getCurrentMember(verifyId);

		ChatRoomMember chatRoomMember = validateAndGetChatRoomMember(chatRoom, member);

		// 마지막 읽은 시간 업데이트
		chatRoomMember.setLastReadAt(LocalDateTime.now());

		ChatUserStatusEvent event = ChatUserStatusEvent.builder()
			.chatRoomId(roomId)
			.memberId(member.getId())
			.memberName(member.getUsername())
			.eventType("JOIN")
			.timestamp(LocalDateTime.now())
			.build();

		messagingTemplate.convertAndSend(
			"/topic/chat/room/" + roomId + "/status",
			event
		);

	}

	/**
	 * 채팅방 퇴장 처리
	 */
	@Override
	@Transactional
	public void leaveChatRoom(Long roomId, String verifyId) {
		ChatRoom chatRoom = validateAndGetChatRoom(roomId);
		Member member = securityUtil.getCurrentMember(verifyId);

		ChatRoomMember chatRoomMember = validateAndGetChatRoomMember(chatRoom, member);

		// 마지막 읽은 시간 업데이트
		chatRoomMember.setLastReadAt(LocalDateTime.now());

		// 퇴장 이벤트 발송
		ChatUserStatusEvent event = ChatUserStatusEvent.builder()
			.chatRoomId(roomId)
			.memberId(member.getId())
			.memberName(member.getUsername())
			.eventType("LEAVE")
			.timestamp(LocalDateTime.now())
			.build();

		messagingTemplate.convertAndSend(
			"/topic/chat/room/" + roomId + "/status",
			event
		);
	}

	/**
	 * 메시지 읽음 처리
	 */
	@Override
	@Transactional
	public void markAsRead(Long roomId, String verifyId) {

		ChatRoom chatRoom = validateAndGetChatRoom(roomId);
		Member member = securityUtil.getCurrentMember(verifyId);

		ChatRoomMember chatRoomMember = validateAndGetChatRoomMember(chatRoom, member);

		// 마지막 읽은 시간 업데이트
		chatRoomMember.setLastReadAt(LocalDateTime.now());
	}

	/**
	 * 새 메시지 여부 확인
	 */
	@Override
	public List<Long> getNewMessageRooms(String verifyId) {
		Member member = securityUtil.getCurrentMember(verifyId);

		// 사용자가 참여한 채팅방 조회
		List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByMember(member);

		// 각 채팅방별 새 메시지 여부 확인
		return chatRoomMembers.stream()
			.filter(crm -> {
				// 마지막 읽은 시간 이후 메시지가 있는지 확인
				LocalDateTime lastReadAt = crm.getLastReadAt() != null ?
					crm.getLastReadAt() : crm.getJoinedAt();

				long count = chatMessageRepository.countByChatRoomAndCreatedAtAfter(
					crm.getChatRoom(), lastReadAt);

				return count > 0;
			})
			.map(crm -> crm.getChatRoom().getId())
			.collect(Collectors.toList());
	}

	@Override
	public Page<ChatMessageResponse> getMessagesBefore(Long chatRoomId, Long lastMessageId, String verifyId,
		Pageable pageable) {

		// 채팅방 및 사용자 조회
		ChatRoom chatRoom = validateAndGetChatRoom(chatRoomId);
		Member member = securityUtil.getCurrentMember(verifyId);

		// 채팅방 멤버인지 확인
		validateChatRoomMember(chatRoom, member);

		// 특정 메시지 ID 이전의 메시지 조회
		Page<ChatMessage> messages = chatMessageRepository.findByRoomIdAndIdLessThanOrderByCreatedAtDesc(
			chatRoomId, lastMessageId, pageable);

		// 응답 DTO 변환
		return messages.map(message -> {
			// 미디어 URL 조회
			String mediaUrl = null;
			List<ChatMedia> mediaList = chatMediaRepository.findByChatMessage(message);
			if (!mediaList.isEmpty()) {
				mediaUrl = MEDIA_URL + mediaList.get(0).getS3Key();
			}

			return createChatMessageResponse(message, message.getSender(), mediaUrl);
		});
	}

	//------------------// 유효성 검사 및 헬퍼 메서드 //------------------//

	/**
	 * 채팅방 유효성 검사 및 조회
	 */
	private ChatRoom validateAndGetChatRoom(Long chatRoomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new BusinessException(ChatErrorCode.NOT_EXIST_CHATROOM));

		if (chatRoom.getDeletedAt() != null) {
			throw new BusinessException(ChatErrorCode.DELETED_CHATROOM);
		}

		return chatRoom;
	}

	/**
	 * 채팅방 멤버 검증
	 */
	private void validateChatRoomMember(ChatRoom chatRoom, Member member) {
		boolean isMember = chatRoomMemberRepository.existsByChatRoomAndMember(chatRoom, member);
		if (!isMember) {
			throw new BusinessException(ChatErrorCode.NOT_EXIST_CHATROOM_MEMBER);
		}
	}

	/**
	 * 채팅방 멤버 검증 및 조회
	 */
	private ChatRoomMember validateAndGetChatRoomMember(ChatRoom chatRoom, Member member) {
		return chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, member)
			.orElseThrow(() -> new BusinessException(ChatErrorCode.NOT_EXIST_CHATROOM_MEMBER));
	}

	/**
	 * 채팅 메시지 저장
	 */
	private ChatMessage saveChatMessage(ChatRoom chatRoom, Member sender, ChatMessageRequest messageRequest) {
		ChatMessage chatMessage = ChatMessage.builder()
			.chatRoom(chatRoom)
			.sender(sender)
			.content(messageRequest.getContent())
			.type(messageRequest.getType())
			.build();

		return chatMessageRepository.save(chatMessage);
	}

	/**
	 * 채팅 미디어 저장
	 */
	private ChatMedia saveChatMedia(ChatMessage chatMessage, String s3Key) {
		ChatMedia chatMedia = ChatMedia.builder()
			.chatMessage(chatMessage)
			.s3Key(s3Key)
			.build();

		chatMessage.addChatMedia(chatMedia);
		return chatMediaRepository.save(chatMedia);
	}

	/**
	 * 채팅 메시지 응답 DTO 생성
	 */
	private ChatMessageResponse createChatMessageResponse(ChatMessage message, Member sender, String mediaUrl) {
		// 삭제된 메시지인지 확인
		boolean isDeleted = message.getDeletedAt() != null;

		ChatRoom chatRoom = message.getChatRoom();

		return ChatMessageResponse.builder()
			.messageId(message.getId())
			.chatRoomId(chatRoom.getId())
			.senderId(sender.getId())
			.senderName(sender.getUsername())
			// 삭제된 메시지는 내용을 "삭제된 메시지입니다"로 설정
			.content(isDeleted ? "삭제된 메시지입니다." : message.getContent())
			.type(message.getType())
			.createdAt(message.getCreatedAt())
			.isDeleted(isDeleted)
			// 삭제된 메시지는 미디어 URL을 표시하지 않음
			.mediaUrl(isDeleted ? null : mediaUrl)
			.build();
	}


}
