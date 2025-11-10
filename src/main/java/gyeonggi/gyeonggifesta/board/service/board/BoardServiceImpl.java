package gyeonggi.gyeonggifesta.board.service.board;

import gyeonggi.gyeonggifesta.auth.exception.AuthErrorCode;
import gyeonggi.gyeonggifesta.board.dto.board.request.CreateBoardReq;
import gyeonggi.gyeonggifesta.board.dto.board.request.UpdateBoardReq;
import gyeonggi.gyeonggifesta.board.dto.board.response.BoardListRes;
import gyeonggi.gyeonggifesta.board.entity.Board;
import gyeonggi.gyeonggifesta.board.exception.BoardErrorCode;
import gyeonggi.gyeonggifesta.board.repository.BoardRepository;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.member.enums.Role;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService{

	private final SecurityUtil securityUtil;
	private final BoardRepository boardRepository;

	/**
	 * 게시판 생성
	 *
	 * @param request 이름
	 */
	@Override
	@Transactional
	public void createBoard(CreateBoardReq request) {
		validateAdmin();

		Board board = Board.builder()
			.name(request.getName())
			.build();

		boardRepository.save(board);
	}

	/**
	 * 게시판 이름 수정
	 *
	 * @param request 이름, id
	 */
	@Override
	@Transactional
	public void updateBoard(UpdateBoardReq request) {
		validateAdmin();

		Board board = boardRepository.findById(request.getBoardId())
			.orElseThrow(() -> new BusinessException(BoardErrorCode.NOT_EXIST_BOARD));

		board.setName(request.getName());
	}

	/**
	 * 게시판 삭제
	 *
	 * @param boardId id
	 */
	@Override
	@Transactional
	public void removeBoard(Long boardId) {
		validateAdmin();

		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new BusinessException(BoardErrorCode.NOT_EXIST_BOARD));

		boardRepository.delete(board);
	}

	private void validateAdmin() {

		Member member = securityUtil.getCurrentMember();

		if (!member.getRole().equals(Role.ROLE_ADMIN)) {
			throw new BusinessException(AuthErrorCode.INVALID_ROLE);
		}
	}

	@Override
	public List<BoardListRes> getAllBoardName() {

		List<Board> allBoards = boardRepository.findAll();

		return allBoards.stream().map(
			board -> BoardListRes.builder()
				.boardId(board.getId())
				.name(board.getName())
				.build()
		).toList();
	}
}
