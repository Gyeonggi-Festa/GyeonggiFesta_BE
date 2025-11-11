package gyeonggi.gyeonggifesta.chat.service.chatroom;

import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.chat.entity.ChatRoomMember;
import gyeonggi.gyeonggifesta.chat.exception.ChatErrorCode;
import gyeonggi.gyeonggifesta.chat.repository.ChatRoomMemberRepository;
import gyeonggi.gyeonggifesta.chat.repository.ChatRoomRepository;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 채팅방 관련 유효성 검사를 담당하는 클래스
 * - 중복되는 유효성 검사 로직을 모듈화하여 코드 재사용성 증가
 */
@Component
@RequiredArgsConstructor
public class ChatRoomValidator {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;

	/**
	 * 채팅방 유효성 검사 및 조회
	 * - 존재하는 채팅방인지 확인
	 * - 삭제된 채팅방이 아닌지 확인
	 */
	public ChatRoom validateAndGetChatRoom(Long chatRoomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new BusinessException(ChatErrorCode.NOT_EXIST_CHATROOM));

		if (chatRoom.getDeletedAt() != null) {
			throw new BusinessException(ChatErrorCode.DELETED_CHATROOM);
		}

		return chatRoom;
	}

	/**
	 * 채팅방 멤버 중복 검사
	 * - 이미 채팅방에 가입된 멤버인지 확인
	 */
	public void validateChatRoomMemberNotExists(ChatRoom chatRoom, Member member) {
		if (chatRoomMemberRepository.existsByChatRoomAndMember(chatRoom, member)) {
			throw new BusinessException(ChatErrorCode.EXIST_CHATROOM_MEMBER);
		}
	}

	public void validateChatRoomMemberKicked(ChatRoom chatRoom, Member member) {
		if (chatRoomMemberRepository.existsByChatRoomAndMemberAndKickedAtIsNotNull(chatRoom, member)) {
			throw new BusinessException(ChatErrorCode.KICKED_CHATROOM_MEMBER);
		}
	}


	/**
	 * 채팅방 소유자 권한 검사
	 * - 요청자가 채팅방 소유자인지 확인
	 */
	public void validateOwner(ChatRoom chatRoom, String verifyId) {
		if (!chatRoom.getOwner().getVerifyId().equals(verifyId)) {
			throw new BusinessException(ChatErrorCode.INVALID_ROLE);
		}
	}

	/**
	 * 채팅방 이름 유효성 검사
	 * - 채팅방 이름 길이 제한 확인 (30자 이내)
	 */
	public void validateChatRoomName(String name) {
		if (name == null || name.isEmpty() || name.length() > 30) {
			throw new BusinessException(ChatErrorCode.INVALID_CHATROOM_NAME);
		}
	}

	/**
	 * 채팅방 멤버 조회
	 * - 채팅방 멤버가 존재하는지 확인
	 */
	public ChatRoomMember validateAndGetChatRoomMember(ChatRoom chatRoom, Member member) {
		return chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, member)
			.orElseThrow(() -> new BusinessException(ChatErrorCode.NOT_EXIST_CHATROOM_MEMBER));
	}
}