package gyeonggi.gyeonggifesta.event.repository;

import gyeonggi.gyeonggifesta.event.entity.EventViewHistory;
import gyeonggi.gyeonggifesta.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventViewHistoryRepository extends JpaRepository<EventViewHistory, Long> {

    Page<EventViewHistory> findByMemberOrderByCreatedAtDesc(Member member, Pageable pageable);
}
