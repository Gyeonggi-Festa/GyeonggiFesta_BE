package gyeonggi.gyeonggifesta.board.dto.post.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListRes {
	private Long postId;
	private String title;
	private String writer;
	private long viewCount;
	private long likes;
	private long comments;
	private LocalDate updatedAt;
}
