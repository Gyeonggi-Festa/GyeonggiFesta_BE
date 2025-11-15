package gyeonggi.gyeonggifesta.schedule.service;

import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.schedule.dto.request.CreateScheduleReq;
import gyeonggi.gyeonggifesta.schedule.dto.request.UpdateScheduleReq;
import gyeonggi.gyeonggifesta.schedule.dto.response.ScheduleRes;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface ScheduleService {

	// 사용자가 직접 일정 등록
	void createSchedule(CreateScheduleReq request);

	// 내 일정 조회
	Page<ScheduleRes> getMySchedules(int page, int size);

	// 내 일정 수정
	void updateSchedule(Long scheduleId, UpdateScheduleReq request);

	// 내 일정 삭제
	void deleteSchedule(Long scheduleId);

	// ===== 동행 채팅방용 내부 API =====

	/**
	 * 동행 채팅방 생성/참여 시 자동 일정 등록
	 * - 중복이면 그냥 무시
	 */
	void createScheduleForCompanion(Member member, ChatRoom chatRoom, LocalDate eventDate);
}
