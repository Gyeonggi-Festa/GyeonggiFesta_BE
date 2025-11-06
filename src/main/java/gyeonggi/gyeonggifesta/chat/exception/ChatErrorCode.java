package gyeonggi.gyeonggifesta.chat.exception;

import gyeonggi.gyeonggifesta.util.response.error_code.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ChatErrorCode implements ErrorCode {

	NOT_EXIST_CHATROOM("CHT-001", HttpStatus.BAD_REQUEST, "존재하지 않는 채팅방"),
	NOT_EXIST_MESSAGE("CHT-002",HttpStatus.BAD_REQUEST, "존재하지 않는 채팅 메시지"),
	DELETED_CHATROOM("CHT-003",HttpStatus.BAD_REQUEST, "삭제된 채팅방"),
	INVALID_CHATROOM_NAME("CHT-004",HttpStatus.BAD_REQUEST, "형식에 어긋난 채팅방 이름"),
	NOT_EXIST_CHATROOM_MEMBER("CHT-005",HttpStatus.BAD_REQUEST, "존재하지 않는 채팅방 유저"),
	EXIST_CHATROOM_MEMBER("CHT-006",HttpStatus.BAD_REQUEST, "이미 참가중인 채팅방 유저"),
	OWNER_CANNOT_EXIT("CHT-007",HttpStatus.BAD_REQUEST, "방장은 탈퇴할 수 없습니다"),
	INVALID_ROLE("CHT-008",HttpStatus.BAD_REQUEST, "요청할 수 없는 권한"),
	NOT_MY_MESSAGE("CHT-009",HttpStatus.BAD_REQUEST, "다른 유저의 메시지는 삭제할 수 없습니다"),
	INVALID_FILE_TYPE("CHT-010",HttpStatus.BAD_REQUEST, "지원하지 않는 파일 타입"),
	FILE_UPLOAD_FAILED("CHT-011",HttpStatus.BAD_REQUEST, "파일 업로드 실패"),
	KICKED_CHATROOM_MEMBER("CHT-012",HttpStatus.BAD_REQUEST, "추방된 사용자"),
	EXITED_CHATROOM_MEMBER("CHT_013", HttpStatus.FORBIDDEN, "채팅방을 탈퇴한 회원입니다.");
	;

	private final String code;
	private final HttpStatus httpStatus;
	private final String message;

	ChatErrorCode(String code, HttpStatus httpStatus, String message) {
		this.code = code;
		this.httpStatus = httpStatus;
		this.message = message;
	}
}
