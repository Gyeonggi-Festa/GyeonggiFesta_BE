package gyeonggi.gyeonggifesta.event.batch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 개발/운영에서 수동으로 배치 잡을 트리거하기 위한 REST 컨트롤러.
 * POST /batch/event-sync/run  호출 시 eventSyncJob 실행.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/batch")
public class BatchTriggerController {

    private final JobLauncher jobLauncher;

    @GetMapping("/event-sync/run")
    public ResponseEntity<String> runEventSyncJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                // 매 실행마다 다른 파라미터를 넣어 재실행 가능하도록 보장
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        return ResponseEntity.ok("eventSyncJob launched");
    }
}
