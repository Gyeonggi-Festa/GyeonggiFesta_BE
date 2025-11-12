package gyeonggi.gyeonggifesta.event.service.ranking;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.repository.EventRepository;
import gyeonggi.gyeonggifesta.event.repository.EventLikeRepository;
import gyeonggi.gyeonggifesta.event.repository.projection.TopLikedEventView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventRankingServiceImpl implements EventRankingService {

    private final EventLikeRepository eventLikeRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EventRes> getTopLikedEventsInDays(int days, int limit) {
        // 파라미터 가드
        int safeDays  = days  <= 0 ? 7  : Math.min(days, 365);
        int safeLimit = limit <= 0 ? 5  : Math.min(limit, 50);

        LocalDateTime since = LocalDateTime.now().minusDays(safeDays).withHour(0).withMinute(0).withSecond(0).withNano(0);

        // 1) 최근 기간 좋아요 집계 (정렬/상한 보장)
        var page = eventLikeRepository.findTopLikedEventsSince(since, PageRequest.of(0, safeLimit));
        List<TopLikedEventView> tops = page.getContent();
        if (tops.isEmpty()) return Collections.emptyList();

        // 2) 이벤트 일괄 로드 (순서 보존 위해 LinkedHashMap 사용)
        List<Long> ids = tops.stream().map(TopLikedEventView::getEventId).toList();
        List<Event> events = eventRepository.findAllById(ids);

        Map<Long, Event> byId = events.stream().collect(Collectors.toMap(
                Event::getId, e -> e, (a,b)->a, LinkedHashMap::new));

        // 3) 집계 순서대로 DTO 매핑 (status/ratingCount null-safe)
        List<EventRes> result = new ArrayList<>();
        for (TopLikedEventView v : tops) {
            Event e = byId.get(v.getEventId());
            if (e == null) continue; // 혹시 모를 동시성/삭제 케이스 방어

            String status = (e.getStatus() != null) ? e.getStatus().name() : null;
            int ratingCount = (e.getEventReviews() != null) ? e.getEventReviews().size() : 0;

            result.add(EventRes.builder()
                    .eventId(e.getId())
                    .title(e.getTitle())
                    .category(e.getCodename())
                    .isFree(e.getIsFree())
                    .status(status)
                    .startDate(e.getStartDate())
                    .endDate(e.getEndDate())
                    .mainImg(e.getMainImg())
                    .likes(e.getLikes())
                    .favorites(e.getFavorites())
                    .comments(e.getComments())
                    .rating(e.getRating())
                    .ratingCount(ratingCount)
                    .build());
        }
        return result;
    }
}
