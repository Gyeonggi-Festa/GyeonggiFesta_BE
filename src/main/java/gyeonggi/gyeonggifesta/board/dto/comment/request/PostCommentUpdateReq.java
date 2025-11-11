package gyeonggi.gyeonggifesta.board.dto.comment.request;

import lombok.Getter;

@Getter
public class PostCommentUpdateReq {
	private Long commentId;
	private String content;
}
