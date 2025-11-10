package gyeonggi.gyeonggifesta.event.batch.reader;

import gyeonggi.gyeonggifesta.event.dto.batch.response.OpenApiEventListRes.GyeonggiEventRow;
import gyeonggi.gyeonggifesta.event.service.event.EventBatchService;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OpenApiEventItemReader implements ItemReader<GyeonggiEventRow> {

	private final ListItemReader<GyeonggiEventRow> delegate;

	public OpenApiEventItemReader(EventBatchService eventBatchService) {
		// 서비스에서 API 호출하여 전체 행 목록을 가져온다.
		List<GyeonggiEventRow> allRows = eventBatchService.getAllEventRowsFromApi();
		this.delegate = new ListItemReader<>(allRows);
	}

	@Override
	public GyeonggiEventRow read() throws Exception {
		return delegate.read();
	}
}
