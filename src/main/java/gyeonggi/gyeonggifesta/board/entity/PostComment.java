package gyeonggi.gyeonggifesta.board.entity;

import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostComment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	@Setter
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	@Setter
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_comment_id")
	@Setter
	private PostComment parent;

	@Column(columnDefinition = "TEXT")
	@Setter
	private String content;

	@Column(nullable = false)
	private boolean deleted = false;

	@OneToMany(mappedBy = "parent", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private List<PostComment> replies = new ArrayList<>();

	@Builder
	public PostComment(Post post, Member member, PostComment parent, String content) {
		this.post = post;
		this.member = member;
		this.parent = parent;
		this.content = content;
		this.deleted = false;
	}

	// 연관관계 편의 메서드: 자식 댓글 추가
	public void addReply(PostComment reply) {
		this.replies.add(reply);
	}

	public void removeReply(PostComment reply) {
		this.replies.remove(reply);
	}

	public void markAsDeleted() {
		this.deleted = true;
	}

	// 댓글 내용 조회 (삭제 여부에 따라 다른 내용 반환)
	public String getDisplayContent() {
		return deleted ? "삭제된 댓글입니다." : content;
	}
}
