package gyeonggi.gyeonggifesta.event.controller.viewhistory;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import gyeonggi.gyeonggifesta.event.service.viewhistory.EventViewHistoryService;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user/event")
public class EventViewHistoryController {

    private final EventViewHistoryService eventViewHistoryService;

    /**
     * 내 최근 조회 목록 조회 (페이지네이션)
     * 예: GET /api/auth/user/event/views?page=0&size=20
     */
    @GetMapping("/views")
    public ResponseEntity<Response<Page<EventRes>>> getMyViews(Pageable pageable) {
        Page<EventRes> page = eventViewHistoryService.getMyRecentViews(pageable);
        return Response.ok(page).toResponseEntity();
    }
}
