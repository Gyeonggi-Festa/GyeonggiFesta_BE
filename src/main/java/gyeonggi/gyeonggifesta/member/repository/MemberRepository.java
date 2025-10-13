package gyeonggi.gyeonggifesta.member.repository;

import gyeonggi.gyeonggifesta.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByVerifyId(String verifyId);

	Optional<Member> findByEmail(String email);

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);
}
