package gyeonggi.gyeonggifesta.event.batch.writer;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventSyncDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSyncWriter implements ItemWriter<EventSyncDto> {

	private final RestTemplate restTemplate;
	private final List<EventSyncDto> allEvents = new ArrayList<>();

	@Value("${request-url.ml-server}")
	private String API_URL;

	@Override
	public void write(Chunk<? extends EventSyncDto> chunk) throws Exception {
		// 모든 이벤트를 리스트에 추가
		allEvents.addAll(chunk.getItems());
		log.info("이벤트 추가: {} 개, 현재 총 {} 개", chunk.getItems().size(), allEvents.size());
	}

	public void sendAllEvents() {
		if (allEvents.isEmpty()) {
			log.info("전송할 이벤트가 없습니다.");
			return;
		}

		log.info("이벤트 동기화 API 호출 시작: 총 {} 개의 이벤트 전송", allEvents.size());

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<List<EventSyncDto>> requestEntity = new HttpEntity<>(allEvents, headers);

			// RestTemplate를 사용하여 API 호출
			restTemplate.postForObject(API_URL + "/event-sync", requestEntity, String.class);

			log.info("이벤트 동기화 API 호출 완료: {} 개의 이벤트 전송 성공", allEvents.size());

			// 전송 후 리스트 비우기
			allEvents.clear();
		} catch (Exception e) {
			log.error("이벤트 동기화 API 호출 실패: {}", e.getMessage(), e);
			throw e;
		}
	}
}
