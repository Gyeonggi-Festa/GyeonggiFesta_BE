package gyeonggi.gyeonggifesta.recommand.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendReq {

	private String userid;
	private List<String> searchHistory;
	private List<String> favorites;

}
