package gyeonggi.gyeonggifesta.event.service.event;

import gyeonggi.gyeonggifesta.event.dto.event.EventSearchCondition;
import gyeonggi.gyeonggifesta.event.dto.event.response.EventDetailRes;
import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {

	/**
	 * 선택적 필터 조건과 제목 검색어, 페이징 정보를 받아 이벤트 목록을 조회
	 *
	 * @param condition 검색 조건을 담은 객체 (상태, 유료/무료, 카테고리, 구 이름, 제목 검색어)
	 * @param pageable 페이지 정보
	 * @return EventRes DTO로 매핑된 이벤트 정보를 담은 Page 객체
	 */
	Page<EventRes> getEvents(EventSearchCondition condition, Pageable pageable);

	EventDetailRes getEventDetail(Long eventId);
}
