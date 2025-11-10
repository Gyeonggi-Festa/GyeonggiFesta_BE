package gyeonggi.gyeonggifesta.event.service.like;

import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.entity.EventLike;
import gyeonggi.gyeonggifesta.event.exception.EventErrorCode;
import gyeonggi.gyeonggifesta.event.repository.EventLikeRepository;
import gyeonggi.gyeonggifesta.event.repository.EventRepository;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventLikeServiceImpl implements EventLikeService {

	private final SecurityUtil securityUtil;
	private final EventLikeRepository eventLikeRepository;
	private final EventRepository eventRepository;

	/**
	 * 문화행사 좋아요 생성
	 *
	 * @param eventId 문화행사 ID
	 */
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

	/**
	 * 문화행사 좋아요 제거
	 *
	 * @param eventId 문화행사 ID
	 */
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
}
