package gyeonggi.gyeonggifesta.event.repository;

import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.entity.EventFavorite;
import gyeonggi.gyeonggifesta.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventFavoriteRepository extends JpaRepository<EventFavorite, Long> {

	List<EventFavorite> findByMemberId(Long memberId);

	Optional<EventFavorite> findByEventAndMember(Event event, Member member);

	boolean existsByEventAndMember(Event event, Member member);

	Page<EventFavorite> findByMember(Member member, Pageable pageable);
}
