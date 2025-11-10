package gyeonggi.gyeonggifesta.event.batch.reader;

import com.fasterxml.jackson.databind.JsonNode;
import gyeonggi.gyeonggifesta.event.component.CultureEventApiClient;
import gyeonggi.gyeonggifesta.event.dto.batch.response.OpenApiEventListRes.GyeonggiEventRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class OpenApiEventItemReader implements ItemReader<GyeonggiEventRow> {

	private final CultureEventApiClient cultureEventApiClient;
	private Iterator<GyeonggiEventRow> iterator;

	@Override
	public GyeonggiEventRow read() {
		// 처음 read() 호출 시에만 데이터 수집
		if (iterator == null) {
			List<JsonNode> rawRows = cultureEventApiClient.fetchAllRows();
			List<GyeonggiEventRow> rows = mapToRows(rawRows);
			this.iterator = rows.iterator();
			log.info("EventItemReader initialized with total rows = {}", rows.size());
		}

		// Iterator 기반으로 끝까지 반환
		return (iterator != null && iterator.hasNext()) ? iterator.next() : null;
	}

	private List<GyeonggiEventRow> mapToRows(List<JsonNode> raw) {
		List<GyeonggiEventRow> mapped = new ArrayList<>(raw.size());
		for (JsonNode n : raw) {
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
		return mapped;
	}

	private static String text(JsonNode n, String field) {
		JsonNode v = n.get(field);
		return (v == null || v.isNull()) ? null : v.asText(null);
	}
}
