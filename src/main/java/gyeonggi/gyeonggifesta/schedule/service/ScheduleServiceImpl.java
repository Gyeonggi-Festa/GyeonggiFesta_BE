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

	// ====== 수동 일정 생성 (기존과 동일) ======
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
	 * 동행 채팅방 자동 일정 생성 – 최종 버전
	 *
	 * - REQUIRES_NEW: 채팅방 생성 트랜잭션과 완전히 분리
	 * - 내부에서 save + flush 를 try/catch 로 감싸서
	 *   제약조건/락 오류 포함 모든 예외를 이 메서드 안에서 소화
	 * - 따라서 일정 생성 실패해도 채팅방 생성/참여는 100% 커밋된다.
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void createScheduleForCompanion(Member member, ChatRoom chatRoom, LocalDate eventDate) {

		try {
			// --- 1) 파라미터 방어 ---
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

			// --- 2) 중복 방지 ---
			boolean exists = scheduleRepository.existsByMemberAndChatRoomAndEventDate(
					member, chatRoom, eventDate);

			if (exists) {
				log.info("[동행 일정] 이미 존재 - memberId={}, chatRoomId={}, eventDate={}",
						member.getId(), chatRoom.getId(), eventDate);
				return;
			}

			// --- 3) 일정 생성 ---
			Schedule schedule = Schedule.builder()
					.member(member)
					.chatRoom(chatRoom)
					.title(chatRoom.getName())  // 방 이름을 일정 제목으로 사용
					.eventDate(eventDate)
					.memo(null)
					.build();

			scheduleRepository.save(schedule);
			// 커밋 시점이 아니라 여기서 insert/제약조건/락 에러를 바로 감지
			scheduleRepository.flush();

			log.info("[동행 일정] 자동 생성 성공 - memberId={}, chatRoomId={}, eventDate={}",
					member.getId(), chatRoom.getId(), eventDate);

		} catch (Exception e) {
			// 여기서 어떤 예외가 나더라도, 이 REQUIRES_NEW 트랜잭션만 롤백되고
			//  채팅방 생성 트랜잭션은 절대 건드리지 않는다.
			log.error("[동행 일정] 자동 생성 실패(채팅방에는 영향 없음) memberId={}, chatRoomId={}, eventDate={}",
					member != null ? member.getId() : null,
					chatRoom != null ? chatRoom.getId() : null,
					eventDate, e);
		}
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
