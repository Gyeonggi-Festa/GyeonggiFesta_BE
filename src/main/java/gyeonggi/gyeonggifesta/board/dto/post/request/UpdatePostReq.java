package gyeonggi.gyeonggifesta.board.dto.post.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UpdatePostReq {
	private String title;
	private String content;
	private List<String> keyList;
}
