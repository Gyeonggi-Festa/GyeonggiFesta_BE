package gyeonggi.gyeonggifesta.event.batch.processor;

import gyeonggi.gyeonggifesta.event.dto.batch.response.OpenApiEventListRes.GyeonggiEventRow;
import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.enums.Status;
import gyeonggi.gyeonggifesta.event.repository.EventRepository;
import gyeonggi.gyeonggifesta.event.service.event.EventBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static gyeonggi.gyeonggifesta.event.util.TextNormalizer.norm;
import static gyeonggi.gyeonggifesta.event.util.TextNormalizer.clampUrl;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class ApiEventItemProcessor implements ItemProcessor<GyeonggiEventRow, Event> {

	private final EventBatchService eventBatchService;
	private final EventRepository eventRepository;

	private final Set<String> processedKeys = new HashSet<>();

	@Override
	public Event process(GyeonggiEventRow item) {
		if (item == null) return null;

		LocalDate register = eventBatchService.convertToLocalDate(item.getWritingDate());
		LocalDate start    = eventBatchService.convertToLocalDate(item.getBeginDate());
		LocalDate end      = eventBatchService.convertToLocalDate(item.getEndDate());

		// HOST 우선(저장/존재확인/키 모두 동일 기준)
		String orgSelected = firstNonNull(item.getHostInstNm(), item.getInstNm());

		// 정규화
		String nTitle = norm(item.getTitle());
		String nCat   = norm(item.getCategoryNm());
		String nOrg   = norm(orgSelected);
		String nReg   = register == null ? "" : register.toString();
		String nEnd   = end == null ? "" : end.toString();

		String key = String.join("|", nTitle, nReg, nCat, nOrg, nEnd);

		if (processedKeys.contains(key)) {
			log.info("[SKIP:dup-in-chunk] {}", key);
			return null;
		}

		boolean exists = eventRepository
				.findByTitleAndRegisterDateAndCodenameAndOrgNameAndEndDate(nTitle, register, nCat, nOrg, end)
				.isPresent();

		if (exists) {
			log.info("[SKIP:exists-in-db] {}", key);
			return null;
		}

		// 상태 계산
		Status status;
		if (start != null && start.isAfter(LocalDate.now()))      status = Status.NOT_STARTED;
		else if (end != null && end.isBefore(LocalDate.now()))    status = Status.END;
		else                                                      status = Status.PROGRESS;

		String isFree = (item.getFeeInfo() != null && item.getFeeInfo().contains("무료")) ? "Y" : "N";

		// URL 방어: 너무 긴 URL은 잘라서 저장
		String orgLink = clampUrl(item.getHomepageUrl() != null ? item.getHomepageUrl() : item.getUrl());
		String mainImg = clampUrl(item.getImageUrl());
		String portal  = clampUrl(item.getUrl());

		Event e = Event.builder()
				.status(status)
				.codename(nCat)
				.title(nTitle)
				.orgName(nOrg)
				.useFee(item.getFeeInfo())
				.timeInfo(item.getTimeInfo())
				.registerDate(register)
				.startDate(start)
				.endDate(end)
				.orgLink(orgLink)
				.mainImg(mainImg)
				.isFree(isFree)
				.portal(portal)
				.build();

		processedKeys.add(key);
		log.debug("[OK] {}", key);
		return e;
	}

	private static String firstNonNull(String a, String b) {
		return (a != null && !a.isBlank()) ? a : (b != null ? b : null);
	}
}
