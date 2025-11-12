package gyeonggi.gyeonggifesta.event.service.viewhistory;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.entity.EventViewHistory;
import gyeonggi.gyeonggifesta.event.exception.EventErrorCode;
import gyeonggi.gyeonggifesta.event.repository.EventRepository;
import gyeonggi.gyeonggifesta.event.repository.EventViewHistoryRepository;
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
public class EventViewHistoryServiceImpl implements EventViewHistoryService {

    private final SecurityUtil securityUtil;
    private final EventRepository eventRepository;
    private final EventViewHistoryRepository eventViewHistoryRepository;

    @Override
    @Transactional
    public void createView(Long eventId) {
        Member me = securityUtil.getCurrentMember();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(EventErrorCode.NOT_EXIST_EVENT));

        EventViewHistory history = EventViewHistory.builder()
                .member(me)
                .event(event)
                .build();

        me.addViewHistory(history);
        eventViewHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventRes> getMyRecentViews(Pageable pageable) {
        Member me = securityUtil.getCurrentMember();
        Page<EventViewHistory> page = eventViewHistoryRepository.findByMemberOrderByCreatedAtDesc(me, pageable);

        return page.map(h -> {
            Event e = h.getEvent();
            String status = (e.getStatus() != null) ? e.getStatus().name() : null;
            int ratingCount = (e.getEventReviews() != null) ? e.getEventReviews().size() : 0;

            return EventRes.builder()
                    .eventId(e.getId())
                    .title(e.getTitle())
                    .category(e.getCodename())
                    .isFree(e.getIsFree())
                    .status(status)
                    .startDate(e.getStartDate())
                    .endDate(e.getEndDate())
                    .mainImg(e.getMainImg())
                    .likes(e.getLikes())
                    .favorites(e.getFavorites())
                    .comments(e.getComments())
                    .rating(e.getRating())
                    .ratingCount(ratingCount)
                    .build();
        });
    }
}
