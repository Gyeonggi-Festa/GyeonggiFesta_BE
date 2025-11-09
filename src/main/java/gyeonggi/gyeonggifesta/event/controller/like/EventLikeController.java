package gyeonggi.gyeonggifesta.event.controller.like;

import gyeonggi.gyeonggifesta.event.service.like.EventLikeService;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user")
public class EventLikeController {

	private final EventLikeService eventLikeService;

	/**
	 * 문화행사 좋아요 생성
	 *
	 * @param eventId 문화행사 ID
	 */
	@PostMapping("/event/like/{eventId}")
	public ResponseEntity<Response<Void>> createEventLike(@PathVariable Long eventId) {
		eventLikeService.createEventLike(eventId);

		return Response.ok().toResponseEntity();
	}

	/**
	 * 문화행사 좋아요 제거
	 *
	 * @param eventId 문화행사 ID
	 */
	@DeleteMapping("/event/like/{eventId}")
	public ResponseEntity<Response<Void>> removeEventLike(@PathVariable Long eventId) {
		eventLikeService.removeEventLike(eventId);

		return Response.ok().toResponseEntity();
	}
}
