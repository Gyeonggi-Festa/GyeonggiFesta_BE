package gyeonggi.gyeonggifesta.board.dto.post.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CreatePostReq {

	private Long boardId;
	private String title;
	private String content;
	private List<String> keyList;

}
