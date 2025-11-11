package gyeonggi.gyeonggifesta.board.dto.comment.request;

import lombok.Getter;

@Getter
public class PostCommentReq {
	private Long postId;
	private Long parentCommentId;
	private String content;
}