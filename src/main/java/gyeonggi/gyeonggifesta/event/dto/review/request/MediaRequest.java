package gyeonggi.gyeonggifesta.event.dto.review.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MediaRequest {

	@NotBlank(message = "S3 키는 필수입니다.")
	private String s3Key;

	private int order;

	@Builder
	public MediaRequest(String s3Key, int order) {
		this.s3Key = s3Key;
		this.order = order;
	}
}
