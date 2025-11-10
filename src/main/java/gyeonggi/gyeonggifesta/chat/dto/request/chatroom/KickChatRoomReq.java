package gyeonggi.gyeonggifesta.chat.dto.request.chatroom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KickChatRoomReq {

	private Long chatRoomId;
	private String verifyId;

}
