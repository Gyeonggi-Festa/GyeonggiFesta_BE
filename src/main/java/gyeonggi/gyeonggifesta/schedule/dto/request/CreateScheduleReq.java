package gyeonggi.gyeonggifesta.schedule.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CreateScheduleReq {

	private String title;       // 일정 이름
	private LocalDate eventDate; // 일정 날짜
	private String memo;        // 메모 (옵션)
	private Long chatRoomId;    // 수동 생성 시에도 채팅방과 연결하고 싶으면 사용 (옵션)
}
