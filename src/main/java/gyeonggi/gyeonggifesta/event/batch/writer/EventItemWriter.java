package gyeonggi.gyeonggifesta.event.batch.writer;

import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.service.event.EventBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventItemWriter implements ItemWriter<Event> {

	private final EventBatchService eventBatchService;

	@Override
	public void write(Chunk<? extends Event> chunk) {
		log.info("[WRITER] chunk size={}", chunk.getItems().size());
		for (Event event : chunk.getItems()) {
			log.info("[WRITER] saving title='{}' category='{}' org='{}' start={} end={} free={}",
					event.getTitle(),
					event.getCodename(),
					event.getOrgName(),
					event.getStartDate(),
					event.getEndDate(),
					event.getIsFree());
			eventBatchService.saveEvent(event);
		}
	}
}
