package gyeonggi.gyeonggifesta.calendar.error;

import gyeonggi.gyeonggifesta.util.response.error_code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CalendarErrorCode implements ErrorCode {

    CAL_STATE_INVALID("CAL-STATE-INVALID", "유효하지 않은 state 입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
