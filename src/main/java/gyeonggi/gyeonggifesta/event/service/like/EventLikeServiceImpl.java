package gyeonggi.gyeonggifesta.event.service.like;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.entity.EventFavorite;
import gyeonggi.gyeonggifesta.event.entity.EventLike;
import gyeonggi.gyeonggifesta.event.exception.EventErrorCode;
import gyeonggi.gyeonggifesta.event.repository.EventLikeRepository;
import gyeonggi.gyeonggifesta.event.repository.EventRepository;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventLikeServiceImpl implements EventLikeService {

	private final SecurityUtil securityUtil;
	private final EventLikeRepository eventLikeRepository;
	private final EventRepository eventRepository;

	/** 문화행사 좋아요 생성 */
	@Override
	@Transactional
	public void createEventLike(Long eventId) {
		Member currentMember = securityUtil.getCurrentMember();

		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new BusinessException(EventErrorCode.NOT_EXIST_EVENT));

		if (eventLikeRepository.existsByEventAndMember(event, currentMember)) {
			throw new BusinessException(EventErrorCode.ALREADY_LIKED);
		}

		EventLike eventLike = EventLike.builder()
				.member(currentMember)
				.event(event)
				.build();

		currentMember.addEventLike(eventLike);
		event.addEventLike(eventLike);
		eventLikeRepository.save(eventLike);
	}

	/** 문화행사 좋아요 제거 */
	@Override
	@Transactional
	public void removeEventLike(Long eventId) {
		Member currentMember = securityUtil.getCurrentMember();

		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new BusinessException(EventErrorCode.NOT_EXIST_EVENT));

		EventLike eventLike = eventLikeRepository.findByEventAndMember(event, currentMember)
				.orElseThrow(() -> new BusinessException(EventErrorCode.NOT_EXIST_LIKE));

		currentMember.removeEventLike(eventLike);
		event.removeEventLike(eventLike);
		eventLikeRepository.delete(eventLike);
	}

	/** 내가 좋아요한 행사 목록 조회 (페이지네이션) */
	@Override
	@Transactional(readOnly = true)
	public Page<EventRes> getLikedEvents(Pageable pageable) {
		Member currentMember = securityUtil.getCurrentMember();

		Page<EventLike> likePage = eventLikeRepository.findByMember(currentMember, pageable);

		return likePage.map(like -> {
			Event event = like.getEvent();
			String status = (event.getStatus() != null) ? event.getStatus().name() : null;
			int ratingCount = (event.getEventReviews() != null) ? event.getEventReviews().size() : 0;

			return EventRes.builder()
					.eventId(event.getId())
					.title(event.getTitle())
					.category(event.getCodename())
					.isFree(event.getIsFree())
					.status(status)               // null-safe
					.startDate(event.getStartDate())
					.endDate(event.getEndDate())
					.mainImg(event.getMainImg())
					.likes(event.getLikes())
					.favorites(event.getFavorites())
					.comments(event.getComments())
					.rating(event.getRating())
					.ratingCount(ratingCount)     // null-safe
					.build();
		});
	}

	public EventLike getEventLikeByEvent(Event event) {
		return eventLikeRepository.findByEventAndMember(event,
			securityUtil.getCurrentMember()).orElse(null);
	}
}
