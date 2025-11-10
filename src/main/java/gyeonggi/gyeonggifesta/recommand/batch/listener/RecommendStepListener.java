package gyeonggi.gyeonggifesta.recommand.batch.listener;

import gyeonggi.gyeonggifesta.recommand.dto.response.AiRecommendRes;
import gyeonggi.gyeonggifesta.recommand.entity.AiRecommendation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemProcessListener;

@Slf4j
public class RecommendStepListener implements ItemProcessListener<AiRecommendRes, AiRecommendation> {

	@Override
	public void beforeProcess(AiRecommendRes item) {
	}

	@Override
	public void afterProcess(AiRecommendRes item, AiRecommendation result) {
	}

	@Override
	public void onProcessError(AiRecommendRes item, Exception e) {
		log.error("AI 추천 Error processing item: {}. Exception: {}", item.getUserid(), e.getMessage());
	}
}
