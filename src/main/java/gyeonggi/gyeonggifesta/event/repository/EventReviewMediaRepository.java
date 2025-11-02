package gyeonggi.gyeonggifesta.event.repository;

import gyeonggi.gyeonggifesta.event.entity.EventReviewMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventReviewMediaRepository extends JpaRepository<EventReviewMedia, Long> {

	List<EventReviewMedia> findByEventReviewIdOrderByOrder(Long reviewId);

	void deleteByEventReviewId(Long reviewId);

	List<EventReviewMedia> findByS3Key(String s3Key);
}