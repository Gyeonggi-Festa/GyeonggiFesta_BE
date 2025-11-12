package gyeonggi.gyeonggifesta.event.service.ranking;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.repository.EventLikeRepository;
import gyeonggi.gyeonggifesta.event.repository.EventRepository;
import gyeonggi.gyeonggifesta.event.repository.projection.TopLikedEventView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventRankingServiceImpl implements EventRankingService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final EventLikeRepository eventLikeRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EventRes> getTopLikedEventsInDays(int days, int limit) {
        int safeDays  = days  <= 0 ? 7  : Math.min(days, 365);
        int safeLimit = limit <= 0 ? 5  : Math.min(limit, 50);

        // KST 자정 기준으로 최근 N일 경계 고정
        LocalDate sinceDate = LocalDate.now(KST).minusDays(safeDays);
        LocalDateTime since = sinceDate.atStartOfDay();

        // 카운트쿼리 없는 집계 + 상한 적용
        List<TopLikedEventView> tops =
                eventLikeRepository.findTopLikedEventsSince(since, PageRequest.of(0, safeLimit));

        if (tops.isEmpty()) return Collections.emptyList();

        // 이벤트 일괄 로드
        List<Long> ids = tops.stream().map(TopLikedEventView::getEventId).toList();
        List<Event> events = eventRepository.findAllById(ids);

        Map<Long, Event> byId = events.stream().collect(Collectors.toMap(
                Event::getId, e -> e, (a, b) -> a, LinkedHashMap::new));

        // 집계 순서대로 DTO 변환 (널/동시삭제 방어)
        List<EventRes> result = new ArrayList<>();
        for (TopLikedEventView v : tops) {
            Event e = byId.get(v.getEventId());
            if (e == null) continue;

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
