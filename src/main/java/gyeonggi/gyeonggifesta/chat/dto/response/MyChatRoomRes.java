package gyeonggi.gyeonggifesta.chat.dto.response;

import gyeonggi.gyeonggifesta.chat.enums.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyChatRoomRes {

	private Long chatRoomId;
	private String name;
	private int participation;
	private ChatRoomType type;
	private String createdFrom;
	private Long createdFromId;
	private int notReadMessageCount;
	private String lastMessageTime;
	private String lastMessageText;
}
