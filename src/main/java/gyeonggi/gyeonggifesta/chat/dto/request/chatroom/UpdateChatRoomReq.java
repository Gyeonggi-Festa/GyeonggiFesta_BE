package gyeonggi.gyeonggifesta.chat.dto.request.chatroom;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateChatRoomReq {

	private Long chatRoomId;
	private String name;
}
