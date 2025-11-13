package gyeonggi.gyeonggifesta.chat.repository;

import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.chat.entity.CompanionChatRoom;
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

	/**
	 * 내가 참여한 채팅방 목록 조회 (EXIT 제외)
	 * - 동행/일반 모두 포함
	 */
	@Query("select cr.id from ChatRoom cr " +
			"join cr.chatRoomMembers crm " +
			"where crm.member = :member " +
			"and crm.status != 'EXIT' " +
			"and cr.name like concat('%', :keyword, '%') " +
			"and cr.deletedAt is null " +
			"group by cr.id")
	Page<Long> findChatRoomIdsByMemberAndKeyword(
			@Param("member") Member member,
			@Param("keyword") String keyword,
			Pageable pageable
	);

	/**
	 * ID 리스트 기반 방 + 멤버 상세 조회
	 */
	@Query("select distinct cr from ChatRoom cr " +
			"left join fetch cr.chatRoomMembers crm " +
			"where cr.id in :ids " +
			"and cr.deletedAt is null")
	List<ChatRoom> findChatRoomsByIdInWithMembers(@Param("ids") List<Long> ids);


	/* ============================================================
		아래 3개는 "일반 오픈채팅" 조회용
		CompanionChatRoom이 연결된 동행방은 전부 제외
	   ============================================================ */


	/**
	 * 일반 오픈채팅 목록 조회 (이름 검색)
	 * - 동행방 제외
	 */
	@Query("select cr from ChatRoom cr " +
			"left join CompanionChatRoom ccr on ccr.chatRoom = cr " +
			"where cr.type = :type " +
			"and lower(cr.name) like lower(concat('%', :keyword, '%')) " +
			"and cr.deletedAt is null " +
			"and ccr.id is null")
	Page<ChatRoom> findAllByTypeAndNameContainingIgnoreCaseAndDeletedAtIsNull(
			@Param("type") ChatRoomType type,
			@Param("keyword") String keyword,
			Pageable pageable
	);

	/**
	 * 일반 오픈채팅 목록 조회 (이름 / 카테고리 검색)
	 * - 동행방 제외
	 */
	@Query("select cr from ChatRoom cr " +
			"left join CompanionChatRoom ccr on ccr.chatRoom = cr " +
			"where cr.type = :type " +
			"and (lower(cr.name) like lower(concat('%', :keyword, '%')) " +
			"     or lower(cr.category) like lower(concat('%', :keyword, '%'))) " +
			"and cr.deletedAt is null " +
			"and ccr.id is null")
	Page<ChatRoom> findAllByTypeAndKeywordInNameOrCategory(
			@Param("type") ChatRoomType type,
			@Param("keyword") String keyword,
			Pageable pageable
	);

	/**
	 * 일반 오픈채팅 목록 조회 (카테고리 필터)
	 * - 동행방 제외
	 */
	@Query("select cr from ChatRoom cr " +
			"left join CompanionChatRoom ccr on ccr.chatRoom = cr " +
			"where cr.type = :type " +
			"and cr.category = :category " +
			"and cr.deletedAt is null " +
			"and ccr.id is null")
	Page<ChatRoom> findAllByTypeAndCategory(
			@Param("type") ChatRoomType type,
			@Param("category") String category,
			Pageable pageable
	);

}
