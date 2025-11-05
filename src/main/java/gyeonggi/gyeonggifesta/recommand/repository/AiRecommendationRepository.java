package gyeonggi.gyeonggifesta.recommand.repository;

import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.recommand.entity.AiRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, Long> {

	List<AiRecommendation> findByMember(Member member);

	// 특정 회원의 최신 추천 목록 조회 (가장 최근 날짜의 추천만)
	@Query("SELECT ar FROM AiRecommendation ar WHERE ar.member = :member AND DATE(ar.createdAt) = " +
		"(SELECT MAX(DATE(a.createdAt)) FROM AiRecommendation a WHERE a.member = :member)")
	List<AiRecommendation> findLatestByMember(@Param("member") Member member);

	// 특정 회원의 모든 추천 기록을 날짜별로 조회 (최신 날짜순으로 정렬)
	@Query("SELECT ar FROM AiRecommendation ar WHERE ar.member = :member ORDER BY ar.createdAt DESC")
	List<AiRecommendation> findAllByMemberOrderByCreatedAtDesc(@Param("member") Member member);

	// 특정 회원의 특정 날짜 추천 목록 조회
	@Query("SELECT ar FROM AiRecommendation ar WHERE ar.member = :member AND DATE(ar.createdAt) = :date")
	List<AiRecommendation> findByMemberAndCreatedAtDate(@Param("member") Member member, @Param("date") LocalDate date);
}
