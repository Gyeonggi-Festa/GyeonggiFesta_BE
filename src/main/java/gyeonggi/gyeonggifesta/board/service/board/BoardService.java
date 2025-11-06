package gyeonggi.gyeonggifesta.board.service.board;

import gyeonggi.gyeonggifesta.board.dto.board.request.CreateBoardReq;
import gyeonggi.gyeonggifesta.board.dto.board.request.UpdateBoardReq;
import gyeonggi.gyeonggifesta.board.dto.board.response.BoardListRes;

import java.util.List;

public interface BoardService {

	/**
	 * 게시판 생성
	 *
	 * @param request 이름
	 */
	void createBoard(CreateBoardReq request);

	/**
	 * 게시판 이름 수정
	 *
	 * @param request 이름, id
	 */
	void updateBoard(UpdateBoardReq request);

	/**
	 * 게시판 삭제
	 *
	 * @param boardId id
	 */
	void removeBoard(Long boardId);

	List<BoardListRes> getAllBoardName();
}
