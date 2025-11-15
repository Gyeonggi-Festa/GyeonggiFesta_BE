package gyeonggi.gyeonggifesta.chat.service.chatroom;

import gyeonggi.gyeonggifesta.chat.dto.request.chatroom.CreateChatRoomReq;
import gyeonggi.gyeonggifesta.chat.dto.request.chatroom.InviteChatRoomReq;
import gyeonggi.gyeonggifesta.chat.dto.request.chatroom.KickChatRoomReq;
import gyeonggi.gyeonggifesta.chat.dto.request.chatroom.UpdateChatRoomReq;
import gyeonggi.gyeonggifesta.chat.dto.response.ChatRoomRes;
import gyeonggi.gyeonggifesta.chat.dto.response.MyChatRoomRes;
import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.chat.exception.ChatErrorCode;
import gyeonggi.gyeonggifesta.chat.repository.ChatRoomRepository;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * 채팅방 서비스 파사드 클래스
 * - 기능별로 분리된 서비스들을 조합하여 사용
 * - 컨트롤러에서 단일 진입점으로 사용할 수 있도록 함
 */
@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

	private final SecurityUtil securityUtil;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomManagementService managementService;
	private final ChatRoomMembershipService membershipService;
	private final ChatRoomQueryService queryService;

	@Override
	public void createChatRoom(CreateChatRoomReq request) {
		managementService.createChatRoom(request);
	}

	@Override
	public void exitChatRoom(Long chatRoomId, String verifyId) {
		membershipService.exitChatRoom(chatRoomId, verifyId);
	}

	@Override
	public void removeChatRoom(Long chatRoomId, String verifyId) {
		managementService.removeChatRoom(chatRoomId, verifyId);
	}

	@Override
	public void updateChatRoomName(UpdateChatRoomReq request, String verifyId) {
		managementService.updateChatRoomName(request, verifyId);
	}

	@Override
	public void joinChatRoom(Long chatRoomId) {
		membershipService.joinChatRoom(chatRoomId);
	}

	@Override
	public void inviteChatRoom(InviteChatRoomReq request) {
		membershipService.inviteChatRoom(request);
	}

	@Override
	public Page<MyChatRoomRes> listMyChatRooms(String verifyId, int page, int size, String keyword) {
		return queryService.listMyChatRooms(verifyId, page, size, keyword);
	}

	@Override
	public Page<ChatRoomRes> listAllChatRooms(int page, int size, String keyword) {
		return queryService.listAllChatRooms(page, size, keyword);
	}

	@Override
	public void kickChatRoomMember(KickChatRoomReq request, String verifyId) {
		managementService.kickChatRoomMember(request, verifyId);
	}

	@Override
	public Page<ChatRoomRes> listChatRoomsByCategory(int page, int size, String category) {
		return queryService.listChatRoomsByCategory(page, size, category);
	}

	@Override
	public boolean isChatRoomOwner(Long chatRoomId) {
		Member currentMember = securityUtil.getCurrentMember();
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new BusinessException(ChatErrorCode.NOT_EXIST_CHATROOM));
		return chatRoom.getOwner().equals(currentMember);
	}
}