package gyeonggi.gyeonggifesta.event.dto.comment.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCommentReq {

	private Long eventId;
	private Long parentCommentId = null;
	private String content;
}
