package gyeonggi.gyeonggifesta.event.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

@Slf4j
public class EventJobListener implements JobExecutionListener {

	@Override
	public void beforeJob(JobExecution jobExecution) {
		log.info("축제 불러오기 Job {} started.", jobExecution.getJobInstance().getJobName());
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		if(jobExecution.getStatus().isUnsuccessful()){
			log.error("축제 불러오기 Job {} failed with exceptions: {}",
				jobExecution.getJobInstance().getJobName(),
				jobExecution.getAllFailureExceptions());
		} else {
			log.info("축제 불러오기 Job {} completed successfully.", jobExecution.getJobInstance().getJobName());
		}
	}
}
