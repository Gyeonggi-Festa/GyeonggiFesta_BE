package gyeonggi.gyeonggifesta.chat.service.chatroom;

import gyeonggi.gyeonggifesta.chat.dto.response.ChatRoomRes;
import gyeonggi.gyeonggifesta.chat.dto.response.MyChatRoomRes;
import gyeonggi.gyeonggifesta.chat.entity.ChatRoom;
import gyeonggi.gyeonggifesta.chat.enums.ChatRoomType;
import gyeonggi.gyeonggifesta.chat.repository.ChatRoomRepository;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 채팅방 조회 관련 서비스
 * - 채팅방 목록 조회 기능을 담당
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomQueryService {

	private final SecurityUtil securityUtil;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomDtoMapper chatRoomDtoMapper;

	/**
	 * 내 채팅방 목록 조회
	 */
	public Page<MyChatRoomRes> listMyChatRooms(String verifyId, int page, int size, String keyword) {
		// 요청 파라미터 준비
		Member currentMember = securityUtil.getCurrentMember(verifyId);
		PageRequest pageable = createPageRequest(page, size);
		String searchKeyword = normalizeKeyword(keyword);

		// 채팅방 조회 및 변환
		return fetchMyChatRoomsPage(currentMember, searchKeyword, pageable);
	}

	/**
	 * 전체 채팅방 목록 조회
	 * - 채팅방 이름, 카테고리로 검색 가능
	 */
	public Page<ChatRoomRes> listAllChatRooms(int page, int size, String keyword) {
		// 요청 파라미터 준비
		PageRequest pageable = createPageRequest(page, size);
		String searchKeyword = normalizeKeyword(keyword);

		// 채팅방 조회
		Page<ChatRoom> chatRoomPage = chatRoomRepository.findAllByTypeAndKeywordInNameOrCategory(
			ChatRoomType.GROUP, searchKeyword, pageable);

		// DTO 변환
		return chatRoomPage.map(chatRoomDtoMapper::toChatRoomRes);
	}

	/**
	 * 특정 카테고리의 채팅방 목록 조회
	 */
	public Page<ChatRoomRes> listChatRoomsByCategory(int page, int size, String category) {
		PageRequest pageable = createPageRequest(page, size);

		Page<ChatRoom> chatRoomPage = chatRoomRepository.findAllByTypeAndCategory(
			ChatRoomType.GROUP, category, pageable);

		return chatRoomPage.map(chatRoomDtoMapper::toChatRoomRes);
	}

	/**
	 * 페이지 요청 객체 생성
	 */
	private PageRequest createPageRequest(int page, int size) {
		return PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
	}

	/**
	 * 검색 키워드 정규화
	 */
	private String normalizeKeyword(String keyword) {
		return (keyword == null) ? "" : keyword;
	}

	/**
	 * 내 채팅방 목록을 페이징하여 조회
	 */
	private Page<MyChatRoomRes> fetchMyChatRoomsPage(Member member, String keyword, PageRequest pageable) {
		// 1. ID만 페이징하여 조회
		Page<Long> chatRoomIdPage = chatRoomRepository.findChatRoomIdsByMemberAndKeyword(
			member, keyword, pageable);

		if (chatRoomIdPage.isEmpty()) {
			return Page.empty(pageable);
		}

		// 2. 채팅방 상세 정보 조회
		List<ChatRoom> chatRooms = fetchChatRoomsWithMembers(chatRoomIdPage.getContent());

		// 3. 응답 DTO 변환
		List<MyChatRoomRes> content = convertToMyChatRoomResList(
			chatRooms, chatRoomIdPage.getContent(), member);

		// 4. 페이지 객체 생성
		return new PageImpl<>(content, pageable, chatRoomIdPage.getTotalElements());
	}

	/**
	 * 채팅방 ID 목록으로 채팅방과 멤버 정보를 한번에 조회
	 */
	private List<ChatRoom> fetchChatRoomsWithMembers(List<Long> chatRoomIds) {
		return chatRoomRepository.findChatRoomsByIdInWithMembers(chatRoomIds);
	}

	/**
	 * 채팅방 목록을 응답 DTO 목록으로 변환 (원래 순서 유지)
	 */
	private List<MyChatRoomRes> convertToMyChatRoomResList(
		List<ChatRoom> chatRooms, List<Long> orderedIds, Member member) {
		// ID로 빠른 조회를 위한 맵 생성
		Map<Long, ChatRoom> chatRoomMap = chatRooms.stream()
			.collect(Collectors.toMap(ChatRoom::getId, Function.identity()));

		// 원래 순서대로 DTO 변환
		return orderedIds.stream()
			.map(chatRoomMap::get)
			.filter(Objects::nonNull)
			.map(chatRoom -> chatRoomDtoMapper.toMyChatRoomRes(chatRoom, member))
			.collect(Collectors.toList());
	}
}