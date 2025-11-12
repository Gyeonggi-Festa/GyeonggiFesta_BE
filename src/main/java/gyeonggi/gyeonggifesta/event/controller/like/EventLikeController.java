package gyeonggi.gyeonggifesta.event.controller.like;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import gyeonggi.gyeonggifesta.event.service.like.EventLikeService;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user")
public class EventLikeController {

	private final EventLikeService eventLikeService;

	/** 문화행사 좋아요 생성 */
	@PostMapping("/event/like/{eventId}")
	public ResponseEntity<Response<Void>> createEventLike(@PathVariable Long eventId) {
		eventLikeService.createEventLike(eventId);
		return Response.ok().toResponseEntity();
	}

	/** 문화행사 좋아요 제거 */
	@DeleteMapping("/event/like/{eventId}")
	public ResponseEntity<Response<Void>> removeEventLike(@PathVariable Long eventId) {
		eventLikeService.removeEventLike(eventId);
		return Response.ok().toResponseEntity();
	}

	/** 내가 좋아요한 행사 목록 조회 (페이지네이션)
	 * 예: GET /api/auth/user/event/likes?page=0&size=20
	 */
	@GetMapping("/event/likes")
	public ResponseEntity<Response<Page<EventRes>>> getMyLikedEvents(Pageable pageable) {
		Page<EventRes> page = eventLikeService.getLikedEvents(pageable);
		return Response.ok(page).toResponseEntity();
	}
}
