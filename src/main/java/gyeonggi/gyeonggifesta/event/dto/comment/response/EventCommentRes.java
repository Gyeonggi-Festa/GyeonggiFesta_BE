package gyeonggi.gyeonggifesta.event.dto.comment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCommentRes {
	private Long commentId;
	private Long eventId;
	private Long memberId;
	private String content;
	private String createdAt;

	// 대댓글 목록
	@Builder.Default
	private List<EventReplyCommentRes> replies = new ArrayList<>();
}
