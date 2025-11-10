package gyeonggi.gyeonggifesta.calendar.repository;

import gyeonggi.gyeonggifesta.calendar.domain.CalendarCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CalendarCredentialRepository extends JpaRepository<CalendarCredential, Long> {
    Optional<CalendarCredential> findByMemberId(Long memberId);
}
