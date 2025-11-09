package gyeonggi.gyeonggifesta.event.repository;

import gyeonggi.gyeonggifesta.event.entity.EventSearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventSearchHistoryRepository extends JpaRepository<EventSearchHistory, Long> {

	List<EventSearchHistory> findByMemberId(Long memberId);
}
