package gyeonggi.gyeonggifesta.event.dto.review.response;

import gyeonggi.gyeonggifesta.event.entity.EventReviewMedia;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EventReviewMediaResponse {

	private Long id;
	private String imageUrl;

	@Builder
	public EventReviewMediaResponse(Long id, String imageUrl) {
		this.id = id;
		this.imageUrl = imageUrl;
	}

	public static EventReviewMediaResponse from(EventReviewMedia media) {
		return EventReviewMediaResponse.builder()
			.id(media.getId())
			.imageUrl(media.getImageUrl())
			.build();
	}
}
