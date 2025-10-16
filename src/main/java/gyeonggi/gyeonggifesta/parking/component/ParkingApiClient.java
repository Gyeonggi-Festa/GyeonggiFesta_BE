package gyeonggi.gyeonggifesta.parking.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.parking.exception.ParkingErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkingApiClient {

	private final RestTemplate restTemplate;
	private final ObjectMapper mapper = new ObjectMapper();

	@Value("${open-api.parking.base-url:https://openapi.gg.go.kr}")
	private String baseUrl;

	@Value("${open-api.parking.service:ParkingPlace}")
	private String serviceName;

	@Value("${open-api.parking.key}")
	private String apiKey;

	@Value("${open-api.parking.page-size:500}")
	private int defaultPageSize;

	// ---------- Public API ----------
	public List<JsonNode> fetchAllRows(String sigunNm) {
		// 1차 시도 (기본)
		try {
			return fetch(sigunNm, defaultPageSize, BuildMode.ENCODE_ALL, HeaderMode.MINIMAL);
		} catch (BusinessException e1) {
			log.warn("[Fallback#1] 기본 호출 실패: {} - {}", e1.getClass().getSimpleName(), e1.getMessage());
			// 2차 시도: 압축 비활성화
			try {
				return fetch(sigunNm, defaultPageSize, BuildMode.ENCODE_ALL, HeaderMode.NO_COMPRESSION);
			} catch (BusinessException e2) {
				log.warn("[Fallback#2] 압축 비활성화 실패: {} - {}", e2.getClass().getSimpleName(), e2.getMessage());
				// 3차 시도: pSize 축소 + 인코딩 방식 변경
				int small = Math.min(defaultPageSize, 50);
				return fetch(sigunNm, small, BuildMode.ENCODE_QUERY_ONLY, HeaderMode.MINIMAL);
			}
		}
	}

	// ---------- Internal ----------
	private enum BuildMode { ENCODE_ALL, ENCODE_QUERY_ONLY }
	private enum HeaderMode { MINIMAL, NO_COMPRESSION }

	private List<JsonNode> fetch(String sigunNm, int pageSize, BuildMode buildMode, HeaderMode headerMode) {
		HttpStatusCode lastStatus = null;
		String lastErrBody = null;

		try {
			int page = 1;
			int total = Integer.MAX_VALUE;
			List<JsonNode> rows = new ArrayList<>();

			while ((page - 1) * pageSize < total) {
				URI uri = (buildMode == BuildMode.ENCODE_ALL)
					? buildUriEncodeAll(sigunNm, page, pageSize)
					: buildUriEncodeQueryOnly(sigunNm, page, pageSize);

				HttpHeaders headers = new HttpHeaders();
				// 일부 게이트웨이는 Accept 헤더가 특정 값일 때만 정상 동작
				headers.set(HttpHeaders.ACCEPT, "*/*");
				headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0");
				if (headerMode == HeaderMode.NO_COMPRESSION) {
					// 압축 응답을 꺼서 RestTemplate/중간 프록시 이슈 회피
					headers.set(HttpHeaders.ACCEPT_ENCODING, "identity");
				}
				HttpEntity<Void> entity = new HttpEntity<>(headers);

				log.info("경기도 주차장 API 호출 URI = {}", uri);
				ResponseEntity<String> res =
					restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

				lastStatus = res.getStatusCode();
				String bodyStr = res.getBody();

				if (!lastStatus.is2xxSuccessful() || bodyStr == null) {
					log.error("HTTP 오류: status={}, body={}", lastStatus, bodyStr);
					throw new BusinessException(ParkingErrorCode.API_REQUEST_FAILED);
				}

				JsonNode body = mapper.readTree(bodyStr);
				ParseResult pr = parseAndCollect(body, rows);
				if (pr.errorCode != null) {
					// 예: ERROR-310 → 폴백 단계로 넘김
					log.error("API 오류 code={} msg={}", pr.errorCode, pr.errorMessage);
					throw new BusinessException(ParkingErrorCode.API_REQUEST_FAILED);
				}
				total = pr.total;

				if (pr.pageRowCount == 0) break;
				try {
					Thread.sleep(700); // 0.7초 대기 (서버 과부하 방지)
				} catch (InterruptedException ignored) {
				}
				page++;
			}

			log.info("API 수집 완료: totalRows={}", rows.size());
			return rows;

		} catch (HttpStatusCodeException httpEx) {
			lastStatus = httpEx.getStatusCode();
			lastErrBody = httpEx.getResponseBodyAsString();
			log.error("HTTP 오류 status={} body={}", lastStatus, lastErrBody);
			throw new BusinessException(ParkingErrorCode.API_REQUEST_FAILED);
		} catch (BusinessException be) {
			throw be;
		} catch (Exception ex) {
			log.error("주차장 API 기타 예외", ex);
			throw new BusinessException(ParkingErrorCode.API_REQUEST_FAILED);
		} finally {
			if (lastStatus != null) {
				log.warn("최종 응답 상태: {}", lastStatus);
			}
		}
	}

	private static class ParseResult {
		int total = Integer.MAX_VALUE;
		int pageRowCount = 0;
		String errorCode;
		String errorMessage;
	}

	private ParseResult parseAndCollect(JsonNode body, List<JsonNode> rows) {
		ParseResult r = new ParseResult();

		JsonNode arr = body.path("ParkingPlace");
		if (!arr.isArray() || arr.size() < 2) {
			// 일부 에러는 RESULT 루트로만 내려오기도 함
			JsonNode result = body.path("RESULT");
			if (result.isObject()) {
				r.errorCode = result.path("CODE").asText(null);
				r.errorMessage = result.path("MESSAGE").asText(null);
			}
			if (r.errorCode == null) {
				r.errorCode = "INVALID";
				r.errorMessage = "응답 형식 오류";
			}
			return r;
		}

		JsonNode head = arr.get(0).path("head");
		if (head.isArray()) {
			for (JsonNode h : head) {
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

		JsonNode rowNode = arr.get(1).path("row");
		if (rowNode.isArray() && rowNode.size() > 0) {
			rowNode.forEach(rows::add);
			r.pageRowCount = rowNode.size();
		}
		return r;
	}

	// 전체 encode
	private URI buildUriEncodeAll(String sigunNm, int page, int pageSize) {
		return UriComponentsBuilder
			.fromHttpUrl(baseUrl)
			.pathSegment(serviceName)
			.queryParam("KEY", apiKey)
			.queryParam("Type", "json")
			.queryParam("pIndex", page)
			.queryParam("pSize", pageSize)
			.queryParam("SIGUN_NM", sigunNm)
			.encode(StandardCharsets.UTF_8)
			.build()
			.toUri();
	}

	// queryParam 인코딩만 신뢰(전체 encode 미적용) + SIGUN_NM 수동 인코딩 비교
	private URI buildUriEncodeQueryOnly(String sigunNm, int page, int pageSize) {
		// 수동 인코딩 값
		String encSigun = URLEncoder.encode(sigunNm, StandardCharsets.UTF_8);
		return UriComponentsBuilder
			.fromHttpUrl(baseUrl + "/" + serviceName) // pathSegment + 전체 encode 생략
			.queryParam("KEY", apiKey)
			.queryParam("Type", "json")
			.queryParam("pIndex", page)
			.queryParam("pSize", pageSize)
			.queryParam("SIGUN_NM", encSigun)
			.build(false) // 전체 encode 비활성화
			.toUri();
	}
}
