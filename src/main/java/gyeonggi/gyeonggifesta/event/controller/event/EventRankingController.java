package gyeonggi.gyeonggifesta.event.controller.event;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import gyeonggi.gyeonggifesta.event.service.ranking.EventRankingService;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user/event")
public class EventRankingController {

    private final EventRankingService eventRankingService;

    /**
     * 최근 7일 좋아요순 상위 N개 (기본 5)
     * 예: GET /api/auth/user/event/top-liked-week?limit=5
     */
    @GetMapping("/top-liked-week")
    public ResponseEntity<Response<List<EventRes>>> getTopLikedWeek(
            @RequestParam(name = "limit", defaultValue = "5") int limit
    ) {
        List<EventRes> data = eventRankingService.getTopLikedEventsInDays(7, limit);
        return Response.ok(data).toResponseEntity();
    }
}
