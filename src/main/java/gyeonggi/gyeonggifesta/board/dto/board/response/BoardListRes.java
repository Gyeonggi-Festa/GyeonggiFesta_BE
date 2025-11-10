package gyeonggi.gyeonggifesta.board.dto.board.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardListRes {

	private Long boardId;
	private String name;
}
