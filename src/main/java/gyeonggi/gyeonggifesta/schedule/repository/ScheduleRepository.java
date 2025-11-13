package gyeonggi.gyeonggifesta.schedule.repository;

import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.schedule.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	Page<Schedule> findByMemberOrderByEventDateAscIdDesc(Member member, Pageable pageable);

	java.util.Optional<Schedule> findByIdAndMember(Long id, Member member);

	boolean existsByMemberAndChatRoomAndEventDate(Member member, ChatRoom chatRoom, LocalDate eventDate);
}
