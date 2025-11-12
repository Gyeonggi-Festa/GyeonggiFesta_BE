package gyeonggi.gyeonggifesta.event.service.viewhistory;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventViewHistoryService {

    void createView(Long eventId);                 // 상세 조회 시 기록
    Page<EventRes> getMyRecentViews(Pageable pageable); // 내 최근 조회 목록 조회
}
