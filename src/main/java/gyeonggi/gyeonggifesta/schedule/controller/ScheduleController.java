package gyeonggi.gyeonggifesta.schedule.controller;

import gyeonggi.gyeonggifesta.schedule.dto.request.CreateScheduleReq;
import gyeonggi.gyeonggifesta.schedule.dto.request.UpdateScheduleReq;
import gyeonggi.gyeonggifesta.schedule.dto.response.ScheduleRes;
import gyeonggi.gyeonggifesta.schedule.service.ScheduleService;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user/schedules")
public class ScheduleController {

	private final ScheduleService scheduleService;

	/**
	 * 일정 수동 등록
	 */
	@PostMapping
	public ResponseEntity<Response<Void>> createSchedule(@RequestBody CreateScheduleReq request) {
		scheduleService.createSchedule(request);
		return Response.ok().toResponseEntity();
	}

	/**
	 * 내 일정 전체 조회
	 */
	@GetMapping
	public ResponseEntity<Response<Page<ScheduleRes>>> getMySchedules(
		@RequestParam(defaultValue = "1", required = false) int page,
		@RequestParam(defaultValue = "10", required = false) int size
	) {
		Page<ScheduleRes> schedules = scheduleService.getMySchedules(page, size);
		return Response.ok(schedules).toResponseEntity();
	}

	/**
	 * 내 일정 수정
	 */
	@PatchMapping("/{scheduleId}")
	public ResponseEntity<Response<Void>> updateSchedule(
		@PathVariable Long scheduleId,
		@RequestBody UpdateScheduleReq request
	) {
		scheduleService.updateSchedule(scheduleId, request);
		return Response.ok().toResponseEntity();
	}

	/**
	 * 내 일정 삭제
	 */
	@DeleteMapping("/{scheduleId}")
	public ResponseEntity<Response<Void>> deleteSchedule(@PathVariable Long scheduleId) {
		scheduleService.deleteSchedule(scheduleId);
		return Response.ok().toResponseEntity();
	}
}
