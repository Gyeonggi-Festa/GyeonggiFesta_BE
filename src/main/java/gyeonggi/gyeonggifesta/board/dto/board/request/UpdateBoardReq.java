package gyeonggi.gyeonggifesta.board.dto.board.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBoardReq {

	private Long boardId;
	private String name;
}
