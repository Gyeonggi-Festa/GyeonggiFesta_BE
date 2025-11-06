// path: src/main/java/gyeonggi/gyeonggifesta/recommand/service/AiRecommendServiceImpl.java
package gyeonggi.gyeonggifesta.recommand.service;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.entity.EventFavorite;
import gyeonggi.gyeonggifesta.event.entity.EventSearchHistory;
import gyeonggi.gyeonggifesta.event.repository.EventFavoriteRepository;
import gyeonggi.gyeonggifesta.event.repository.EventSearchHistoryRepository;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.member.repository.MemberRepository;
import gyeonggi.gyeonggifesta.recommand.dto.request.AiRecommendReq;
import gyeonggi.gyeonggifesta.recommand.dto.response.RecommendHistoryRes;
import gyeonggi.gyeonggifesta.recommand.entity.AiRecommendation;
import gyeonggi.gyeonggifesta.recommand.repository.AiRecommendationRepository;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiRecommendServiceImpl implements AiRecommendService {

	private final AiRecommendationRepository aiRecommendationRepository;
	private final EventSearchHistoryRepository eventSearchHistoryRepository;
	private final EventFavoriteRepository eventFavoriteRepository;
	private final MemberRepository memberRepository;
	private final SecurityUtil securityUtil;

	@Override
	public List<EventRes> getRecommendEvents() {
		Member member = securityUtil.getCurrentMember();

		List<AiRecommendation> aiRecommendations = aiRecommendationRepository.findLatestByMember(member);

		// 추천 정보가 없는 경우 빈 리스트 반환
		if (aiRecommendations.isEmpty()) {
			return Collections.emptyList();
		}

		// 추천 이벤트를 EventRes로 변환하여 반환
		return aiRecommendations.stream()
				.map(recommendation -> convertToEventRes(recommendation.getEvent()))
				.collect(Collectors.toList());
	}

	/**
	 * 현재 로그인한 사용자의 날짜별 추천 이벤트 기록을 조회
	 *
	 * @return 날짜별 추천 이벤트 기록 목록
	 */
	@Override
	public List<RecommendHistoryRes> getRecommendEventHistory() {
		Member member = securityUtil.getCurrentMember();

		// 해당 사용자의 모든 추천 정보 조회
		List<AiRecommendation> allRecommendations =
				aiRecommendationRepository.findAllByMemberOrderByCreatedAtDesc(member);

		if (allRecommendations.isEmpty()) {
			return Collections.emptyList();
		}

		// 날짜별로 그룹화
		Map<LocalDate, List<AiRecommendation>> recommendationsByDate = allRecommendations.stream()
				.collect(Collectors.groupingBy(
						recommendation -> recommendation.getCreatedAt().toLocalDate()
				));

		// 결과 DTO 생성
		return recommendationsByDate.entrySet().stream()
				.map(entry -> {
					LocalDate date = entry.getKey();
					List<EventRes> events = entry.getValue().stream()
							.map(recommendation -> convertToEventRes(recommendation.getEvent()))
							.collect(Collectors.toList());

					return RecommendHistoryRes.builder()
							.date(date)
							.events(events)
							.build();
				})
				.sorted(Comparator.comparing(RecommendHistoryRes::getDate).reversed()) // 최신 날짜순 정렬
				.collect(Collectors.toList());
	}

	/**
	 * 모든 사용자의 AI 추천 요청 DTO 목록 생성
	 *
	 * @return 모든 사용자의 AI 추천 요청 DTO 목록
	 */
	@Override
	public List<AiRecommendReq> createAllMembersAiRecommendRequests() {
		List<Member> allMembers = memberRepository.findAll();
		return allMembers.stream()
				.map(member -> createAiRecommendRequest(member.getId(), member.getVerifyId()))
				.collect(Collectors.toList());
	}

	/**
	 * 특정 사용자의 AI 추천 요청 DTO 생성
	 * 검색 기록과 즐겨찾기를 조회하여 추천 요청 DTO 생성
	 *
	 * @param memberId 회원 ID
	 * @param verifyId 회원 검증 ID
	 * @return 생성된 AI 추천 요청 DTO
	 */
	private AiRecommendReq createAiRecommendRequest(Long memberId, String verifyId) {
		// 회원의 검색 기록 조회
		List<String> searchHistories = getSearchHistories(memberId);

		// 회원의 즐겨찾기 목록 조회
		List<String> favorites = getFavorites(memberId);

		// 조회한 정보로 DTO 생성 및 반환
		return AiRecommendReq.builder()
				.userid(verifyId)  // 필드명을 API 요구사항에 맞춰 소문자로 유지
				.searchHistory(searchHistories)
				.favorites(favorites)
				.build();
	}

	/**
	 * 회원 ID로 검색 기록 조회
	 *
	 * @param memberId 회원 ID
	 * @return 검색 기록 목록 (이벤트 제목)
	 */
	private List<String> getSearchHistories(Long memberId) {
		List<EventSearchHistory> searchHistories = eventSearchHistoryRepository.findByMemberId(memberId);
		return searchHistories.stream()
				.map(EventSearchHistory::getContent)
				.collect(Collectors.toList());
	}

	/**
	 * 회원 ID로 즐겨찾기 목록 조회
	 *
	 * @param memberId 회원 ID
	 * @return 즐겨찾기 이벤트 제목 목록
	 */
	private List<String> getFavorites(Long memberId) {
		List<EventFavorite> favorites = eventFavoriteRepository.findByMemberId(memberId);
		return favorites.stream()
				.map(favorite -> favorite.getEvent().getTitle())
				.collect(Collectors.toList());
	}

	/**
	 * 이벤트 엔티티를 EventRes DTO로 변환
	 *
	 * @param event 이벤트 엔티티
	 * @return 변환된 EventRes DTO
	 */
	private EventRes convertToEventRes(Event event) {
		return EventRes.builder()
				.eventId(event.getId())
				.title(event.getTitle())
				.category(event.getCodename())
				.isFree(event.getIsFree())
				.status(event.getStatus() != null ? event.getStatus().name() : null)
				.startDate(event.getStartDate())
				.endDate(event.getEndDate())
				.mainImg(event.getMainImg())
				.likes(event.getLikes())
				.favorites(event.getFavorites())
				.comments(event.getComments())
				.rating(event.getRating())
				.ratingCount(event.getEventReviews() != null ? event.getEventReviews().size() : 0)
				.build();
	}
}
