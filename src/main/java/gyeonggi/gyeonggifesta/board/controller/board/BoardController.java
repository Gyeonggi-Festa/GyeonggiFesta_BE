package gyeonggi.gyeonggifesta.board.controller.board;

import gyeonggi.gyeonggifesta.board.dto.board.request.CreateBoardReq;
import gyeonggi.gyeonggifesta.board.dto.board.request.UpdateBoardReq;
import gyeonggi.gyeonggifesta.board.dto.board.response.BoardListRes;
import gyeonggi.gyeonggifesta.board.service.board.BoardService;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BoardController {

	private final BoardService boardService;

	@PostMapping("/auth/admin/board")
	public ResponseEntity<Response<Void>> createBoard(@RequestBody CreateBoardReq request) {
		boardService.createBoard(request);

		return Response.ok().toResponseEntity();
	}

	@PatchMapping("/auth/admin/board")
	public ResponseEntity<Response<Void>> updateBoard(@RequestBody UpdateBoardReq request) {
		boardService.updateBoard(request);

		return Response.ok().toResponseEntity();
	}

	@DeleteMapping("/auth/admin/board/{boardId}")
	public ResponseEntity<Response<Void>> removeBoard(@PathVariable Long boardId) {
		boardService.removeBoard(boardId);

		return Response.ok().toResponseEntity();
	}

	@GetMapping("/auth/user/board")
	public ResponseEntity<Response<List<BoardListRes>>> getBoardList() {
		List<BoardListRes> boardList = boardService.getAllBoardName();

		return Response.ok(boardList).toResponseEntity();
	}
}
