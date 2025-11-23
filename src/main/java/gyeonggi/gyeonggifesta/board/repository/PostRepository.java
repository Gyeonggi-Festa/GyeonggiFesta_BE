package gyeonggi.gyeonggifesta.board.repository;

import gyeonggi.gyeonggifesta.board.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
        Page<Post> findByBoardId(Long boardId, Pageable pageable);

        Page<Post> findByBoardIdAndEventId(Long boardId, Long eventId, Pageable pageable);

        @Query("SELECT p FROM Post p LEFT JOIN p.postLikes l WHERE p.board.id = :boardId GROUP BY p.id ORDER BY COUNT(l) DESC")
        Page<Post> findByBoardIdOrderByLikesCountDesc(@Param("boardId") Long boardId, Pageable pageable);

        @Query("SELECT p FROM Post p LEFT JOIN p.postLikes l WHERE p.board.id = :boardId AND p.event.id = :eventId GROUP BY p.id ORDER BY COUNT(l) DESC")
        Page<Post> findByBoardIdAndEventIdOrderByLikesCountDesc(@Param("boardId") Long boardId, @Param("eventId") Long eventId, Pageable pageable);

        @Query("SELECT p FROM Post p LEFT JOIN p.postComments c WHERE p.board.id = :boardId GROUP BY p.id ORDER BY COUNT(c) DESC")
        Page<Post> findByBoardIdOrderByCommentsCountDesc(@Param("boardId") Long boardId, Pageable pageable);

        @Query("SELECT p FROM Post p LEFT JOIN p.postComments c WHERE p.board.id = :boardId AND p.event.id = :eventId GROUP BY p.id ORDER BY COUNT(c) DESC")
        Page<Post> findByBoardIdAndEventIdOrderByCommentsCountDesc(@Param("boardId") Long boardId, @Param("eventId") Long eventId, Pageable pageable);

}
