package gyeonggi.gyeonggifesta.board.repository;

import gyeonggi.gyeonggifesta.board.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

	List<PostComment> findByPost_IdAndParentIsNull(Long postId, Pageable pageable);
	Page<PostComment> findByPost_Id(Long postId, Pageable pageable);
}
