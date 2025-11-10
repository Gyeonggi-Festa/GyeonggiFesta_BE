package gyeonggi.gyeonggifesta.event.dto.review.response;

import gyeonggi.gyeonggifesta.event.entity.EventReviewMedia;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MediaResponse {

	private String imageUrl;
	private int order;
	private String s3Key;

	@Builder
	public MediaResponse(String imageUrl, int order, String s3Key) {
		this.imageUrl = imageUrl;
		this.order = order;
		this.s3Key = s3Key;
	}

	public static MediaResponse from(EventReviewMedia media) {
		return MediaResponse.builder()
			.imageUrl(media.getImageUrl())
			.order(media.getOrder())
			.s3Key(media.getS3Key())
			.build();
	}
}