package gyeonggi.gyeonggifesta.board.dto.media.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PresignedUrlResponse {
	private final String s3Key;
	private final String presignedUrl;
}
