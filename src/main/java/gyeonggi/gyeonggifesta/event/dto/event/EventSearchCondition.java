package gyeonggi.gyeonggifesta.event.dto.event;

import gyeonggi.gyeonggifesta.event.enums.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class EventSearchCondition {

	private final Status status;
	private final String isFree;
	private final String codename;
	private final String title;
	private LocalDate startDate; // 검색 시작 날짜
	private LocalDate endDate;

	@Builder
	public EventSearchCondition(Status status, String isFree, String codename, String title,
								LocalDate startDate, LocalDate endDate) {
		this.status = status;
		this.isFree = isFree;
		this.codename = codename;
		this.title = title;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public boolean hasTitleKeyword() {
		return title != null && !title.trim().isEmpty();
	}

	public boolean hasDateCondition() {
		return startDate != null || endDate != null;
	}
}
