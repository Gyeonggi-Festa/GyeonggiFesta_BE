package gyeonggi.gyeonggifesta.schedule.repository;

import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.schedule.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	// 내 일정 조회는 확실하게 member_id 로 조회
	Page<Schedule> findByMemberIdOrderByEventDateAscIdDesc(Long memberId, Pageable pageable);

	// 수정/삭제용 조회도 member_id 기반으로
	Optional<Schedule> findByIdAndMemberId(Long id, Long memberId);

	// 동행 일정 중복 체크용 (그대로 유지)
	boolean existsByMemberAndChatRoomAndEventDate(Member member, ChatRoom chatRoom, LocalDate eventDate);
}
