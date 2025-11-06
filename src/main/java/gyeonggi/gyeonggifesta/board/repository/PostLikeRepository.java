package gyeonggi.gyeonggifesta.board.repository;

import gyeonggi.gyeonggifesta.board.entity.Post;
import gyeonggi.gyeonggifesta.board.entity.PostLike;
import gyeonggi.gyeonggifesta.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

	boolean existsByPostAndMember(Post post, Member member);

	Optional<PostLike> findByPostAndMember(Post post, Member member);
	Page<PostLike> findByMember(Member member, Pageable pageable);
}
