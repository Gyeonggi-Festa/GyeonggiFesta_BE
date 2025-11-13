package gyeonggi.gyeonggifesta.chat.event;

import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.chat.repository.ChatRoomRepository;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.member.repository.MemberRepository;
import gyeonggi.gyeonggifesta.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanionScheduleEventListener {

	private final MemberRepository memberRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ScheduleService scheduleService;

	/**
	 * 동행방 생성 트랜잭션이 "성공적으로 커밋된 이후"에만 실행
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleCompanionChatRoomCreated(CompanionChatRoomCreatedEvent event) {

		log.info("[동행 일정] 이벤트 수신 - memberId={}, chatRoomId={}, eventDate={}",
				event.getMemberId(), event.getChatRoomId(), event.getEventDate());

		// 혹시라도 null 이 들어온 경우 방어
		if (event.getChatRoomId() == null || event.getMemberId() == null) {
			log.warn("[동행 일정] 생성 스킵 - null ID 포함 event={}", event);
			return;
		}

		try {
			Optional<Member> memberOpt = memberRepository.findById(event.getMemberId());
			Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findById(event.getChatRoomId());

			if (memberOpt.isEmpty() || chatRoomOpt.isEmpty()) {
				log.warn("[동행 일정] 생성 스킵 - member/chatRoom 미존재 memberId={}, chatRoomId={}",
						event.getMemberId(), event.getChatRoomId());
				return;
			}

			Member member = memberOpt.get();
			ChatRoom chatRoom = chatRoomOpt.get();

			log.info("[동행 일정] 생성 호출 - memberId={}, chatRoomId={}, eventDate={}",
					member.getId(), chatRoom.getId(), event.getEventDate());

			scheduleService.createScheduleForCompanion(
					member,
					chatRoom,
					event.getEventDate()
			);

			log.info("[동행 일정] 생성 완료 - memberId={}, chatRoomId={}, eventDate={}",
					member.getId(), chatRoom.getId(), event.getEventDate());

		} catch (Exception e) {
			log.error("[동행 일정] 자동 생성 실패 (채팅방은 이미 생성됨). event={}", event, e);
		}
	}
}
