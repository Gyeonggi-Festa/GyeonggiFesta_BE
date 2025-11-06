package gyeonggi.gyeonggifesta.board.entity;

import gyeonggi.gyeonggifesta.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostMedia extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "media_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	@Setter
	private Post post;

	@Column(name = "s3_key")
	private String s3Key;

	@Builder
	public PostMedia(Post post, String s3Key) {
		this.post = post;
		this.s3Key = s3Key;
	}
}
