package gyeonggi.gyeonggifesta.board.dto.comment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentRes {
	private Long commentId;
	private Long postId;
	private String verifyId;
	private String memberName;
	private String content;
	private String createdAt;
	private List<PostCommentRes> replies;  // 대댓글 리스트
}
