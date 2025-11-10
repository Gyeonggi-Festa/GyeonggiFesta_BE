package gyeonggi.gyeonggifesta.util.response;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import gyeonggi.gyeonggifesta.util.response.error_code.ErrorCode;

@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
	private String code;
	private int status;
	private String message;
	private T data;

	public static Response<Void> ok() {
		Response<Void> response = new Response<>();
		response.code = "GEN-000";
		response.status = HttpStatus.OK.value();
		return response;
	}

	public static <T> Response<T> ok(T data) {
		Response<T> response = new Response<>();
		response.code = "GEN-000";
		response.status = HttpStatus.OK.value();
		response.data = data;
		return response;
	}

	public static <T> Response<T> errorResponse(ErrorCode errorCode) {
		Response<T> response = new Response<>();
		response.code = errorCode.getCode();
		response.status = errorCode.getHttpStatus().value();
		response.message = errorCode.getMessage();
		response.data = null;
		return response;
	}

	public String convertToJson() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(this);
	}

	/**
	 * 현재 Response 객체를 ResponseEntity로 변환하여 반환.
	 *
	 * @return ResponseEntity로 래핑된 Response 객체
	 */
	public ResponseEntity<Response<T>> toResponseEntity() {
		HttpStatus httpStatus = HttpStatus.valueOf(this.status);
		return new ResponseEntity<>(this, httpStatus);
	}
}
