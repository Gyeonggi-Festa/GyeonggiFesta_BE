package gyeonggi.gyeonggifesta.event.batch.processor;

import gyeonggi.gyeonggifesta.event.dto.batch.response.OpenApiEventListRes.GyeonggiEventRow;
import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.enums.Status;
import gyeonggi.gyeonggifesta.event.service.event.EventBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiEventItemProcessor implements ItemProcessor<GyeonggiEventRow, Event> {

	private final EventBatchService eventBatchService;
	private final Set<String> processedKeys = new HashSet<>();

	@Override
	public Event process(GyeonggiEventRow item) {
		if (item == null) return null;

		// 중복키: 제목 + WRITNG_DE + CATEGORY_NM
		String key = n(item.getTitle()) + "_" +
				n(eventBatchService.convertToLocalDate(item.getWritingDate())) + "_" +
				n(item.getCategoryNm());

		if (processedKeys.contains(key)) {
			log.debug("[SKIP:dup] {}", key);
			return null;
		}

		// DB 중복 체크
		Optional<Event> existing = eventBatchService.findByTitleAndRegisterDateAndCodename(
				item.getTitle(),
				eventBatchService.convertToLocalDate(item.getWritingDate()),
				item.getCategoryNm()
		);
		if (existing.isPresent()) {
			log.debug("[SKIP:exists] {}", key);
			return null;
		}

		LocalDate start = eventBatchService.convertToLocalDate(item.getBeginDate());
		LocalDate end   = eventBatchService.convertToLocalDate(item.getEndDate());

		Status status;
		if (start != null && start.isAfter(LocalDate.now()))      status = Status.NOT_STARTED;
		else if (end != null && end.isBefore(LocalDate.now()))    status = Status.END;
		else                                                      status = Status.PROGRESS;

		String isFree = (item.getFeeInfo() != null && item.getFeeInfo().contains("무료")) ? "Y" : "N";

		Event e = Event.builder()
				.status(status)
				.codename(item.getCategoryNm())
				.title(item.getTitle())
				.orgName(item.getHostInstNm())
				.useFee(item.getFeeInfo())
				.timeInfo(item.getTimeInfo())
				.registerDate(eventBatchService.convertToLocalDate(item.getWritingDate()))
				.startDate(start)
				.endDate(end)
				.orgLink(item.getHomepageUrl() != null ? item.getHomepageUrl() : item.getUrl())
				.mainImg(item.getImageUrl())
				.isFree(isFree)
				.portal(item.getUrl())
				.build();

		processedKeys.add(key);
		log.debug("[OK] {}", key);
		return e;
	}

	private static String n(Object s) { return s == null ? "" : String.valueOf(s); }
}
