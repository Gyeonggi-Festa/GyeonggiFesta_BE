package gyeonggi.gyeonggifesta.event.batch.reader;

import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.HashMap;

@Slf4j
@Configuration
public class EventSyncReader implements ItemReader<Event> {

	private static final int CHUNK_SIZE = 100;
	private final EventRepository eventRepository;
	private RepositoryItemReader<Event> delegate;

	public EventSyncReader(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
		this.delegate = createDelegate();
	}

	private RepositoryItemReader<Event> createDelegate() {
		log.info("이벤트 동기화를 위한 모든 데이터 조회 시작");

		// 정렬 설정
		HashMap<String, Sort.Direction> sorts = new HashMap<>();
		sorts.put("id", Sort.Direction.ASC);

		return new RepositoryItemReaderBuilder<Event>()
			.name("eventSyncReader")
			.repository(eventRepository)
			.methodName("findAll") // 모든 이벤트 조회
			.pageSize(CHUNK_SIZE)
			.sorts(sorts)
			.build();
	}

	@Override
	public Event read() throws Exception {
		return delegate.read();
	}
}