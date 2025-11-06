package gyeonggi.gyeonggifesta.exception;

import gyeonggi.gyeonggifesta.util.response.Response;
import gyeonggi.gyeonggifesta.util.response.error_code.GeneralErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Response<Void>> handleCustomException(BusinessException ex) {
		Response<Void> response = Response.errorResponse(ex.getErrorCode());

		return response.toResponseEntity();
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Response<Void>> handleValidException(MethodArgumentNotValidException ex) {

		Response<Void> response = Response.errorResponse(GeneralErrorCode.INVALID_INPUT_VALUE);

		return response.toResponseEntity();
	}
}
