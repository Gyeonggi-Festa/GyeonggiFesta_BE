package gyeonggi.gyeonggifesta.chat.entity;

import gyeonggi.gyeonggifesta.chat.enums.ChatRole;
import gyeonggi.gyeonggifesta.chat.enums.ChatRoomMemberStatus;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_room_member")
public class ChatRoomMember extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "crm_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoom chatRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(value = EnumType.STRING)
	private ChatRole role;

	@Column(name = "joined_at", nullable = false)
	private LocalDateTime joinedAt;

	@Column(name = "kicked_at")
	@Setter
	private LocalDateTime kickedAt;

	@Enumerated(value = EnumType.STRING)
	private ChatRoomMemberStatus status;

	@Column(name = "last_read_at")
	@Setter
	private LocalDateTime lastReadAt;

	@Builder
	public ChatRoomMember(ChatRoom chatRoom, Member member, ChatRole role, LocalDateTime joinedAt, ChatRoomMemberStatus status, LocalDateTime lastReadAt) {
		this.chatRoom = chatRoom;
		this.member = member;
		this.role = role;
		this.joinedAt = joinedAt != null ? joinedAt : LocalDateTime.now();
		this.status = status;
		this.lastReadAt = lastReadAt;
	}

	// 연관관계 편의 메서드: 채팅방 설정
	public void setChatRoom(ChatRoom chatRoom) {
		this.chatRoom = chatRoom;
	}

	// 연관관계 편의 메서드: 회원 설정
	public void setMember(Member member) {
		this.member = member;
	}

	public void setRole(ChatRole role) {
		this.role = role;
	}

	public void setStatus(ChatRoomMemberStatus status) {
		this.status = status;
	}
}
