package gyeonggi.gyeonggifesta.event.service.event;

import com.fasterxml.jackson.databind.JsonNode;
import gyeonggi.gyeonggifesta.event.component.CultureEventApiClient;
import gyeonggi.gyeonggifesta.event.dto.batch.response.OpenApiEventListRes.GyeonggiEventRow;
import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.enums.Status;
import gyeonggi.gyeonggifesta.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 경기도 문화행사 수집/저장 배치 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventBatchServiceImpl implements EventBatchService {

	private final EventRepository eventRepository;

	// 프로젝트 내 다른 컴포넌트에서도 RestTemplate을 사용하므로 주입 유지
	private final RestTemplate restTemplate;

	// 경기도 문화행사 API 호출 전용 클라이언트 (주차장과 동일한 폴백 전략 적용)
	private final CultureEventApiClient cultureEventApiClient;

	// 테스트 코드(ReflectionTestUtils)나 환경설정에서 사용할 수 있으니 보관
	@Value("${open-api.event.key}")
	private String API_KEY;

	/** 경기도 문화행사 전체 Row 조회 (폴백 포함) */
	@Override
	public List<GyeonggiEventRow> getAllEventRowsFromApi() {
		return fetchAllRowsFromApi();
	}

	/** 중복체크 */
	@Override
	public Optional<Event> findByTitleAndRegisterDateAndCodename(String title, LocalDate registerDate, String codename) {
		return eventRepository.findByTitleAndRegisterDateAndCodename(title, registerDate, codename);
	}

	/** 신규 Event 저장 */
	@Override
	@Transactional
	public Event saveEvent(Event event) {
		return eventRepository.save(event);
	}

	/** 종료된 이벤트 상태 갱신 (END) */
	@Override
	@Transactional
	public int updateMissingEvents(Set<String> apiEventKeys) {
		List<Event> existingEvents = eventRepository.findAllByStatus(Status.PROGRESS);
		int updatedCount = 0;
		LocalDate today = LocalDate.now();

		for (Event event : existingEvents) {
			if (event.getEndDate() != null && event.getEndDate().isBefore(today)) {
				event.setStatus(Status.END);
				eventRepository.save(event);
				updatedCount++;
			}
		}
		return updatedCount;
	}

	/** 문자열 → LocalDate 변환 (yyyy-MM-dd / yyyyMMdd 지원) */
	@Override
	public LocalDate convertToLocalDate(String dateStr) {
		if (dateStr == null || dateStr.isEmpty()) return null;

		String s = dateStr.trim();
		DateTimeFormatter dash = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter compact = DateTimeFormatter.ofPattern("yyyyMMdd");

		try {
			if (s.length() == 10 && s.charAt(4) == '-' && s.charAt(7) == '-') {
				return LocalDate.parse(s, dash);
			}
			if (s.length() == 8) {
				return LocalDate.parse(s, compact);
			}
			// 예외 케이스는 일단 yyyy-MM-dd 시도
			return LocalDate.parse(s, dash);
		} catch (Exception e) {
			log.warn("날짜 변환 실패: {}", dateStr);
			return null;
		}
	}

	/* ===================== 내부 구현 ===================== */

	/** 경기도 문화행사 API 호출 → JsonNode row → GyeonggiEventRow 매핑 */
	private List<GyeonggiEventRow> fetchAllRowsFromApi() {
		List<JsonNode> rawRows = cultureEventApiClient.fetchAllRows();
		List<GyeonggiEventRow> mapped = new ArrayList<>(rawRows.size());

		for (JsonNode n : rawRows) {
			GyeonggiEventRow row = new GyeonggiEventRow();
			row.setInstNm(text(n, "INST_NM"));
			row.setTitle(text(n, "TITLE"));
			row.setCategoryNm(text(n, "CATEGORY_NM"));
			row.setUrl(text(n, "URL"));
			row.setImageUrl(text(n, "IMAGE_URL"));
			row.setBeginDate(text(n, "BEGIN_DE"));
			row.setEndDate(text(n, "END_DE"));
			row.setTimeInfo(text(n, "EVENT_TM_INFO"));
			row.setFeeInfo(text(n, "PARTCPT_EXPN_INFO"));
			row.setTelInfo(text(n, "TELNO_INFO"));
			row.setHostInstNm(text(n, "HOST_INST_NM"));
			row.setHomepageUrl(text(n, "HMPG_URL"));
			row.setWritingDate(text(n, "WRITNG_DE"));

			mapped.add(row);
		}
		log.info("총 수집 건수: {}", mapped.size());
		return mapped;
	}

	private static String text(JsonNode n, String field) {
		JsonNode v = n.get(field);
		return (v == null || v.isNull()) ? null : v.asText(null);
	}
}
