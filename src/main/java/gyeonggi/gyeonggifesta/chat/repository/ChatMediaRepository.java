package gyeonggi.gyeonggifesta.chat.repository;

import gyeonggi.gyeonggifesta.chat.entity.ChatMedia;
import gyeonggi.gyeonggifesta.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMediaRepository extends JpaRepository<ChatMedia, Long> {

	/**
	 * 특정 채팅 메시지에 연결된 모든 미디어 조회
	 */
	List<ChatMedia> findByChatMessage(ChatMessage chatMessage);

	/**
	 * 특정 채팅 메시지 ID에 연결된 모든 미디어 조회
	 */
	List<ChatMedia> findByChatMessageId(Long messageId);
}
