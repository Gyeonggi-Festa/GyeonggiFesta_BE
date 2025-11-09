package gyeonggi.gyeonggifesta.event.batch.processor;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventSyncDto;
import gyeonggi.gyeonggifesta.event.entity.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSyncProcessor implements ItemProcessor<Event, EventSyncDto> {

	@Override
	public EventSyncDto process(Event event) {
		return EventSyncDto.fromEntity(event);
	}
}