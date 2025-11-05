package gyeonggi.gyeonggifesta.recommand.service;

import gyeonggi.gyeonggifesta.event.dto.event.response.EventRes;
import gyeonggi.gyeonggifesta.recommand.dto.request.AiRecommendReq;
import gyeonggi.gyeonggifesta.recommand.dto.response.RecommendHistoryRes;

import java.util.List;

public interface AiRecommendService {

	/**
	 * 모든 사용자의 AI 추천 요청 DTO 목록을 생성
	 *
	 * @return 모든 사용자의 AI 추천 요청 DTO 목록
	 */
	List<AiRecommendReq> createAllMembersAiRecommendRequests();

	/**
	 * 현재 로그인한 사용자의 최신 추천 이벤트 목록을 조회
	 *
	 * @return 추천 이벤트 목록
	 */
	List<EventRes> getRecommendEvents();

	/**
	 * 현재 로그인한 사용자의 날짜별 추천 이벤트 기록을 조회
	 *
	 * @return 날짜별 추천 이벤트 기록 목록
	 */
	List<RecommendHistoryRes> getRecommendEventHistory();
}
