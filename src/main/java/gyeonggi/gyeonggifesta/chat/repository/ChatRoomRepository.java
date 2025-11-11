package gyeonggi.gyeonggifesta.chat.repository;

import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.chat.enums.ChatRoomType;
import gyeonggi.gyeonggifesta.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	// 페이징을 위한 쿼리 - ID만 가져옴 (EXIT 상태가 아닌 멤버만)
	@Query("select cr.id from ChatRoom cr " +
		"join cr.chatRoomMembers crm " +
		"where crm.member = :member " +
		"and crm.status != 'EXIT' " +  // EXIT 상태가 아닌 멤버만 포함
		"and cr.name like CONCAT('%', :keyword, '%') " +
		"and cr.deletedAt is null " +
		"group by cr.id")
	Page<Long> findChatRoomIdsByMemberAndKeyword(@Param("member") Member member,
		@Param("keyword") String keyword,
		Pageable pageable);

	// ID 리스트로 채팅방과 멤버 정보를 한 번에 조회
	@Query("select distinct cr from ChatRoom cr " +
		"left join fetch cr.chatRoomMembers crm " +
		"where cr.id in :ids " +
		"and cr.deletedAt is null")
	List<ChatRoom> findChatRoomsByIdInWithMembers(@Param("ids") List<Long> ids);

	Page<ChatRoom> findAllByTypeAndNameContainingIgnoreCaseAndDeletedAtIsNull(ChatRoomType type, String keyword, Pageable pageable);

	// 이름 또는 카테고리로 검색하는 메서드
	@Query("select cr from ChatRoom cr " +
		"where cr.type = :type " +
		"and (cr.name like CONCAT('%', :keyword, '%') OR " +
		"     cr.category like CONCAT('%', :keyword, '%')) " +
		"and cr.deletedAt is null")
	Page<ChatRoom> findAllByTypeAndKeywordInNameOrCategory(
		@Param("type") ChatRoomType type,
		@Param("keyword") String keyword,
		Pageable pageable);

	@Query("select cr from ChatRoom cr " +
		"where cr.type = :type " +
		"and cr.category = :category " +
		"and cr.deletedAt is null")
	Page<ChatRoom> findAllByTypeAndCategory(
		@Param("type") ChatRoomType type,
		@Param("category") String category,
		Pageable pageable);
}
