package gyeonggi.gyeonggifesta.chat.entity;

import gyeonggi.gyeonggifesta.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_message")
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "message_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoom chatRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id", nullable = false)
	private Member sender;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(length = 50)
	private String type;  // 예: "TEXT", "IMAGE", "FILE" 등

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	// 메시지에 첨부된 미디어들
	@OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ChatMedia> chatMedias = new ArrayList<>();

	@Builder
	public ChatMessage(ChatRoom chatRoom, Member sender, String content, String type, LocalDateTime deletedAt) {
		this.chatRoom = chatRoom;
		this.sender = sender;
		this.content = content;
		this.type = type;
		this.deletedAt = deletedAt;
	}

	// 연관관계 편의 메서드: 채팅방 설정
	public void setChatRoom(ChatRoom chatRoom) {
		this.chatRoom = chatRoom;
	}

	// 연관관계 편의 메서드: 발신자 설정
	public void setSender(Member sender) {
		this.sender = sender;
	}

	// 연관관계 편의 메서드: 첨부 미디어 추가
	public void addChatMedia(ChatMedia chatMedia) {
		this.chatMedias.add(chatMedia);
		chatMedia.setChatMessage(this);
	}

	// 연관관계 편의 메서드: 첨부 미디어 제거
	public void removeChatMedia(ChatMedia chatMedia) {
		this.chatMedias.remove(chatMedia);
		chatMedia.setChatMessage(null);
	}

	// 메시지를 삭제된 상태로 표시하는 편의 메서드
	public void markAsDeleted() {
		this.deletedAt = LocalDateTime.now();
	}
}
