package gyeonggi.gyeonggifesta.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 동행찾기 채팅방 목록 응답 DTO
 * - 프론트에서 바로 카드 리스트에 쓰기 좋은 형태
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanionChatRoomRes {

	private Long chatRoomId;      // 실제 채팅방 ID (웹소켓, 메시지 조회 등에서 사용)
	private String name;
	private int participation;
	private String information;
	private String category;
	private LocalDate eventDate;  // 축제 날짜
}
