package gyeonggi.gyeonggifesta.event.service.favorite;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.entity.EventFavorite;
import gyeonggi.gyeonggifesta.event.exception.EventErrorCode;
import gyeonggi.gyeonggifesta.event.repository.EventFavoriteRepository;
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
public class EventFavoriteServiceImpl implements EventFavoriteService{

	private final SecurityUtil securityUtil;
	private final EventFavoriteRepository eventFavoriteRepository;
	private final EventRepository eventRepository;

	@Override
	@Transactional
	public void createEventFavorite(Long eventId) {
		Member currentMember = securityUtil.getCurrentMember();

		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new BusinessException(EventErrorCode.NOT_EXIST_EVENT));

		if (eventFavoriteRepository.existsByEventAndMember(event, currentMember)) {
			throw new BusinessException(EventErrorCode.ALREADY_FAVORITE);
		}

		EventFavorite eventFavorite = EventFavorite.builder()
				.member(currentMember)
				.event(event)
				.build();

		currentMember.addEventFavorite(eventFavorite);
		event.addEventFavorite(eventFavorite);
		eventFavoriteRepository.save(eventFavorite);
	}

	@Override
	@Transactional
	public void removeEventFavorite(Long eventId) {
		Member currentMember = securityUtil.getCurrentMember();

		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new BusinessException(EventErrorCode.NOT_EXIST_EVENT));

		EventFavorite eventFavorite = eventFavoriteRepository.findByEventAndMember(event, currentMember)
				.orElseThrow(() -> new BusinessException(EventErrorCode.NOT_EXIST_FAVORITE));

		currentMember.removeEventFavorite(eventFavorite);
		event.removeEventFavorite(eventFavorite);
		eventFavoriteRepository.delete(eventFavorite);
	}

	@Override
	public Page<EventRes> getFavoriteEvents(Pageable pageable) {
		Member currentMember = securityUtil.getCurrentMember();
		Page<EventFavorite> favoritePage = eventFavoriteRepository.findByMember(currentMember, pageable);

		return favoritePage.map(fav -> {
			Event event = fav.getEvent();
			return EventRes.builder()
					.eventId(event.getId())
					.title(event.getTitle())
					.category(event.getCodename())
					.isFree(event.getIsFree())
					.status(event.getStatus().name())
					.startDate(event.getStartDate())
					.endDate(event.getEndDate())
					.mainImg(event.getMainImg())
					.likes(event.getLikes())
					.favorites(event.getFavorites())
					.comments(event.getComments())
					.rating(event.getRating())
					.ratingCount(event.getEventReviews().size())
					.build();
		});
	}

	public EventFavorite getEventFavoriteByEvent(Event event) {
		return eventFavoriteRepository.findByEventAndMember(event,
				securityUtil.getCurrentMember())
			.orElseThrow(() -> new BusinessException(EventErrorCode.NOT_EXIST_FAVORITE));
	}
}
