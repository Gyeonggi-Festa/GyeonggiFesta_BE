package gyeonggi.gyeonggifesta.event.batch.step;

import gyeonggi.gyeonggifesta.event.batch.processor.ApiEventItemProcessor;
import gyeonggi.gyeonggifesta.event.batch.reader.OpenApiEventItemReader;
import gyeonggi.gyeonggifesta.event.batch.tasklet.UpdateMissingEventsTasklet;
import gyeonggi.gyeonggifesta.event.batch.writer.EventItemWriter;
import gyeonggi.gyeonggifesta.event.dto.batch.response.OpenApiEventListRes.GyeonggiEventRow;
import gyeonggi.gyeonggifesta.event.entity.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class EventStepConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	private final OpenApiEventItemReader openApiEventItemReader;
	private final ApiEventItemProcessor apiEventItemProcessor;
	private final EventItemWriter eventItemWriter;
	private final UpdateMissingEventsTasklet updateMissingEventsTasklet;

	@Bean
	public Step processApiEventsStep() {
		return new StepBuilder("processApiEventsStep", jobRepository)
				.<GyeonggiEventRow, Event>chunk(100, transactionManager)
				.reader(openApiEventItemReader)
				.processor(apiEventItemProcessor)
				.writer(eventItemWriter)
				// 내결함: 개별 레코드 실패 시 스킵하고 계속
				.faultTolerant()
				.skip(DataIntegrityViolationException.class)
				.skipLimit(200)
				.build();
	}

	@Bean
	public Step updateMissingEventsStep() {
		return new StepBuilder("updateMissingEventsStep", jobRepository)
				.tasklet(updateMissingEventsTasklet, transactionManager)
				.build();
	}
}
