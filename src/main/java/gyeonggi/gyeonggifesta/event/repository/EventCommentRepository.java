package gyeonggi.gyeonggifesta.event.repository;

import gyeonggi.gyeonggifesta.event.entity.EventComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventCommentRepository extends JpaRepository<EventComment, Long> {

	Page<EventComment> findByEvent_Id(Long eventId, Pageable pageable);

	/**
	 * 특정 이벤트의 부모 댓글만 페이징 조회 (parent가 null인 댓글들)
	 */
	Page<EventComment> findByEvent_IdAndParentIsNull(Long eventId, Pageable pageable);

	/**
	 * 특정 이벤트의 특정 부모 댓글들에 달린 대댓글들 조회
	 */
	List<EventComment> findByEvent_IdAndParentIdIn(Long eventId, List<Long> parentIds);
}
