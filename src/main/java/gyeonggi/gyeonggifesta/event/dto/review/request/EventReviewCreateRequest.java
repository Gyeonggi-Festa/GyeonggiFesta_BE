package gyeonggi.gyeonggifesta.event.dto.review.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class EventReviewCreateRequest {

	@NotNull(message = "이벤트 ID는 필수입니다.")
	private Long eventId;

	@NotBlank(message = "리뷰 내용은 필수입니다.")
	private String content;

	@NotNull(message = "별점은 필수입니다.")
	private Double rating;

	private List<MediaRequest> mediaList;

	@Builder
	public EventReviewCreateRequest(Long eventId, String content, Double rating, List<MediaRequest> mediaList) {
		this.eventId = eventId;
		this.content = content;
		this.rating = rating;
		this.mediaList = mediaList;
	}
}