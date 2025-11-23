package gyeonggi.gyeonggifesta.config;

import gyeonggi.gyeonggifesta.event.batch.listener.EventJobListener;
import gyeonggi.gyeonggifesta.event.batch.step.EventSyncStepConfig;
import gyeonggi.gyeonggifesta.event.batch.step.FaultTolerantEventStepConfig;
import gyeonggi.gyeonggifesta.recommand.batch.listener.RecommendJobListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class BatchConfig {

	private final JobLauncher jobLauncher;
	private final JobRepository jobRepository;
	private final FaultTolerantEventStepConfig faultTolerantEventStepConfig;
	private final Step processApiEventsStep;
	private final Step updateMissingEventsStep;
	private final Step processAiRecommendationsStep;
	private final EventSyncStepConfig eventSyncStepConfig;

	@Bean
	public Job eventSyncJob() {
		return new JobBuilder("eventSyncJob", jobRepository)
			.listener(new EventJobListener())
			.start(faultTolerantEventStepConfig.faultTolerantProcessApiEventsStep())
			.next(processApiEventsStep)
			.next(updateMissingEventsStep)
			.build();
	}

	/**
	 * AI 추천 작업 정의
	 */
	@Bean
	public Job aiRecommendationJob() {
		return new JobBuilder("aiRecommendationJob", jobRepository)
			.listener(new RecommendJobListener())
			.start(processAiRecommendationsStep)
			.build();
	}

	@Bean
	public Job eventDataSyncJob() {
		return new JobBuilder("eventDataSyncJob", jobRepository)
			.start(eventSyncStepConfig.eventSyncStep())
			.build();
	}

	@Scheduled(cron = "0 0 22 * * ?", zone = "Asia/Seoul")
	public void performEventSyncJob() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
			.addLong("time", System.currentTimeMillis())
			.toJobParameters();
		jobLauncher.run(eventSyncJob(), jobParameters);
	}

	@Scheduled(cron = "0 0/20 0 * * ?", zone = "Asia/Seoul")
	public void performAiRecommendationJob() throws Exception {
		try {
			JobParameters jobParameters = new JobParametersBuilder()
				.addLong("time", System.currentTimeMillis())
				.toJobParameters();

			jobLauncher.run(aiRecommendationJob(), jobParameters);
			log.info("AI 추천 배치 작업이 성공적으로 실행되었습니다.");
		} catch (Exception e) {
			log.error("AI 추천 배치 작업 실행 중 오류 발생: {}", e.getMessage(), e);
		}
	}

	@Scheduled(cron = "0 0 23 * * ?", zone = "Asia/Seoul")
	public void performEventDataSyncJob() throws Exception {
		try {
			JobParameters jobParameters = new JobParametersBuilder()
				.addLong("time", System.currentTimeMillis())
				.toJobParameters();
			jobLauncher.run(eventDataSyncJob(), jobParameters);
			log.info("이벤트 데이터 동기화 배치 작업이 성공적으로 실행되었습니다.");
		} catch (Exception e) {
			log.error("이벤트 데이터 동기화 배치 작업 실행 중 오류 발생: {}", e.getMessage(), e);
		}
	}
}
