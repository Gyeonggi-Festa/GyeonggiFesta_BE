package gyeonggi.gyeonggifesta.event.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CultureEventApiClient {

	private final RestTemplate restTemplate;
	private final ObjectMapper mapper = new ObjectMapper();

	@Value("${open-api.event.base-url:https://openapi.gg.go.kr}")
	private String baseUrl;

	@Value("${open-api.event.service:GGCULTUREVENTSTUS}")
	private String serviceName;

	@Value("${open-api.event.key}")
	private String apiKey;

	@Value("${open-api.event.page-size:500}")
	private int defaultPageSize;

	/**
	 * 전체 문화행사 row 수집 (폴백 포함)
	 */
	public List<JsonNode> fetchAllRows() {
		try {
			return fetch(defaultPageSize, HeaderMode.MINIMAL);
		} catch (RuntimeException e1) {
			log.warn("[CultureEvent Fallback#1] 기본 호출 실패: {}", e1.getMessage());
			// 압축 비활성화
			try {
				return fetch(defaultPageSize, HeaderMode.NO_COMPRESSION);
			} catch (RuntimeException e2) {
				log.warn("[CultureEvent Fallback#2] 압축 비활성화 실패: {}", e2.getMessage());
				// pSize 축소
				int small = Math.min(defaultPageSize, 50);
				return fetch(small, HeaderMode.MINIMAL);
			}
		}
	}

	private enum HeaderMode { MINIMAL, NO_COMPRESSION }

	private List<JsonNode> fetch(int pageSize, HeaderMode headerMode) {
		HttpStatusCode lastStatus = null;
		String lastErrBody = null;

		try {
			int page = 1;
			int total = Integer.MAX_VALUE;
			List<JsonNode> rows = new ArrayList<>();

			while ((page - 1) * pageSize < total) {
				URI uri = buildUri(page, pageSize);
				HttpHeaders headers = new HttpHeaders();
				headers.set(HttpHeaders.ACCEPT, "*/*");
				headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0");
				if (headerMode == HeaderMode.NO_COMPRESSION) {
					headers.set(HttpHeaders.ACCEPT_ENCODING, "identity");
				}
				HttpEntity<Void> entity = new HttpEntity<>(headers);

				log.info("경기도 문화행사 API 호출 URI = {}", uri);
				ResponseEntity<String> res = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
				lastStatus = res.getStatusCode();
				String bodyStr = res.getBody();

				if (!lastStatus.is2xxSuccessful() || bodyStr == null) {
					log.error("HTTP 오류: status={}, body={}", lastStatus, bodyStr);
					throw new RuntimeException("API_REQUEST_FAILED");
				}

				JsonNode body = mapper.readTree(bodyStr);
				ParseResult pr = parseAndCollect(body, rows);
				if (pr.errorCode != null) {
					// 예: ERROR-310
					throw new RuntimeException("API_ERROR " + pr.errorCode + " - " + pr.errorMessage);
				}

				total = pr.total;
				if (pr.pageRowCount == 0) break;

				try { Thread.sleep(600); } catch (InterruptedException ignored) {}
				page++;
			}

			log.info("문화행사 API 수집 완료: totalRows={}", rows.size());
			return rows;

		} catch (HttpStatusCodeException httpEx) {
			lastStatus = httpEx.getStatusCode();
			lastErrBody = httpEx.getResponseBodyAsString();
			log.error("HTTP 오류 status={} body={}", lastStatus, lastErrBody);
			throw new RuntimeException("API_REQUEST_FAILED", httpEx);
		} catch (RuntimeException re) {
			log.error("문화행사 API 런타임 오류: {}", re.getMessage());
			throw re;
		} catch (Exception ex) {
			log.error("문화행사 API 기타 예외", ex);
			throw new RuntimeException("API_REQUEST_FAILED", ex);
		} finally {
			if (lastStatus != null) {
				log.warn("문화행사 최종 응답 상태: {}", lastStatus);
			}
		}
	}

	private static class ParseResult {
		int total = Integer.MAX_VALUE;
		int pageRowCount = 0;
		String errorCode;
		String errorMessage;
	}

	/**
	 * 경기도 표준: {SERVICE_NAME: [ { head: [...] }, { row: [...] } ]}
	 * 또는 에러: {"RESULT":{"CODE":"ERROR-310","MESSAGE":"..."}} 형태
	 */
	private ParseResult parseAndCollect(JsonNode body, List<JsonNode> rows) {
		ParseResult r = new ParseResult();

		// 1) 정상 루트
		JsonNode arr = body.path(serviceName);
		if (arr.isArray() && arr.size() >= 2) {
			// head
			JsonNode headArr = arr.get(0).path("head");
			if (headArr.isArray()) {
				for (JsonNode h : headArr) {
					String code = h.path("CODE").asText("");
					String msg  = h.path("MESSAGE").asText("");
					if (code.startsWith("ERROR")) {
						r.errorCode = code;
						r.errorMessage = msg;
						return r;
					}
					if (h.has("list_total_count")) {
						r.total = h.get("list_total_count").asInt(Integer.MAX_VALUE);
					}
				}
			}
			// row
			JsonNode rowNode = arr.get(1).path("row");
			if (rowNode.isArray() && rowNode.size() > 0) {
				rowNode.forEach(rows::add);
				r.pageRowCount = rowNode.size();
			}
			return r;
		}

		// 2) 에러 루트
		JsonNode result = body.path("RESULT");
		if (result.isObject()) {
			r.errorCode = result.path("CODE").asText(null);
			r.errorMessage = result.path("MESSAGE").asText(null);
			return r;
		}

		// 3) 알 수 없는 응답
		r.errorCode = "INVALID";
		r.errorMessage = "응답 형식 오류";
		return r;
	}

	private URI buildUri(int page, int pageSize) {
		return UriComponentsBuilder
			.fromHttpUrl(baseUrl)
			.pathSegment(serviceName)
			.queryParam("KEY", apiKey)
			.queryParam("Type", "json")
			.queryParam("pIndex", page)
			.queryParam("pSize", pageSize)
			.encode(StandardCharsets.UTF_8)
			.build()
			.toUri();
	}
}
