package gyeonggi.gyeonggifesta.schedule.exception;

import gyeonggi.gyeonggifesta.util.response.error_code.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ScheduleErrorCode implements ErrorCode {

	NOT_EXIST_SCHEDULE("SCH-001", HttpStatus.NOT_FOUND, "존재하지 않는 일정입니다.");

	private final String code;
	private final HttpStatus httpStatus;
	private final String message;

	ScheduleErrorCode(String code, HttpStatus httpStatus, String message) {
		this.code = code;
		this.httpStatus = httpStatus;
		this.message = message;
	}
}
