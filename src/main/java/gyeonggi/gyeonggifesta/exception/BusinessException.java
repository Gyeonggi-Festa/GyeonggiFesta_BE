package gyeonggi.gyeonggifesta.exception;

import lombok.Builder;
import lombok.Getter;
import gyeonggi.gyeonggifesta.util.response.error_code.ErrorCode;

public class BusinessException extends RuntimeException {

	@Getter
	private final ErrorCode errorCode;

	@Builder
	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
