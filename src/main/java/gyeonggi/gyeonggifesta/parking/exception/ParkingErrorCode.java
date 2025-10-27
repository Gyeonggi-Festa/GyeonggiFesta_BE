package gyeonggi.gyeonggifesta.parking.exception;

import gyeonggi.gyeonggifesta.util.response.error_code.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ParkingErrorCode implements ErrorCode {
	API_REQUEST_FAILED("PKG-001", HttpStatus.INTERNAL_SERVER_ERROR, "주차장 정보 API 호출 실패"),
	INVALID_RESPONSE("PKG-002", HttpStatus.INTERNAL_SERVER_ERROR, "주차장 API 응답 형식 오류"),
	NOT_FOUND("PKG-404", HttpStatus.NOT_FOUND, "대상이 존재하지 않습니다.");

	private final String code;
	private final HttpStatus httpStatus;
	private final String message;

	ParkingErrorCode(String code, HttpStatus httpStatus, String message) {
		this.code = code;
		this.httpStatus = httpStatus;
		this.message = message;
	}
}
