package gyeonggi.gyeonggifesta.chat.dto.request.chatroom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 동행찾기 전용 채팅방 생성 요청 DTO
 * - 클라이언트는 기존 바디에 eventDate만 추가해서 보내면 됨
 *   {
 *     "name": "채팅방 테스트",
 *     "type": "GROUP",          // 들어와도 무시됨
 *     "information": "설명글",
 *     "category": "연극",
 *     "eventDate": "2025-11-30"
 *   }
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanionChatRoomReq {

	private String name;
	private String information;
	private String category;
	private LocalDate eventDate; // 축제 날짜 (필수)
}
