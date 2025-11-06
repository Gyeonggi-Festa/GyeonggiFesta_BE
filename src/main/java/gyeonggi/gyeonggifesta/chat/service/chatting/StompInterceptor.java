package gyeonggi.gyeonggifesta.chat.service.chatting;

import gyeonggi.gyeonggifesta.auth.exception.AuthErrorCode;
import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.chat.enums.ChatRoomMemberStatus;
import gyeonggi.gyeonggifesta.chat.exception.ChatErrorCode;
import gyeonggi.gyeonggifesta.chat.repository.ChatRoomMemberRepository;
import gyeonggi.gyeonggifesta.chat.repository.ChatRoomRepository;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.member.repository.MemberRepository;
import gyeonggi.gyeonggifesta.util.jwt.JwtTokenProvider;
import gyeonggi.gyeonggifesta.util.response.error_code.ErrorCode;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 웹소켓 연결 및 STOMP 메시지를 가로채는 인터셉터
 * - 사용자 인증 처리
 * - 채팅방 접근 권한 검증
 * - 연결 관리
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class StompInterceptor implements ChannelInterceptor {

	private final JwtTokenProvider jwtTokenProvider;
	private final SecurityUtil securityUtil;
	private final MemberRepository memberRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;


	private final ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider;

	private SimpMessagingTemplate getMessagingTemplate() {
		return messagingTemplateProvider.getObject();
	}


	/**
	 * 메시지가 전송되기 전에 호출되는 메서드
	 * - 인증 및 권한 검사 수행
	 */
	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		// 웹소켓 연결 수립 시 (CONNECT)
		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			// 1. JWT 토큰 추출 및 검증
			String token = accessor.getFirstNativeHeader("Authorization");

			if (token != null && token.startsWith("Bearer ")) {
				token = token.substring(7);
			}

			// 2. 토큰이 유효하면 인증 정보 설정
			if (jwtTokenProvider.validateToken(token)) {
				String verifyId = jwtTokenProvider.getClaims(token).getSubject();
				accessor.getSessionAttributes().put("verifyId", verifyId);

				accessor.setUser(() -> verifyId);
				log.info("WebSocket 연결 성공: {}", verifyId);
			} else {
				throw new BusinessException(AuthErrorCode.INVALID_ACCESS_TOKEN);
			}
		}

		// 특정 채팅방 구독 시 (SUBSCRIBE)
		else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
			// 구독 대상 경로 확인
			String destination = accessor.getDestination();
			if (destination != null && (
				destination.startsWith("/topic/chat/room/") ||
					destination.matches("/topic/chat/room/\\d+(/.*)?")
			)) {
				try {
					// 채팅방 ID 추출
					Long chatRoomId = extractRoomIdFromDestination(destination);
					if (chatRoomId == null) {
						log.error("채팅방 ID를 추출할 수 없습니다: {}", destination);
						return null;
					}

					// 세션 속성에서 verifyId 가져오기
					String verifyId = (String) accessor.getSessionAttributes().get("verifyId");
					if (verifyId == null && accessor.getUser() != null) {
						verifyId = accessor.getUser().getName();
					}

					if (verifyId == null) {
						log.error("사용자 인증 정보가 없습니다: {}", destination);
						return null;
					}

					// verifyId로 회원 정보 조회
					Member member = memberRepository.findByVerifyId(verifyId)
						.orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));

					// 채팅방 존재 및 권한 확인
					ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
						.orElseThrow(() -> new BusinessException(ChatErrorCode.NOT_EXIST_CHATROOM));

					chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, member)
						.ifPresent(crm -> {
							if (crm.getStatus() == ChatRoomMemberStatus.EXIT) {
								sendErrorToClient(accessor, ChatErrorCode.EXITED_CHATROOM_MEMBER);
								throw new BusinessException(ChatErrorCode.EXITED_CHATROOM_MEMBER);
							}

							if (crm.getKickedAt() != null) {
								// 강퇴된 회원인 경우
								sendErrorToClient(accessor, ChatErrorCode.KICKED_CHATROOM_MEMBER);
								throw new BusinessException(ChatErrorCode.KICKED_CHATROOM_MEMBER);
							}
						});

					boolean isMember = chatRoomMemberRepository
						.existsByChatRoomAndMemberAndStatusNotAndKickedAtIsNull(
							chatRoom, member, ChatRoomMemberStatus.EXIT);

					if (!isMember) {
						sendErrorToClient(accessor, ChatErrorCode.NOT_EXIST_CHATROOM_MEMBER);
						return null;
					}

					log.info("채팅방({}) 구독 성공: {}", chatRoomId, verifyId);
				} catch (BusinessException e) {
					sendErrorToClient(accessor, e.getErrorCode());
					return null;
				} catch (Exception e) {
					log.error("구독 처리 오류: {}", e.getMessage(), e);
					return null;
				}
			}
		}
		return message;
	}

	/**
	 * 목적지 경로에서 채팅방 ID 추출
	 */
	private Long extractRoomIdFromDestination(String destination) {
		// 패턴 확인
		if (destination == null) {
			return null;
		}

		// "/topic/chat/room/123" 또는 "/topic/chat/room/123/status" 형태 모두 처리
		String[] parts = destination.split("/");

		// 기본 패턴 "/topic/chat/room/{roomId}"
		if (parts.length >= 5) {
			try {
				return Long.parseLong(parts[4]);
			} catch (NumberFormatException e) {
				log.error("채팅방 ID 추출 실패: {}", destination, e);
				throw new BusinessException(ChatErrorCode.NOT_EXIST_CHATROOM);
			}
		}

		log.error("잘못된 목적지 형식: {}", destination);
		throw new BusinessException(ChatErrorCode.NOT_EXIST_CHATROOM);
	}

	/**
	 * 클라이언트에게 에러 메시지 전송
	 */
	private void sendErrorToClient(StompHeaderAccessor accessor, ErrorCode errorCode) {
		try {
			String verifyId = null;
			if (accessor.getUser() != null) {
				verifyId = accessor.getUser().getName();
			} else if (accessor.getSessionAttributes() != null) {
				verifyId = (String) accessor.getSessionAttributes().get("verifyId");
			}

			if (verifyId == null) {
				log.warn("에러 메시지를 전송할 사용자 ID를 찾을 수 없습니다.");
				return;
			}

			// 에러 응답 형식 구성 (기존 Response 형식과 일치)
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("status", errorCode.getHttpStatus().value());

			Map<String, Object> error = new HashMap<>();
			error.put("code", errorCode.getCode());
			error.put("message", errorCode.getMessage());

			errorResponse.put("error", error);

			// 개인 에러 큐로 메시지 전송
			String destination = "/user/" + verifyId + "/queue/errors";
			getMessagingTemplate().convertAndSend(destination, errorResponse);

			log.info("에러 메시지 전송: [{}] {} -> {}", errorCode.getCode(), errorCode.getMessage(), verifyId);
		} catch (Exception e) {
			log.error("에러 메시지 전송 실패: {}", e.getMessage(), e);
		}
	}
}
