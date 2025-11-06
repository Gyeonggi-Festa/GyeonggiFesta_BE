package gyeonggi.gyeonggifesta.chat.entity;

import gyeonggi.gyeonggifesta.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_media")
public class ChatMedia extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "media_id")
	private Long mediaId;

	// 채팅 메시지와 연관관계 (message_id)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "message_id", nullable = false)
	private ChatMessage chatMessage;

	// S3 객체 키 (저장 경로)
	@Column(name = "s3_key", nullable = false, length = 255)
	private String s3Key;

	@Builder
	public ChatMedia(ChatMessage chatMessage, String s3Key) {
		this.chatMessage = chatMessage;
		this.s3Key = s3Key;
	}

	// 연관관계 편의 메서드: ChatMessage 설정
	public void setChatMessage(ChatMessage chatMessage) {
		this.chatMessage = chatMessage;
	}
}
