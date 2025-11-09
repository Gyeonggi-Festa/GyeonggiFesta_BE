package gyeonggi.gyeonggifesta.event.entity;

import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventComment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id", nullable = false)
	@Setter
	private Event event;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	@Setter
	private Member member;

	@Column(columnDefinition = "TEXT")
	@Setter
	private String content;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_comment_id")
	@Setter
	private EventComment parent;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventComment> replies = new ArrayList<>();

	@Builder
	public EventComment(Event event, Member member, String content, EventComment parent) {
		this.event = event;
		this.member = member;
		this.content = content;
		this.parent = parent;
	}

	// 연관관계 편의 메서드: 부모 댓글에 대댓글을 추가하고, 대댓글의 parent 필드도 설정
	public void addReply(EventComment reply) {
		this.replies.add(reply);
		reply.setParent(this);
	}

	public void removeReply(EventComment reply) {
		this.replies.remove(reply);
	}

	public void clearReplies() {
		this.replies.clear();
	}
}
