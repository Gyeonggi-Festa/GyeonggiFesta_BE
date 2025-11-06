package gyeonggi.gyeonggifesta.chat.service.chatroom;

import gyeonggi.gyeonggifesta.chat.dto.response.ChatRoomRes;
import gyeonggi.gyeonggifesta.chat.dto.response.MyChatRoomRes;
import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.chat.entity.ChatRoomMember;
import gyeonggi.gyeonggifesta.chat.repository.ChatMessageRepository;
import gyeonggi.gyeonggifesta.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 채팅방 관련 DTO 변환 담당 클래스
 * - 엔티티를 DTO로 변환하는 로직 모듈화
 */
@Component
@RequiredArgsConstructor
public class ChatRoomDtoMapper {

	private final ChatMessageRepository chatMessageRepository;

	/**
	 * 내 채팅방 정보 DTO 변환
	 */
	public MyChatRoomRes toMyChatRoomRes(ChatRoom chatRoom, Member currentMember) {
		int unreadCount = calculateUnreadCount(chatRoom, currentMember);

		Pair<LocalDateTime, String> lastMessageInfo = getLastMessageInfo(chatRoom);
		LocalDateTime lastMsgTime = lastMessageInfo.getFirst();
		String lastMessageText = lastMessageInfo.getSecond();
		String formattedLastMsgTime = formatLastMessageTime(lastMsgTime);

		return MyChatRoomRes.builder()
			.chatRoomId(chatRoom.getId())
			.name(chatRoom.getName())
			.participation(chatRoom.getChatRoomMembers().size())
			.type(chatRoom.getType())
			.createdFrom(chatRoom.getFromType())
			.createdFromId(chatRoom.getFromId())
			.notReadMessageCount(unreadCount)
			.lastMessageTime(formattedLastMsgTime)
			.lastMessageText(lastMessageText)
			.build();
	}

	/**
	 * 전체 채팅방 정보 DTO 변환
	 */
	public ChatRoomRes toChatRoomRes(ChatRoom chatRoom) {
		return ChatRoomRes.builder()
			.chatRoomId(chatRoom.getId())
			.name(chatRoom.getName())
			.participation(chatRoom.getChatRoomMembers().size())
			.information(chatRoom.getInformation())
			.category(chatRoom.getCategory())
			.build();
	}

	/**
	 * 읽지 않은 메시지 수 계산
	 */
	private int calculateUnreadCount(ChatRoom chatRoom, Member currentMember) {
		ChatRoomMember memberData = chatRoom.getChatRoomMembers().stream()
			.filter(crm -> crm.getMember().getId().equals(currentMember.getId()))
			.findFirst()
			.orElse(null);

		int unreadCount = 0;
		if (memberData != null) {
			LocalDateTime lastReadAt =
				memberData.getLastReadAt() != null ? memberData.getLastReadAt() : memberData.getJoinedAt();
			unreadCount = chatMessageRepository.countByChatRoomAndCreatedAtAfter(chatRoom, lastReadAt);
		}
		return unreadCount;
	}

	private Pair<LocalDateTime, String> getLastMessageInfo(ChatRoom chatRoom) {
		Pageable topOne = PageRequest.of(0, 1);
		List<Map<String, Object>> result = chatMessageRepository.findLastMessageInfoByChatRoomId(chatRoom.getId(), topOne);

		if (result.isEmpty()) {
			return Pair.of(chatRoom.getCreatedAt(), "");
		}

		Map<String, Object> lastMessage = result.get(0);
		LocalDateTime createdAt = (LocalDateTime) lastMessage.get("createdAt");
		String content = (String) lastMessage.get("content");

		return Pair.of(createdAt, content);
	}

	/**
	 * 메시지 시간 포맷팅
	 * - 오늘: 오전/오후 시간 (오전 09:30)
	 * - 어제: "어제"
	 * - 같은 해 다른 날: "M월 d일" (4월 3일)
	 * - 다른 해: "yyyy. MM. dd" (2023. 04. 03)
	 */
	private String formatLastMessageTime(LocalDateTime messageTime) {
		if (messageTime == null) {
			return "";
		}

		LocalDate today = LocalDate.now();
		LocalDate messageDate = messageTime.toLocalDate();

		if (messageDate.equals(today)) {
			// 오늘인 경우: 오전/오후 시간 포맷
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN);
			return messageTime.format(formatter);
		} else if (messageDate.equals(today.minusDays(1))) {
			// 어제인 경우
			return "어제";
		} else if (messageDate.getYear() == today.getYear()) {
			// 같은 연도 내이면: "월 일" 형식
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN);
			return messageTime.format(formatter);
		} else {
			// 이전 연도이면: "yyyy. MM. dd" 형식
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd", Locale.KOREAN);
			return messageTime.format(formatter);
		}
	}
}