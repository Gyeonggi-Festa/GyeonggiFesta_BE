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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

	private final ScheduleRepository scheduleRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final SecurityUtil securityUtil;

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
	}

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
				scheduleRepository.findByMemberOrderByEventDateAscIdDesc(currentMember, pageable);

		return schedulePage.map(this::toScheduleRes);
	}

	@Override
	@Transactional
	public void updateSchedule(Long scheduleId, UpdateScheduleReq request) {
		Member currentMember = securityUtil.getCurrentMember();

		Schedule schedule = scheduleRepository.findByIdAndMember(scheduleId, currentMember)
				.orElseThrow(() -> new BusinessException(ScheduleErrorCode.NOT_EXIST_SCHEDULE));

		if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDate.now())) {
			throw new BusinessException(GeneralErrorCode.INVALID_INPUT_VALUE);
		}

		schedule.update(request.getTitle(), request.getEventDate(), request.getMemo());
	}

	@Override
	@Transactional
	public void deleteSchedule(Long scheduleId) {
		Member currentMember = securityUtil.getCurrentMember();

		Schedule schedule = scheduleRepository.findByIdAndMember(scheduleId, currentMember)
				.orElseThrow(() -> new BusinessException(ScheduleErrorCode.NOT_EXIST_SCHEDULE));

		scheduleRepository.delete(schedule);
	}

	/**
	 * 동행 채팅방용 일정 자동 등록
	 * - REQUIRES_NEW: 채팅방 생성/참여 트랜잭션과 분리
	 * - 여기서는 예외를 catch 하지 않는다 → 커밋 에러 포함 모든 예외는 호출자(CompanionChatRoomService)에서 처리
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void createScheduleForCompanion(Member member, ChatRoom chatRoom, LocalDate eventDate) {
		// 방어 로직: null / 과거 날짜면 그냥 스킵
		if (member == null || chatRoom == null || eventDate == null) {
			log.warn("동행 일정 자동 생성 스킵 - member/chatRoom/eventDate 중 null: member={}, chatRoom={}, eventDate={}",
					member != null ? member.getId() : null,
					chatRoom != null ? chatRoom.getId() : null,
					eventDate);
			return;
		}

		if (eventDate.isBefore(LocalDate.now())) {
			log.warn("동행 일정 자동 생성 스킵 - 과거 날짜 eventDate={}", eventDate);
			return;
		}

		boolean exists = scheduleRepository.existsByMemberAndChatRoomAndEventDate(
				member, chatRoom, eventDate
		);

		if (exists) {
			log.info("동행 일정 이미 존재 - memberId={}, chatRoomId={}, eventDate={}",
					member.getId(), chatRoom.getId(), eventDate);
			return;
		}

		// 채팅방 이름을 그대로 일정 title 로 사용
		Schedule schedule = Schedule.builder()
				.member(member)
				.chatRoom(chatRoom)
				.title(chatRoom.getName())   // 예: "청춘 페스티벌 같이 갈 사람~"
				.eventDate(eventDate)        // 예: 2025-11-30
				.memo(null)
				.build();

		scheduleRepository.save(schedule);

		log.info("동행 일정 자동 생성 성공 - memberId={}, chatRoomId={}, eventDate={}",
				member.getId(), chatRoom.getId(), eventDate);
	}

	private ScheduleRes toScheduleRes(Schedule schedule) {
		Long chatRoomId = schedule.getChatRoom() != null ? schedule.getChatRoom().getId() : null;

		return ScheduleRes.builder()
				.scheduleId(schedule.getId())
				.title(schedule.getTitle())
				.eventDate(schedule.getEventDate())
				.memo(schedule.getMemo())
				.chatRoomId(chatRoomId)
				.build();
	}
}
