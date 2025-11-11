package gyeonggi.gyeonggifesta.chat.repository;

import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.chat.entity.ChatRoomMember;
import gyeonggi.gyeonggifesta.chat.enums.ChatRoomMemberStatus;
import gyeonggi.gyeonggifesta.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

	Optional<ChatRoomMember> findByChatRoomAndMember(ChatRoom chatRoom, Member member);

	List<ChatRoomMember> findAllByMember(Member member);
	boolean existsByChatRoomAndMember(ChatRoom chatRoom, Member member);
	boolean existsByChatRoomAndMemberAndKickedAtIsNotNull(ChatRoom chatRoom, Member member);
	boolean existsByChatRoomAndMemberAndStatusNotAndKickedAtIsNull(
		ChatRoom chatRoom, Member member, ChatRoomMemberStatus status);
}
