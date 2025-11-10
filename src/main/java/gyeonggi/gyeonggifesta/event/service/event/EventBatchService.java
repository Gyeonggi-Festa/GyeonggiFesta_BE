package gyeonggi.gyeonggifesta.event.service.event;

import gyeonggi.gyeonggifesta.event.dto.batch.response.OpenApiEventListRes.GyeonggiEventRow;
import gyeonggi.gyeonggifesta.event.entity.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventBatchService {

	List<GyeonggiEventRow> getAllEventRowsFromApi();

	Optional<Event> findByTitleAndRegisterDateAndCodename(String title, LocalDate registerDate, String codename);

	Optional<Event> findByTitleRegisterCategoryOrgEnd(
			String title, LocalDate registerDate, String codename, String orgName, LocalDate endDate
	);

	Event saveEvent(Event event);

	int updateMissingEvents(Set<String> apiEventKeys);

	LocalDate convertToLocalDate(String dateStr);
}
