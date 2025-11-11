package gyeonggi.gyeonggifesta.recommand.controller;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import gyeonggi.gyeonggifesta.recommand.dto.response.RecommendHistoryRes;
import gyeonggi.gyeonggifesta.recommand.service.AiRecommendService;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user")
public class AiRecommendationController {

	private final AiRecommendService aiRecommendService;

	@GetMapping("/event/recommend")
	public ResponseEntity<Response<List<EventRes>>> getRecommendEvents() {
		List<EventRes> result = aiRecommendService.getRecommendEvents();
		return Response.ok(result).toResponseEntity();
	}

	@GetMapping("/event/recommend/history")
	public ResponseEntity<Response<List<RecommendHistoryRes>>> getRecommendEventHistory() {
		List<RecommendHistoryRes> result = aiRecommendService.getRecommendEventHistory();
		return Response.ok(result).toResponseEntity();
	}

}
