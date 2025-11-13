package gyeonggi.gyeonggifesta.chat.entity;

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
@Table(name = "companion_chat_room")
public class CompanionChatRoom extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "companion_chat_room_id")
	private Long id;

	/**
	 * 실제 채팅은 기존 ChatRoom / ChatMessage 를 그대로 사용하고
	 * 동행찾기용 메타 정보만 별도 테이블로 관리
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id", nullable = false, unique = true)
	private ChatRoom chatRoom;

	/**
	 * 행사(축제) 날짜 – 필수
	 */
	@Column(name = "event_date", nullable = false)
	private LocalDate eventDate;

	@Builder
	public CompanionChatRoom(ChatRoom chatRoom, LocalDate eventDate) {
		this.chatRoom = chatRoom;
		this.eventDate = eventDate;
	}
}
