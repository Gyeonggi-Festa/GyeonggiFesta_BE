package gyeonggi.gyeonggifesta.recommand.service;

import gyeonggi.gyeonggifesta.recommand.dto.request.AiRecommendReq;
import gyeonggi.gyeonggifesta.recommand.dto.response.AiRecommendRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MlServerClient {

	private final RestTemplate restTemplate;

	@Value("${request-url.ml-server}")
	private String mlServerUrl;

	/**
	 * 여러 사용자의 AI 추천 요청을 한 번에 처리
	 *
	 * @param requests AI 추천 요청 DTO 리스트
	 * @return AI 추천 응답 DTO 리스트
	 */
	public List<AiRecommendRes> getRecommendations(List<AiRecommendReq> requests) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<List<AiRecommendReq>> entity = new HttpEntity<>(requests, headers);

		// 요청을 리스트로 보내고, 응답도 리스트로 받음
		ResponseEntity<List<AiRecommendRes>> response = restTemplate.exchange(
			mlServerUrl + "/recommend",
			HttpMethod.POST,
			entity,
			new ParameterizedTypeReference<List<AiRecommendRes>>() {}
		);

		List<AiRecommendRes> responseList = response.getBody();

		// 응답이 비어있는 경우 예외 처리
		if (responseList == null || responseList.isEmpty()) {
			throw new RuntimeException("ML 서버로부터 응답을 받지 못했습니다.");
		}

		log.info("AI 추천 API 응답 수신 완료: {} 건", responseList.size());
		return responseList;
	}
}