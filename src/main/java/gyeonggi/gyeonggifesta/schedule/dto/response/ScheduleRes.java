package gyeonggi.gyeonggifesta.schedule.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRes {

	private Long scheduleId;
	private String title;
	private LocalDate eventDate;
	private String memo;
	private Long chatRoomId;   // 동행 채팅방에서 온 일정이면 채팅방 ID 세팅, 아니면 null
}
