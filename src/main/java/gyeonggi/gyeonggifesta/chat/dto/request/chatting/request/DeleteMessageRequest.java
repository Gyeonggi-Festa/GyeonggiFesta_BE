package gyeonggi.gyeonggifesta.chat.dto.request.chatting.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteMessageRequest {
	private Long messageId;  // 삭제할 메시지 ID
}
