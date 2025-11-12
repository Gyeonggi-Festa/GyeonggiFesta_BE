package gyeonggi.gyeonggifesta.event.service.ranking;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;

import java.util.List;

public interface EventRankingService {
    /**
     * 최근 N일 동안 생성된 좋아요 수 기준 상위 limit개 이벤트를 반환
     * @param days  집계 기간(일)
     * @param limit 결과 개수(기본 5)
     */
    List<EventRes> getTopLikedEventsInDays(int days, int limit);
}
