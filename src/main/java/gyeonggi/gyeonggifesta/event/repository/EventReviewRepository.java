package gyeonggi.gyeonggifesta.event.repository;

import gyeonggi.gyeonggifesta.event.entity.EventReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventReviewRepository extends JpaRepository<EventReview, Long> {

	Page<EventReview> findByEventIdOrderByCreatedAtDesc(Long eventId, Pageable pageable);

	Page<EventReview> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

	Optional<EventReview> findByEventIdAndMemberId(Long eventId, Long memberId);

	long countByEventId(Long eventId);
}
