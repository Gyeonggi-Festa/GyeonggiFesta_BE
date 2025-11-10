package gyeonggi.gyeonggifesta.chat.dto.request.chatting.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KickUserStatusEvent {

	private Long chatRoomId;           // 채팅방 ID
	private String eventType;          // 이벤트 타입 (JOIN, LEAVE)
	private String message;
	private LocalDateTime timestamp;   // 이벤트 발생 시간
}
