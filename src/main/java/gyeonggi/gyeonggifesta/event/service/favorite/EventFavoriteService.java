package gyeonggi.gyeonggifesta.event.service.favorite;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.entity.EventFavorite;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventFavoriteService {

	/**
	 * 문화행사 즐겨찾기 생성
	 *
	 * @param eventId 문화행사 ID
	 */
	void createEventFavorite(Long eventId);

	/**
	 * 문화행사 즐겨찾기 제거
	 *
	 * @param eventId 문화행사 ID
	 */
	void removeEventFavorite(Long eventId);


	/**
	 * 문화행사 즐겨찾기 조회
	 */
	Page<EventRes> getFavoriteEvents(Pageable pageable);

	EventFavorite getEventFavoriteByEvent(Event event);
}
