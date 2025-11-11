package gyeonggi.gyeonggifesta.chat.dto.request.chatroom;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateChatRoomReq {
	private String name;
	private String type;
	private String path;
	private String information;
	private String category;
}
