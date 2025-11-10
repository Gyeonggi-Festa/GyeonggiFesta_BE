package gyeonggi.gyeonggifesta.chat.service.chatting;

import gyeonggi.gyeonggifesta.chat.dto.request.chatting.request.ChatMessageRequest;
import gyeonggi.gyeonggifesta.chat.dto.request.chatting.response.ChatMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatMessageService {

	/**
	 * 메시지 전송
	 *
	 * @param messageRequest 전송할 메시지 정보
	 * @param verifyId 발신자 인증ID
	 * @return 저장된 메시지 응답
	 */
	ChatMessageResponse sendMessage(ChatMessageRequest messageRequest, String verifyId);

	/**
	 * 채팅방 메시지 목록 조회
	 *
	 * @param chatRoomId 채팅방 ID
	 * @param verifyId 요청자 인증ID
	 * @param pageable 페이징 정보
	 * @return 채팅 메시지 페이지
	 */
	Page<ChatMessageResponse> getMessages(Long chatRoomId, String verifyId, Pageable pageable);

	/**
	 * 메시지 삭제 (soft delete)
	 *
	 * @param messageId 메시지 ID
	 * @param verifyId 요청자 인증ID
	 */
	void deleteMessage(Long messageId, String verifyId);

	/**
	 * 채팅방 입장 처리
	 *
	 * @param roomId 채팅방 ID
	 * @param verifyId 사용자 인증ID
	 */
	void enterChatRoom(Long roomId, String verifyId);

	void leaveChatRoom(Long roomId, String verifyId);

	/**
	 * 메시지 읽음 처리
	 *
	 * @param roomId 채팅방 ID
	 * @param verifyId 사용자 인증ID
	 */
	void markAsRead(Long roomId, String verifyId);

	/**
	 * 새 메시지 여부 확인
	 *
	 * @param verifyId 사용자 인증ID
	 * @return 새 메시지가 있는 채팅방 ID 목록
	 */
	List<Long> getNewMessageRooms(String verifyId);

	/**
	 * 특정 메시지 ID 이전의 메시지 목록 조회 (무한 스크롤용)
	 *
	 * @param chatRoomId 채팅방 ID
	 * @param lastMessageId 마지막으로 로드된 메시지 ID
	 * @param verifyId 요청자 인증ID
	 * @param pageable 페이징 정보
	 * @return 채팅 메시지 페이지
	 */
	Page<ChatMessageResponse> getMessagesBefore(Long chatRoomId, Long lastMessageId, String verifyId, Pageable pageable);
}
