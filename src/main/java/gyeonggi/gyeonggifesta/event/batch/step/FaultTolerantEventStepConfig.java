package gyeonggi.gyeonggifesta.event.batch.step;

import gyeonggi.gyeonggifesta.event.batch.listener.EventStepListener;
import gyeonggi.gyeonggifesta.event.batch.processor.ApiEventItemProcessor;
import gyeonggi.gyeonggifesta.event.batch.reader.OpenApiEventItemReader;
import gyeonggi.gyeonggifesta.event.batch.writer.EventItemWriter;
import gyeonggi.gyeonggifesta.event.dto.batch.response.OpenApiEventListRes.GyeonggiEventRow;
import gyeonggi.gyeonggifesta.event.entity.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class FaultTolerantEventStepConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final OpenApiEventItemReader openApiEventItemReader;
	private final ApiEventItemProcessor apiEventItemProcessor;
	private final EventItemWriter eventItemWriter;

	@Bean
	public Step faultTolerantProcessApiEventsStep() {
		return new StepBuilder("faultTolerantProcessApiEventsStep", jobRepository)
				.<GyeonggiEventRow, Event>chunk(100, transactionManager)
				.reader(openApiEventItemReader)
				.processor(apiEventItemProcessor)
				.writer(eventItemWriter)
				.faultTolerant()
				.retryLimit(3)
				.retry(Exception.class)
				.skipLimit(10)
				.skip(Exception.class)
				.listener(new EventStepListener())
				.build();
	}
}
