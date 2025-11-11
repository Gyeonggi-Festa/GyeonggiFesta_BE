package gyeonggi.gyeonggifesta.recommand.batch.step;

import gyeonggi.gyeonggifesta.recommand.dto.request.AiRecommendReq;
import gyeonggi.gyeonggifesta.recommand.dto.response.AiRecommendRes;
import gyeonggi.gyeonggifesta.recommand.service.AiRecommendService;
import gyeonggi.gyeonggifesta.recommand.service.AiRecommendationSaveService;
import gyeonggi.gyeonggifesta.recommand.service.MlServerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AiRecommendStepConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final AiRecommendService aiRecommendService;
	private final MlServerClient mlServerClient;
	private final AiRecommendationSaveService aiRecommendationSaveService;

	/**
	 * AI 추천 처리 스텝 정의
	 */
	@Bean
	public Step processAiRecommendationsStep() {
		return new StepBuilder("processAiRecommendationsStep", jobRepository)
			.tasklet(processAiRecommendationsTasklet(), transactionManager)
			.build();
	}

	/**
	 * AI 추천 처리를 위한 태스클릿 정의
	 * 모든 사용자에 대한 추천 요청을 일괄적으로 처리
	 */
	@Bean
	public Tasklet processAiRecommendationsTasklet() {
		return (contribution, chunkContext) -> {
			log.info("AI 추천 처리 시작");

			// 1. 모든 사용자의 추천 요청 DTO 생성
			List<AiRecommendReq> requests = aiRecommendService.createAllMembersAiRecommendRequests();
			log.info("생성된 추천 요청 수: {}", requests.size());

			if (CollectionUtils.isEmpty(requests)) {
				log.info("추천할 사용자가 없습니다.");
				return RepeatStatus.FINISHED;
			}

			try {
				// 2. 모든 요청을 리스트로 한 번에 ML 서버에 전송
				List<AiRecommendRes> responses = mlServerClient.getRecommendations(requests);
				log.info("AI 추천 응답 수신: {} 건", responses.size());

				// 3. 응답 결과를 사용자 ID로 매핑하여 저장
				int successCount = 0;
				int failCount = 0;

				for (AiRecommendRes response : responses) {
					try {
						// 각 응답을 개별적으로 저장
						aiRecommendationSaveService.saveRecommendations(response);
						successCount++;
					} catch (Exception e) {
						log.error("사용자 {}의 추천 결과 저장 중 오류 발생: {}", response.getUserid(), e.getMessage());
						failCount++;
					}
				}

				log.info("AI 추천 처리 완료 - 성공: {}, 실패: {}", successCount, failCount);

			} catch (Exception e) {
				log.error("AI 추천 일괄 처리 중 오류 발생: {}", e.getMessage());
			}

			return RepeatStatus.FINISHED;
		};
	}
}
