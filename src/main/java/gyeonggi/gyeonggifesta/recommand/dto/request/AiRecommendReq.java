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

	private List<String> views; // 추가부분 -> 조회 기록(최근 본 이벤트 제목 리스트 등) 얘도 모델 추천할 때 사용

}
