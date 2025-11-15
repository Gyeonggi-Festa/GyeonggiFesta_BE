package gyeonggi.gyeonggifesta.chat.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 동행 채팅방이 성공적으로 생성된 후에
 * 일정 자동 생성 등을 하기 위해 발행하는 도메인 이벤트
 */
@Getter
@AllArgsConstructor
public class CompanionChatRoomCreatedEvent {

	private final Long memberId;
	private final Long chatRoomId;
	private final LocalDate eventDate;
}
