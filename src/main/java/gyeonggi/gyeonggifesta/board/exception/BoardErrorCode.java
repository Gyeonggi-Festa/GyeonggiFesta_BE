package gyeonggi.gyeonggifesta.board.exception;

import gyeonggi.gyeonggifesta.util.response.error_code.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BoardErrorCode implements ErrorCode {

	NOT_EXIST_BOARD("BOR-001", HttpStatus.BAD_REQUEST, "존재하지 않는 게시판"),
	NOT_EXIST_POST("BOR-002", HttpStatus.BAD_REQUEST, "존재하지 않는 게시글"),
	NOT_EXIST_COMMENT("BOR-003", HttpStatus.BAD_REQUEST, "존재하지 않는 댓글"),
	NOT_EXIST_LIKE("BOR-004", HttpStatus.BAD_REQUEST, "존재하지 않는 좋아요"),
	NOT_EXIST_MEDIA("BOR-005", HttpStatus.BAD_REQUEST, "존재하지 않는 미디어"),
	NOT_WRITER("BOR-006", HttpStatus.BAD_REQUEST, "작성자가 아닙니다"),
	ALREADY_LIKED("BOR-007", HttpStatus.BAD_REQUEST, "이미 좋아요를 누른 게시글"),
	PARENT_COMMENT_NOT_BELONG_TO_POST("BOR-008", HttpStatus.BAD_REQUEST, "게시글과 댓글의 위치가 다릅니다"),
	NESTED_COMMENT_NOT_ALLOWED("BOR-009", HttpStatus.BAD_REQUEST, "대댓글의 대댓글은 불가능합니다"),
	INVALID_EVENT_FOR_POST("BOR-010", HttpStatus.BAD_REQUEST, "게시글에 사용할 수 없는 행사입니다"),
	INVALID_VISIT_DATE("BOR-011", HttpStatus.BAD_REQUEST, "유효하지 않은 방문 가능 날짜입니다"),
	INVALID_RECRUITMENT_INFO("BOR-012", HttpStatus.BAD_REQUEST, "모집 인원 또는 기간 정보가 올바르지 않습니다"),
	INVALID_PREFERRED_GENDER("BOR-013", HttpStatus.BAD_REQUEST, "선호 성별 값이 올바르지 않습니다"),
	INVALID_AGE_RANGE("BOR-014", HttpStatus.BAD_REQUEST, "선호 연령대 값이 올바르지 않습니다"),
	;

	private final String code;
	private final HttpStatus httpStatus;
	private final String message;

	BoardErrorCode(String code, HttpStatus httpStatus, String message) {
		this.code = code;
		this.httpStatus = httpStatus;
		this.message = message;
	}
}
