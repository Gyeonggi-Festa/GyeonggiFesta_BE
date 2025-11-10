package gyeonggi.gyeonggifesta.event.controller.review;

import gyeonggi.gyeonggifesta.auth.custom.CustomUserDetails;
import gyeonggi.gyeonggifesta.event.dto.review.request.EventReviewCreateRequest;
import gyeonggi.gyeonggifesta.event.dto.review.request.EventReviewUpdateRequest;
import gyeonggi.gyeonggifesta.event.dto.review.response.EventReviewResponse;
import gyeonggi.gyeonggifesta.event.service.review.EventReviewService;
import gyeonggi.gyeonggifesta.util.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 이벤트 리뷰 관련 API를 처리
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/user")
public class EventReviewController {

	private final EventReviewService eventReviewService;

	@PostMapping("/reviews")
	public ResponseEntity<Response<EventReviewResponse>> createReview(
		@Valid @RequestBody EventReviewCreateRequest request) {
		EventReviewResponse response = eventReviewService.createReview(request);
		return Response.ok(response).toResponseEntity();
	}

	@PatchMapping("/reviews/{reviewId}")
	public ResponseEntity<Response<EventReviewResponse>> updateReview(
		@PathVariable Long reviewId,
		@RequestBody EventReviewUpdateRequest request) {
		EventReviewResponse response = eventReviewService.updateReview(reviewId, request);
		return Response.ok(response).toResponseEntity();
	}

	@DeleteMapping("/reviews/{reviewId}")
	public ResponseEntity<Response<Void>> deleteReview(@PathVariable Long reviewId) {
		eventReviewService.deleteReview(reviewId);
		return Response.ok().toResponseEntity();
	}

	/**
	 * 이벤트의 리뷰 목록을 조회
	 *
	 * @param eventId 이벤트 ID
	 * @param pageable 페이징 정보
	 * @return 페이징된 리뷰 목록
	 */
	@GetMapping("/events/{eventId}/reviews")
	public ResponseEntity<Response<Page<EventReviewResponse>>> getReviewsByEventId(
		@PathVariable Long eventId,
		@PageableDefault(size = 10) Pageable pageable) {
		Page<EventReviewResponse> response = eventReviewService.getReviewsByEventId(eventId, pageable);
		return Response.ok(response).toResponseEntity();
	}

	/**
	 * 특정 리뷰 조회
	 *
	 * @param reviewId 리뷰 ID
	 * @return 리뷰 정보
	 */
	@GetMapping("/reviews/{reviewId}")
	public ResponseEntity<Response<EventReviewResponse>> getReviewById(
		@PathVariable Long reviewId) {
		EventReviewResponse response = eventReviewService.getReviewById(reviewId);
		return Response.ok(response).toResponseEntity();
	}

	/**
	 * 로그인한 사용자의 리뷰 목록 조회
	 *
	 * @param pageable 페이징 정보
	 * @return 페이징된 리뷰 목록
	 */
	@GetMapping("/my-reviews")
	public ResponseEntity<Response<Page<EventReviewResponse>>> getMyReviews(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PageableDefault(size = 10) Pageable pageable) {
		Page<EventReviewResponse> response = eventReviewService.getReviewsByMemberId(userDetails.getName(), pageable);
		return Response.ok(response).toResponseEntity();
	}

	/**
	 * 이벤트에 대한 현재 사용자의 리뷰 조회
	 *
	 * @param eventId 이벤트 ID
	 * @return 리뷰 정보, 없으면 null
	 */
	@GetMapping("/events/{eventId}/my-review")
	public ResponseEntity<Response<EventReviewResponse>> getMyReviewByEventId(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long eventId) {
		EventReviewResponse response = eventReviewService.getReviewByEventAndMember(userDetails.getName(), eventId);
		return Response.ok(response).toResponseEntity();
	}
}