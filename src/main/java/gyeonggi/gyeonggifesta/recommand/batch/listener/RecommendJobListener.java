package gyeonggi.gyeonggifesta.recommand.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

@Slf4j
public class RecommendJobListener implements JobExecutionListener {

	@Override
	public void beforeJob(JobExecution jobExecution) {
		log.info("AI 추천 Job {} started.", jobExecution.getJobInstance().getJobName());
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		if(jobExecution.getStatus().isUnsuccessful()){
			log.error("AI 추천 Job {} failed with exceptions: {}",
				jobExecution.getJobInstance().getJobName(),
				jobExecution.getAllFailureExceptions());
		} else {
			log.info("AI 추천 Job {} completed successfully.", jobExecution.getJobInstance().getJobName());
		}
	}
}
