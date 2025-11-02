package gyeonggi.gyeonggifesta.event.controller.favorite;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import gyeonggi.gyeonggifesta.event.service.favorite.EventFavoriteService;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user")
public class EventFavoriteController {

	private final EventFavoriteService eventFavoriteService;

	/**
	 * 문화행사 즐겨찾기 생성
	 *
	 * @param eventId 문화행사 ID
	 */
	@PostMapping("/event/favorite/{eventId}")
	public ResponseEntity<Response<Void>> createEventFavorite(@PathVariable Long eventId) {
		eventFavoriteService.createEventFavorite(eventId);

		return Response.ok().toResponseEntity();
	}

	/**
	 * 문화행사 즐겨찾기 제거
	 *
	 * @param eventId 문화행사 ID
	 */
	@DeleteMapping("/event/favorite/{eventId}")
	public ResponseEntity<Response<Void>> removeEventFavorite(@PathVariable Long eventId) {
		eventFavoriteService.removeEventFavorite(eventId);

		return Response.ok().toResponseEntity();
	}

	/**
	 * 문화행사 즐겨찾기 조회
	 */
	@GetMapping("/event/favorite")
	public ResponseEntity<Response<Page<EventRes>>> getFavoriteEvents(
		@RequestParam(name = "page", defaultValue = "1") int page,
		@RequestParam(name = "size", defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

		Page<EventRes> events = eventFavoriteService.getFavoriteEvents(pageable);
		return Response.ok(events).toResponseEntity();
	}

}
