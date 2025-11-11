package gyeonggi.gyeonggifesta.event.batch.tasklet;

import gyeonggi.gyeonggifesta.event.dto.batch.response.OpenApiEventListRes.GyeonggiEventRow;
import gyeonggi.gyeonggifesta.event.service.event.EventBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static gyeonggi.gyeonggifesta.event.util.TextNormalizer.norm;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class UpdateMissingEventsTasklet implements Tasklet {

	private final EventBatchService eventBatchService;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		// 1) API에서 최신 이벤트 목록 조회
		List<GyeonggiEventRow> apiRows = eventBatchService.getAllEventRowsFromApi();

		// 2) 중복 식별 키 구성 (DB 유니크와 동일 조합):
		//    제목 + WRITNG_DE(등록일) + 카테고리 + 기관명(HOST 우선) + 종료일
		Set<String> apiEventKeys = apiRows.stream()
				.map(row -> {
					String title = norm(row.getTitle());
					String cat   = norm(row.getCategoryNm());
					String org   = norm(firstNonNull(row.getHostInstNm(), row.getInstNm()));
					String reg   = row.getWritingDate() == null ? "" :
							String.valueOf(eventBatchService.convertToLocalDate(row.getWritingDate()));
					String end   = row.getEndDate() == null ? "" :
							String.valueOf(eventBatchService.convertToLocalDate(row.getEndDate()));
					return String.join("|", title, reg, cat, org, end);
				})
				.collect(Collectors.toSet());

		// 3) 종료 상태 업데이트
		int updatedCount = eventBatchService.updateMissingEvents(apiEventKeys);
		log.info("Updated {} events to END status", updatedCount);

		return RepeatStatus.FINISHED;
	}

	private static String firstNonNull(String a, String b) {
		return (a != null && !a.isBlank()) ? a : (b != null ? b : null);
	}
}
