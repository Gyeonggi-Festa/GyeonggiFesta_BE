package gyeonggi.gyeonggifesta.board.repository;

import gyeonggi.gyeonggifesta.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findByEventId(Long eventId);
}
