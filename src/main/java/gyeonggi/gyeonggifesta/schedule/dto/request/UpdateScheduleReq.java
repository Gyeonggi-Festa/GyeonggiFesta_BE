package gyeonggi.gyeonggifesta.schedule.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UpdateScheduleReq {

	private String title;        // 변경할 제목(옵션)
	private LocalDate eventDate; // 변경할 날짜(옵션)
	private String memo;         // 변경할 메모(옵션)
}
