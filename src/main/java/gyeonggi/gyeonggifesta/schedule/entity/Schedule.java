package gyeonggi.gyeonggifesta.schedule.entity;

import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "member_schedule",
	uniqueConstraints = {
		// 한 회원이 같은 채팅방/날짜로 일정 중복 생성되는 것 방지
		@UniqueConstraint(columnNames = {"member_id", "chat_room_id", "event_date"})
	}
)
public class Schedule extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "schedule_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	// 동행 채팅방과 연결 (일반 수동 일정은 null 가능)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id")
	private ChatRoom chatRoom;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(name = "event_date", nullable = false)
	private LocalDate eventDate;

	@Column(length = 255)
	private String memo;

	@Builder
	public Schedule(Member member, ChatRoom chatRoom, String title, LocalDate eventDate, String memo) {
		this.member = member;
		this.chatRoom = chatRoom;
		this.title = title;
		this.eventDate = eventDate;
		this.memo = memo;
	}

	// 변경 메서드
	public void update(String title, LocalDate eventDate, String memo) {
		if (title != null && !title.isBlank()) {
			this.title = title;
		}
		if (eventDate != null) {
			this.eventDate = eventDate;
		}
		// memo는 빈 문자열도 허용 (지우고 싶을 수 있으니까)
		if (memo != null) {
			this.memo = memo;
		}
	}
}
