package gyeonggi.gyeonggifesta.calendar.repository;

import gyeonggi.gyeonggifesta.calendar.domain.CalendarEventLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CalendarEventLinkRepository extends JpaRepository<CalendarEventLink, Long> {
    Optional<CalendarEventLink> findByMemberIdAndEventId(Long memberId, Long eventId);
}
