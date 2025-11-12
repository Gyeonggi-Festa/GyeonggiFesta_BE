package gyeonggi.gyeonggifesta.calendar.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/** 프런트 HTTPS 콜백에서 받은 code/state를 서버로 전달할 때 사용 */
@Getter
@NoArgsConstructor
public class CalendarCodeExchangeReq {
    private String code;
    private String state;
}
