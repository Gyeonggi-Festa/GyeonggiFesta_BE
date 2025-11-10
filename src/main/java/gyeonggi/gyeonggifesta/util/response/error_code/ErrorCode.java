package gyeonggi.gyeonggifesta.util.response.error_code;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

	HttpStatus getHttpStatus();

	String getCode();

	String getMessage();

}
