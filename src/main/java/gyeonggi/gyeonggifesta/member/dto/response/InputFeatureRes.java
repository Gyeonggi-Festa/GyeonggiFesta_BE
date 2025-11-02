package gyeonggi.gyeonggifesta.member.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InputFeatureRes {

	private String accessToken;
	private String refreshToken;
}
