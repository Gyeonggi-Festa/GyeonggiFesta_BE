package gyeonggi.gyeonggifesta.schedule.service;

import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.chat.repository.ChatRoomRepository;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.schedule.dto.request.CreateScheduleReq;
import gyeonggi.gyeonggifesta.schedule.dto.request.UpdateScheduleReq;
import gyeonggi.gyeonggifesta.schedule.dto.response.ScheduleRes;
import gyeonggi.gyeonggifesta.schedule.entity.Schedule;
import gyeonggi.gyeonggifesta.schedule.exception.ScheduleErrorCode;
import gyeonggi.gyeonggifesta.schedule.repository.ScheduleRepository;
import gyeonggi.gyeonggifesta.util.response.error_code.GeneralErrorCode;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

	private final ScheduleRepository scheduleRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final SecurityUtil securityUtil;

	// ========== 사용자 수동 일정 등록 ==========

	@Override
	@Transactional
	public void createSchedule(CreateScheduleReq request) {
		Member currentMember = securityUtil.getCurrentMember();

		if (request.getTitle() == null || request.getTitle().isBlank()) {
			throw new BusinessException(GeneralErrorCode.INVALID_INPUT_VALUE);
		}
		if (request.getEventDate() == null) {
			throw new BusinessException(GeneralErrorCode.INVALID_INPUT_VALUE);
		}
		if (request.getEventDate().isBefore(LocalDate.now())) {
			throw new BusinessException(GeneralErrorCode.INVALID_INPUT_VALUE);
		}

		ChatRoom chatRoom = null;
		if (request.getChatRoomId() != null) {
			chatRoom = chatRoomRepository.findById(request.getChatRoomId())
					.orElseThrow(() -> new BusinessException(GeneralErrorCode.INVALID_INPUT_VALUE));
		}

		if (chatRoom != null &&
				scheduleRepository.existsByMemberAndChatRoomAndEventDate(
						currentMember, chatRoom, request.getEventDate())) {
			throw new BusinessException(GeneralErrorCode.INVALID_INPUT_VALUE);
		}

		Schedule schedule = Schedule.builder()
				.member(currentMember)
				.chatRoom(chatRoom)
				.title(request.getTitle())
				.eventDate(request.getEventDate())
				.memo(request.getMemo())
				.build();

		scheduleRepository.save(schedule);

		log.info("[수동 일정] 생성 성공 - memberId={}, scheduleId={}, eventDate={}",
				currentMember.getId(), schedule.getId(), schedule.getEventDate());
	}

	// ========== 내 일정 조회 ==========

	@Override
	@Transactional(readOnly = true)
	public Page<ScheduleRes> getMySchedules(int page, int size) {
		Member currentMember = securityUtil.getCurrentMember();

		PageRequest pageable = PageRequest.of(
				page - 1,
				size,
				Sort.by(Sort.Direction.ASC, "eventDate")
						.and(Sort.by(Sort.Direction.DESC, "id"))
		);

		Page<Schedule> schedulePage =
				scheduleRepository.findByMemberIdOrderByEventDateAscIdDesc(currentMember.getId(), pageable);

		log.info("[내 일정 조회] memberId={}, totalElements={}",
				currentMember.getId(), schedulePage.getTotalElements());

		return schedulePage.map(this::toScheduleRes);
	}

	// ========== 내 일정 수정 ==========

	@Override
	@Transactional
	public void updateSchedule(Long scheduleId, UpdateScheduleReq request) {
		Member currentMember = securityUtil.getCurrentMember();

		Schedule schedule = scheduleRepository.findByIdAndMemberId(scheduleId, currentMember.getId())
				.orElseThrow(() -> new BusinessException(ScheduleErrorCode.NOT_EXIST_SCHEDULE));

		if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDate.now())) {
			throw new BusinessException(GeneralErrorCode.INVALID_INPUT_VALUE);
		}

		schedule.update(request.getTitle(), request.getEventDate(), request.getMemo());

		log.info("[내 일정 수정] memberId={}, scheduleId={}", currentMember.getId(), scheduleId);
	}

	// ========== 내 일정 삭제 ==========

	@Override
	@Transactional
	public void deleteSchedule(Long scheduleId) {
		Member currentMember = securityUtil.getCurrentMember();

		Schedule schedule = scheduleRepository.findByIdAndMemberId(scheduleId, currentMember.getId())
				.orElseThrow(() -> new BusinessException(ScheduleErrorCode.NOT_EXIST_SCHEDULE));

		scheduleRepository.delete(schedule);

		log.info("[내 일정 삭제] memberId={}, scheduleId={}", currentMember.getId(), scheduleId);
	}

	// ========== 동행 채팅방 자동 일정 생성 (AFTER_COMMIT에서 호출) ==========

	@Override
	@Transactional
	public void createScheduleForCompanion(Member member, ChatRoom chatRoom, LocalDate eventDate) {

		// 파라미터 방어
		if (member == null || chatRoom == null || eventDate == null) {
			log.warn("[동행 일정] 생성 스킵 - null 파라미터 member={}, chatRoom={}, eventDate={}",
					member != null ? member.getId() : null,
					chatRoom != null ? chatRoom.getId() : null,
					eventDate);
			return;
		}

		if (eventDate.isBefore(LocalDate.now())) {
			log.warn("[동행 일정] 생성 스킵 - 과거 날짜 eventDate={}", eventDate);
			return;
		}

		boolean exists = scheduleRepository.existsByMemberAndChatRoomAndEventDate(
				member, chatRoom, eventDate
		);

		if (exists) {
			log.info("[동행 일정] 이미 존재 - memberId={}, chatRoomId={}, eventDate={}",
					member.getId(), chatRoom.getId(), eventDate);
			return;
		}

		Schedule schedule = Schedule.builder()
				.member(member)
				.chatRoom(chatRoom)
				.title(chatRoom.getName())
				.eventDate(eventDate)
				.memo(null)
				.build();

		scheduleRepository.save(schedule);

		// 디버깅 정보 강화
		log.info("[동행 일정 생성] SUCCESS - scheduleId={}, memberId={}, chatRoomId={}, eventDate={}, title={}",
				schedule.getId(),
				schedule.getMember().getId(),
				schedule.getChatRoom().getId(),
				schedule.getEventDate(),
				schedule.getTitle()
		);
	}


	private ScheduleRes toScheduleRes(Schedule schedule) {
		return ScheduleRes.builder()
				.scheduleId(schedule.getId())
				.title(schedule.getTitle())
				.eventDate(schedule.getEventDate())
				.memo(schedule.getMemo())
				.chatRoomId(schedule.getChatRoom() != null ? schedule.getChatRoom().getId() : null)
				.build();
	}
}
