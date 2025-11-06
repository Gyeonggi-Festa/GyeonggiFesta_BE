package gyeonggi.gyeonggifesta.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomRes {

	private Long chatRoomId;
	private String name;
	private int participation;
	private String information;
	private String category;
}
