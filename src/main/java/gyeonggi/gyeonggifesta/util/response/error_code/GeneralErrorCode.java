package gyeonggi.gyeonggifesta.util.response.error_code;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum GeneralErrorCode implements ErrorCode {
    BAD_REQUEST("GEN-001", HttpStatus.BAD_REQUEST, "잘못된 요청"),
    INTERNAL_SERVER_ERROR("GEN-002", HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러"),
    INVALID_INPUT_VALUE("GEN-003", HttpStatus.BAD_REQUEST, "입력 값이 유효하지 않습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    GeneralErrorCode(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
