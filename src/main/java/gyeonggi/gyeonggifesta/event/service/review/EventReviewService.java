package gyeonggi.gyeonggifesta.event.service.review;

import gyeonggi.gyeonggifesta.event.dto.review.request.EventReviewCreateRequest;
import gyeonggi.gyeonggifesta.event.dto.review.request.EventReviewUpdateRequest;
import gyeonggi.gyeonggifesta.event.dto.review.response.EventReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventReviewService {

	EventReviewResponse createReview(EventReviewCreateRequest request);

	EventReviewResponse updateReview(Long reviewId, EventReviewUpdateRequest request);

	void deleteReview(Long reviewId);

	/**
	 * 이벤트 ID로 리뷰 목록을 조회합니다.
	 *
	 * @param eventId  이벤트 ID
	 * @param pageable 페이징 정보
	 * @return 페이징된 리뷰 목록
	 */
	Page<EventReviewResponse> getReviewsByEventId(Long eventId, Pageable pageable);

	Page<EventReviewResponse> getReviewsByMemberId(String verifyId, Pageable pageable);

	/**
	 * 리뷰 ID로 리뷰를 조회합니다.
	 *
	 * @param reviewId 리뷰 ID
	 * @return 리뷰 정보
	 */
	EventReviewResponse getReviewById(Long reviewId);

	EventReviewResponse getReviewByEventAndMember(String verifyId, Long eventId);
}