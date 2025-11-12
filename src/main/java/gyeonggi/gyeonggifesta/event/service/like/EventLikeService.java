package gyeonggi.gyeonggifesta.event.service.like;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventLikeService {

	/** 문화행사 좋아요 생성 */
	void createEventLike(Long eventId);

	/** 문화행사 좋아요 제거 */
	void removeEventLike(Long eventId);

	/** 내가 좋아요한 행사 목록 조회 (페이지네이션) */
	Page<EventRes> getLikedEvents(Pageable pageable);
}
