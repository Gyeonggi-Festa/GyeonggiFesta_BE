package gyeonggi.gyeonggifesta.event.repository;

import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.entity.EventLike;
import gyeonggi.gyeonggifesta.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventLikeRepository extends JpaRepository<EventLike, Long> {

	Optional<EventLike> findByEventAndMember(Event event, Member member);

	boolean existsByEventAndMember(Event event, Member member);

	// 내가 좋아요한 목록 페이지 조회
	//  N+1 줄이려고 Event를 즉시 로딩(EntityGraph)해둠
	@EntityGraph(attributePaths = "event")
	Page<EventLike> findByMember(Member member, Pageable pageable);
}
