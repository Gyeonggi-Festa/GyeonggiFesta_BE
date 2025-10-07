package gyeonggi.gyeonggifesta.auth.exception;

import gyeonggi.gyeonggifesta.util.response.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum AuthErrorCode implements ErrorCode {
	INVALID_OAUTH2_PROVIDER("AUTH-001", HttpStatus.BAD_REQUEST, "잘못된 로그인 방식"),
	USER_NOT_FOUND("AUTH-002", HttpStatus.NOT_FOUND, "존재하지 않는 유저"),
	UNAUTHORIZED("AUTH-003", HttpStatus.UNAUTHORIZED, "인증되지 않음"),
	EMAIL_DUPLICATED("AUTH-004", HttpStatus.BAD_REQUEST, "중복된 이메일"),
	INVALID_LOGIN_ARGUMENT("AUTH-005", HttpStatus.NOT_FOUND, "잘못된 비밀번호"),
	INVALID_ACCESS_TOKEN("AUTH-006", HttpStatus.UNAUTHORIZED, "잘못된 엑세스 토큰, 리프레시 토큰으로 요청하세요"),
	INVALID_REFRESH_TOKEN("AUTH-007", HttpStatus.UNAUTHORIZED, "잘못된 리프레시 토큰"),
	INVALID_ROLE("AUTH-008", HttpStatus.UNAUTHORIZED, "잘못된 권한으로 요청"),
	;

	private final String code;
	private final HttpStatus httpStatus;
	private final String message;

	AuthErrorCode(String code, HttpStatus httpStatus, String message) {
		this.code = code;
		this.httpStatus = httpStatus;
		this.message = message;
	}
}
