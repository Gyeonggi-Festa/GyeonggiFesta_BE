package gyeonggi.gyeonggifesta.recommand.dto.response;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendHistoryRes {
	private LocalDate date;
	private List<EventRes> events;
}
