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

		LocalDate register = eventBatchService.convertToLocalDate(item.getWritingDate());
		LocalDate start    = eventBatchService.convertToLocalDate(item.getBeginDate());
		LocalDate end      = eventBatchService.convertToLocalDate(item.getEndDate());

		// 키용 기관명: INST_NM 우선, 없으면 HOST_INST_NM
		String orgForKey = firstNonNull(item.getInstNm(), item.getHostInstNm());

		// 강화된 중복키: 제목 + 등록일 + 카테고리 + 기관명 + 종료일
		String key = String.join("_",
				n(item.getTitle()),
				n(register),
				n(item.getCategoryNm()),
				n(orgForKey),
				n(end)
		);

		// 청크 내 중복 방지
		if (processedKeys.contains(key)) {
			log.info("[SKIP:dup-in-chunk] {}", key);
			return null;
		}

		// DB 존재 체크(동일 기준)
		Optional<Event> existing = eventBatchService.findByTitleRegisterCategoryOrgEnd(
				item.getTitle(), register, item.getCategoryNm(), orgForKey, end
		);
		if (existing.isPresent()) {
			log.info("[SKIP:exists-in-db] {}", key);
			return null;
		}

		// 상태 계산
		Status status;
		if (start != null && start.isAfter(LocalDate.now()))      status = Status.NOT_STARTED;
		else if (end != null && end.isBefore(LocalDate.now()))    status = Status.END;
		else                                                      status = Status.PROGRESS;

		String isFree = (item.getFeeInfo() != null && item.getFeeInfo().contains("무료")) ? "Y" : "N";

		// 저장/표시용 기관명: HOST_INST_NM 우선, 없으면 INST_NM
		String orgForPersist = firstNonNull(item.getHostInstNm(), item.getInstNm());

		Event e = Event.builder()
				.status(status)
				.codename(item.getCategoryNm())
				.title(item.getTitle())
				.orgName(orgForPersist)
				.useFee(item.getFeeInfo())
				.timeInfo(item.getTimeInfo())
				.registerDate(register)
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

	private static String firstNonNull(String a, String b) {
		return (a != null && !a.isBlank()) ? a : (b != null ? b : null);
	}
	private static String n(Object s) { return s == null ? "" : String.valueOf(s); }
}
