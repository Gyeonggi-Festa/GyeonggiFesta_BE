package gyeonggi.gyeonggifesta.chat.dto.request.chatting.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 클라이언트로 전송되는 채팅 메시지 응답 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
	private Long messageId;           // 메시지 ID
	private Long chatRoomId;          // 채팅방 ID
	private Long senderId;            // 발신자 ID
	private String senderName;        // 발신자 이름
	private String content;           // 메시지 내용
	private String type;              // 메시지 타입
	private LocalDateTime createdAt;  // 메시지 생성 시간
	private boolean isDeleted;        // 삭제 여부

	// 이미지/파일 첨부 관련 정보
	private String mediaUrl;          // 미디어 URL (있는 경우)
}
