package gyeonggi.gyeonggifesta.chat.controller;

import gyeonggi.gyeonggifesta.auth.exception.AuthErrorCode;
import gyeonggi.gyeonggifesta.chat.dto.request.chatting.request.ChatMessageRequest;
import gyeonggi.gyeonggifesta.chat.dto.request.chatting.request.DeleteMessageRequest;
import gyeonggi.gyeonggifesta.chat.service.chatting.ChatMessageService;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

	private final ChatMessageService chatMessageService;

	/**
	 * 채팅 메시지 전송
	 * - 클라이언트가 "/app/chat/message"로 메시지를 보내면 처리
	 */
	@MessageMapping("/chat/message")
	public void sendMessage(@Payload ChatMessageRequest messageRequest,
		SimpMessageHeaderAccessor headerAccessor) {
		try {
			// 현재 인증된 사용자 정보 획득 (인터셉터에서 설정됨)
			String verifyId = getUserVerifyId(headerAccessor);

			// 서비스를 통해 메시지 처리 및 발송
			chatMessageService.sendMessage(messageRequest, verifyId);
		} catch (Exception e) {
			log.error("메시지 전송 오류: {}", e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 채팅방 입장 이벤트 처리
	 * - 클라이언트가 "/app/chat/room/{roomId}/enter"로 메시지를 보내면 처리
	 */
	@MessageMapping("/chat/room/{roomId}/enter")
	public void enterChatRoom(@DestinationVariable Long roomId,
		SimpMessageHeaderAccessor headerAccessor) {
		try {
			String verifyId = getUserVerifyId(headerAccessor);
			chatMessageService.enterChatRoom(roomId, verifyId);
		} catch (Exception e) {
			log.error("채팅방 입장 오류: {}", e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 채팅방 퇴장
	 */
	@MessageMapping("/chat/room/{roomId}/leave")
	public void leaveChatRoom(@DestinationVariable Long roomId,
		SimpMessageHeaderAccessor headerAccessor) {

		try {
			String verifyId = getUserVerifyId(headerAccessor);
			chatMessageService.leaveChatRoom(roomId, verifyId);
		} catch (Exception e) {
			log.error("채팅방 퇴장 오류: {}", e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 채팅 메시지 삭제
	 * - 클라이언트가 "/app/chat/message/delete"로 메시지를 보내면 처리
	 * - 삭제된 메시지는 웹소켓을 통해 실시간으로 모든 사용자에게 전달됨
	 */
	@MessageMapping("/chat/message/delete")
	public void deleteMessage(@Payload DeleteMessageRequest deleteRequest,
		SimpMessageHeaderAccessor headerAccessor) {
		try {
			// 현재 인증된 사용자 정보 획득
			String verifyId = getUserVerifyId(headerAccessor);

			// 서비스를 통해 메시지 삭제 처리
			// 삭제 후 웹소켓을 통해 해당 채팅방의 모든 사용자에게 삭제 이벤트 발송
			chatMessageService.deleteMessage(deleteRequest.getMessageId(), verifyId);
		} catch (Exception e) {
			log.error("메시지 삭제 오류: {}", e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 메시지 읽음 표시 처리
	 * - 클라이언트가 "/app/chat/room/{roomId}/read"로 메시지를 보내면 처리
	 */
	@MessageMapping("/chat/room/{roomId}/read")
	public void markAsRead(@DestinationVariable Long roomId,
		SimpMessageHeaderAccessor headerAccessor) {
		try {
			String verifyId = getUserVerifyId(headerAccessor);
			chatMessageService.markAsRead(roomId, verifyId);
		} catch (Exception e) {
			log.error("메시지 읽음 처리 오류: {}", e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 헤더 액세서에서 사용자 인증 ID 추출
	 * - 인터셉터에서 설정한 Principal 또는 세션 속성에서 가져옴
	 */
	private String getUserVerifyId(SimpMessageHeaderAccessor headerAccessor) {
		// 1. Principal에서 verifyId 시도
		if (headerAccessor.getUser() != null) {
			return headerAccessor.getUser().getName();
		}

		// 2. 세션 속성에서 verifyId 시도
		if (headerAccessor.getSessionAttributes() != null &&
			headerAccessor.getSessionAttributes().containsKey("verifyId")) {
			return (String) headerAccessor.getSessionAttributes().get("verifyId");
		}

		// 3. 세션 ID로 사용자 찾기 시도 (로그 출력용)
		String sessionId = headerAccessor.getSessionId();
		log.error("사용자 인증 정보를 찾을 수 없습니다. SessionId: {}", sessionId);

		throw new BusinessException(AuthErrorCode.UNAUTHORIZED);
	}
}
