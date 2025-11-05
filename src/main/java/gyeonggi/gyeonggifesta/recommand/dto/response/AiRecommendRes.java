package gyeonggi.gyeonggifesta.recommand.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendRes {

	// 사용자 ID
	private String userid;

	// 축제 추천 정보 리스트
	private List<FestivalRecommendation> festivalRecommendations;

	/**
	 * 축제 추천 정보를 담는 내부 클래스
	 */
	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class FestivalRecommendation {

		// 추천 이벤트 ID 목록
		private List<String> eventid;
	}
}