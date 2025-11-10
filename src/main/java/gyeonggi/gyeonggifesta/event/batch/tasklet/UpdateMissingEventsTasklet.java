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

		// 2) 중복 식별 키 구성:
		//    제목 + 작성일(WRITNG_DE) + 카테고리 + 기관명(INST_NM) + 종료일
		//    (※ SIGUN_NM은 문화행사 스펙에 없음)
		Set<String> apiEventKeys = apiRows.stream()
				.map(row ->
						(row.getTitle() != null ? row.getTitle() : "") + "_" +
								(row.getWritingDate() != null ? String.valueOf(eventBatchService.convertToLocalDate(row.getWritingDate())) : "") + "_" +
								(row.getCategoryNm() != null ? row.getCategoryNm() : "") + "_" +
								(row.getInstNm() != null ? row.getInstNm() : "") + "_" +
								(row.getEndDate() != null ? String.valueOf(eventBatchService.convertToLocalDate(row.getEndDate())) : "")
				)
				.collect(Collectors.toSet());

		// 3) 종료 상태 업데이트
		int updatedCount = eventBatchService.updateMissingEvents(apiEventKeys);
		log.info("Updated {} events to END status", updatedCount);

		return RepeatStatus.FINISHED;
	}
}
