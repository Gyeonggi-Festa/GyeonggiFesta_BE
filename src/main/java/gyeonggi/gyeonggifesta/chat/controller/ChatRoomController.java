package gyeonggi.gyeonggifesta.chat.controller;

import gyeonggi.gyeonggifesta.auth.custom.CustomUserDetails;
import gyeonggi.gyeonggifesta.chat.dto.request.chatroom.CreateChatRoomReq;
import gyeonggi.gyeonggifesta.chat.dto.request.chatroom.InviteChatRoomReq;
import gyeonggi.gyeonggifesta.chat.dto.request.chatroom.KickChatRoomReq;
import gyeonggi.gyeonggifesta.chat.dto.request.chatroom.UpdateChatRoomReq;
import gyeonggi.gyeonggifesta.chat.dto.response.CompanionChatRoomRes;
import gyeonggi.gyeonggifesta.chat.dto.response.ChatRoomRes;
import gyeonggi.gyeonggifesta.chat.dto.response.MyChatRoomRes;
import gyeonggi.gyeonggifesta.chat.service.chatroom.ChatRoomService;
import gyeonggi.gyeonggifesta.chat.service.chatroom.CompanionChatRoomService;
import gyeonggi.gyeonggifesta.chat.dto.request.chatroom.CreateCompanionChatRoomReq;
import gyeonggi.gyeonggifesta.util.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user")
public class ChatRoomController {

	private final ChatRoomService chatRoomService;
	private final CompanionChatRoomService companionChatRoomService;

	/**
	 * 채팅방 생성
	 */
	@PostMapping("/chatrooms")
	public ResponseEntity<Response<Void>> createChatRoom(@RequestBody CreateChatRoomReq request) {
		chatRoomService.createChatRoom(request);
		return Response.ok().toResponseEntity();
	}

	/**
	 * 동행찾기 전용 채팅방 생성
	 *
	 * POST /api/auth/user/companion-chatrooms
	 * {
	 *   "name": "채팅방 테스트222~~~~~~~",
	 *   "type": "GROUP",          // 보내도 무시됨
	 *   "information": "설명글입니다~~~~~",
	 *   "category": "연극",
	 *   "eventDate": "2025-11-30"
	 * }
	 */
	@PostMapping("/companion-chatrooms")
	public ResponseEntity<Response<Void>> createCompanionChatRoom(
			@RequestBody CreateCompanionChatRoomReq request) {

		companionChatRoomService.createCompanionChatRoom(request);
		return Response.ok().toResponseEntity();
	}

	/**
	 * 채팅방 탈퇴
	 */
	@DeleteMapping("/chatrooms/{chatRoomId}/exit")
	public ResponseEntity<Response<Void>> exitChatRoom(@PathVariable Long chatRoomId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		chatRoomService.exitChatRoom(chatRoomId, userDetails.getName());
		return Response.ok().toResponseEntity();
	}

	/**
	 * 채팅방 삭제 (soft delete)
	 */
	@DeleteMapping("/chatrooms/{chatRoomId}")
	public ResponseEntity<Response<Void>> removeChatRoom(@PathVariable Long chatRoomId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		chatRoomService.removeChatRoom(chatRoomId, userDetails.getName());
		return Response.ok().toResponseEntity();
	}

	/**
	 * 채팅방 이름 변경 (방장만 가능)
	 */
	@PatchMapping("/chatrooms/name")
	public ResponseEntity<Response<Void>> updateChatRoomName(@RequestBody UpdateChatRoomReq request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		chatRoomService.updateChatRoomName(request, userDetails.getName());
		return Response.ok().toResponseEntity();
	}

	/**
	 * 채팅방 참여 (join)
	 */
	@PostMapping("/chatrooms/{chatRoomId}/join")
	public ResponseEntity<Response<Void>> joinChatRoom(@PathVariable Long chatRoomId) {
		chatRoomService.joinChatRoom(chatRoomId);
		return Response.ok().toResponseEntity();
	}

	/**
	 * 채팅방 초대 (이메일, 고유번호)
	 */
	@PostMapping("/chatrooms/invite")
	public ResponseEntity<Response<Void>> inviteChatRoom(@RequestBody @Valid InviteChatRoomReq request) {
		chatRoomService.inviteChatRoom(request);
		return Response.ok().toResponseEntity();
	}

	/**
	 * 채팅방 강퇴
	 * @param reqeust
	 * @param userDetails
	 * @return
	 */
	@DeleteMapping("/chatrooms/kick")
	public ResponseEntity<Response<Void>> kickChatRoomMember(
		@RequestBody KickChatRoomReq reqeust,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		chatRoomService.kickChatRoomMember(reqeust, userDetails.getName());

		return Response.ok().toResponseEntity();
	}

	/**
	 * 채팅방 목록 조회 (verifyId에 해당하는 사용자의 참여 채팅방 목록)
	 */
	@GetMapping("/my-chatrooms")
	public ResponseEntity<Response<Page<MyChatRoomRes>>> listMyChatRooms(
		@RequestParam(defaultValue = "1", required = false) int page,
		@RequestParam(defaultValue = "10", required = false) int size,
		@RequestParam(required = false) String keyword,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		Page<MyChatRoomRes> myChatRooms = chatRoomService.listMyChatRooms(userDetails.getName(), page, size, keyword);
		return Response.ok(myChatRooms).toResponseEntity();
	}

	/**
	 * 채팅방 목록 전체 조회
	 */
	@GetMapping("/chatrooms")
	public ResponseEntity<Response<Page<ChatRoomRes>>> listAllChatRooms(
		@RequestParam(defaultValue = "1", required = false) int page,
		@RequestParam(defaultValue = "10", required = false) int size,
		@RequestParam(required = false) String keyword) {

		Page<ChatRoomRes> allChatRooms = chatRoomService.listAllChatRooms(page, size, keyword);
		return Response.ok(allChatRooms).toResponseEntity();
	}

	/**
	 * 특정 카테고리의 채팅방 목록 조회
	 */
	@GetMapping("/chatrooms/{category}")
	public ResponseEntity<Response<Page<ChatRoomRes>>> listChatRoomsByCategory(
		@PathVariable String category,
		@RequestParam(defaultValue = "1", required = false) int page,
		@RequestParam(defaultValue = "10", required = false) int size) {

		Page<ChatRoomRes> categoryRooms = chatRoomService.listChatRoomsByCategory(page, size, category);
		return Response.ok(categoryRooms).toResponseEntity();
	}

	/**
	 * 동행찾기 채팅방 목록 조회
	 *
	 * GET /api/auth/user/companion-chatrooms?page=1&size=10&category=연극
	 */
	@GetMapping("/companion-chatrooms")
	public ResponseEntity<Response<Page<CompanionChatRoomRes>>> listCompanionChatRooms(
			@RequestParam(defaultValue = "1", required = false) int page,
			@RequestParam(defaultValue = "10", required = false) int size,
			@RequestParam(required = false) String category) {

		Page<CompanionChatRoomRes> rooms =
				companionChatRoomService.listCompanionChatRooms(page, size, category);

		return Response.ok(rooms).toResponseEntity();
	}

	@GetMapping("/chatrooms/{chatRoomId}/owner")
	public ResponseEntity<Response<Boolean>> isChatRoomOwner(@PathVariable Long chatRoomId) {
		boolean result = chatRoomService.isChatRoomOwner(chatRoomId);
		return Response.ok(result).toResponseEntity();
	}

}
