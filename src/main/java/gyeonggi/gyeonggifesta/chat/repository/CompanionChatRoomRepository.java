package gyeonggi.gyeonggifesta.chat.repository;

import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.chat.entity.CompanionChatRoom;
import gyeonggi.gyeonggifesta.chat.enums.ChatRoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanionChatRoomRepository extends JpaRepository<CompanionChatRoom, Long> {

	/**
	 * 동행찾기 채팅방 목록 조회
	 * - ChatRoom.type = GROUP
	 * - ChatRoom.deletedAt is null
	 * - category 필터는 옵션
	 */
	@Query("select ccr from CompanionChatRoom ccr " +
			"join ccr.chatRoom cr " +
			"where cr.type = :type " +
			"and cr.deletedAt is null " +
			"and (:category is null or :category = '' or cr.category = :category)")
	Page<CompanionChatRoom> findCompanionRooms(
			@Param("type") ChatRoomType type,
			@Param("category") String category,
			Pageable pageable
	);

	/**
	 * 특정 ChatRoom 과 연결된 동행 채팅방 조회
	 * - 채팅방 참여 시 일정 자동 등록에 사용
	 */
	Optional<CompanionChatRoom> findByChatRoom(ChatRoom chatRoom);
}
