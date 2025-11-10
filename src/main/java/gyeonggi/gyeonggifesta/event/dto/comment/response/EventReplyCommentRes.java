package gyeonggi.gyeonggifesta.event.dto.comment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventReplyCommentRes {
	private Long commentId;
	private Long eventId;
	private Long memberId;
	private String content;
	private Long parentCommentId;  // 부모 댓글 ID 참조
	private String createdAt;
}