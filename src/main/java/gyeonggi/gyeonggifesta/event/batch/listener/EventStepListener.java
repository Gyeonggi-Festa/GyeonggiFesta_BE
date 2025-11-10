package gyeonggi.gyeonggifesta.event.batch.listener;

import gyeonggi.gyeonggifesta.event.dto.batch.response.OpenApiEventListRes.GyeonggiEventRow;
import gyeonggi.gyeonggifesta.event.entity.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemProcessListener;

@Slf4j
public class EventStepListener implements ItemProcessListener<GyeonggiEventRow, Event> {

	@Override
	public void beforeProcess(GyeonggiEventRow item) {
		// 필요하면 로그 추가 가능
	}

	@Override
	public void afterProcess(GyeonggiEventRow item, Event result) {
		// 필요하면 처리 완료 로그 추가 가능
	}

	@Override
	public void onProcessError(GyeonggiEventRow item, Exception e) {
		log.error("경기도 축제 불러오기 중 오류 - 제목: {} | 원인: {}",
				item.getTitle(), e.getMessage(), e);
	}
}
