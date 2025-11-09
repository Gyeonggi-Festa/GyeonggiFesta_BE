package gyeonggi.gyeonggifesta.event.batch.listener;

import gyeonggi.gyeonggifesta.event.batch.writer.EventSyncWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSyncStepListener implements StepExecutionListener {

	private final EventSyncWriter eventSyncWriter;

	@Override
	public void beforeStep(StepExecution stepExecution) {
		log.info("이벤트 동기화 Step 시작: {}", stepExecution.getStepName());
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		log.info("이벤트 동기화 Step 종료: {}, 상태: {}",
			stepExecution.getStepName(),
			stepExecution.getStatus());

		// Step 완료 후 한 번에 모든 이벤트 전송
		eventSyncWriter.sendAllEvents();

		return ExitStatus.COMPLETED;
	}
}