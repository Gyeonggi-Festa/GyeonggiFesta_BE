package gyeonggi.gyeonggifesta.event.service.event;

import gyeonggi.gyeonggifesta.event.dto.event.EventSearchCondition;
import gyeonggi.gyeonggifesta.event.dto.event.response.EventDetailRes;
import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.entity.EventFavorite;
import gyeonggi.gyeonggifesta.event.entity.EventLike;
import gyeonggi.gyeonggifesta.event.entity.EventSearchHistory;
import gyeonggi.gyeonggifesta.event.exception.EventErrorCode;
import gyeonggi.gyeonggifesta.event.repository.EventRepository;
import gyeonggi.gyeonggifesta.event.repository.EventSearchHistoryRepository;
import gyeonggi.gyeonggifesta.event.repository.EventSpecifications;
import gyeonggi.gyeonggifesta.event.service.favorite.EventFavoriteService;
import gyeonggi.gyeonggifesta.event.service.like.EventLikeService;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

	private final SecurityUtil securityUtil;
	private final EventRepository eventRepository;
	private final EventSearchHistoryRepository eventSearchHistoryRepository;
	private final EventFavoriteService eventFavoriteService;
	private final EventLikeService eventLikeService;

	@Override
	@Transactional
	public Page<EventRes> getEvents(EventSearchCondition condition, Pageable pageable) {

		Optional<Member> currentMemberOpt = securityUtil.getCurrentMemberOpt();

		Specification<Event> spec = createEventSpecification(currentMemberOpt, condition);

		Page<Event> eventsPage = eventRepository.findAll(spec, pageable);

		return eventsPage.map(this::convertToEventRes);
	}

	/**
	 * 검색 조건에 따른 Event 엔티티 명세를 생성
	 *
	 * @param condition 검색 조건 객체
	 * @return 생성된 명세 객체
	 */
	private Specification<Event> createEventSpecification(Optional<Member> memberOpt, EventSearchCondition condition) {
		Specification<Event> spec = Specification.where(null);

		// 상태 필터
		if (condition.getStatus() != null) {
			spec = spec.and(EventSpecifications.hasStatus(condition.getStatus()));
		}

		// 유료/무료 필터
		if (condition.getIsFree() != null && !condition.getIsFree().isEmpty()) {
			spec = spec.and(EventSpecifications.hasIsFree(condition.getIsFree()));
		}

		// 카테고리 필터
		if (condition.getCodename() != null && !condition.getCodename().isEmpty()) {
			spec = spec.and(EventSpecifications.hasCodename(condition.getCodename()));
		}

		// 날짜 범위 필터
		if (condition.hasDateCondition()) {
			spec = spec.and(EventSpecifications.dateRangeOverlaps(condition.getStartDate(), condition.getEndDate()));
		}

		// 제목 검색 + 검색어 저장
		if (condition.hasTitleKeyword()) {
			if (memberOpt.isPresent()) {
				EventSearchHistory searchHistory = EventSearchHistory.builder()
					.member(memberOpt.get())
					.content(condition.getTitle())
					.build();
				eventSearchHistoryRepository.save(searchHistory);
			}
			spec = spec.and(EventSpecifications.titleContains(condition.getTitle()));
		}

		return spec;
	}

	/**
	 * Event 엔티티를 EventRes DTO로 변환
	 */
	private EventRes convertToEventRes(Event event) {
		return EventRes.builder()
			.eventId(event.getId())
			.title(event.getTitle())
			.category(event.getCodename())
			.isFree(event.getIsFree())
			.status(event.getStatus().toString())
			.startDate(event.getStartDate())
			.endDate(event.getEndDate())
			.mainImg(event.getMainImg())
			.rating(event.getRating())
			.likes(event.getLikes())
			.favorites(event.getFavorites())
			.comments(event.getComments())
			.ratingCount(event.getEventReviews().size())
			.latitude(event.getLatitude())
			.longitude(event.getLongitude())
			.roadAddress(event.getRoadAddress())
			.build();
	}

	@Override
	public EventDetailRes getEventDetail(Long eventId) {

		Member currentMember = securityUtil.getCurrentMember();

		Event event = eventRepository.findById(eventId)
			.orElseThrow(() -> new BusinessException(EventErrorCode.NOT_EXIST_EVENT));

		EventFavorite favorite = eventFavoriteService.getEventFavoriteByEvent(event);
		EventLike like = eventLikeService.getEventLikeByEvent(event);

		boolean isFavorite = currentMember.getEventFavorites().contains(favorite);
		boolean isLiked = currentMember.getEventLikes().contains(like);

		return EventDetailRes.builder()
			.eventId(event.getId())
			.status(event.getStatus().name())
			.category(event.getCodename())
			.title(event.getTitle())
			.orgName(event.getOrgName())
			.useFee(event.getUseFee())
			.timeInfo(event.getTimeInfo())
			.orgLink(event.getOrgLink())
			.mainImg(event.getMainImg())
			.startDate(event.getStartDate())
			.endDate(event.getEndDate())
			.isFree(event.getIsFree())
			.likes(event.getLikes())
			.favorites(event.getFavorites())
			.comments(event.getComments())
			.rating(event.getRating())
			.isFavorite(isFavorite)
			.isLiked(isLiked)
			.latitude(event.getLatitude())
			.longitude(event.getLongitude())
			.roadAddress(event.getRoadAddress())
			.build();
	}

}
