package gyeonggi.gyeonggifesta.event.service.like;

public interface EventLikeService {

	/**
	 * 문화행사 좋아요 생성
	 *
	 * @param eventId 문화행사 ID
	 */
	void createEventLike(Long eventId);

	/**
	 * 문화행사 좋아요 제거
	 *
	 * @param eventId 문화행사 ID
	 */
	void removeEventLike(Long eventId);
}
