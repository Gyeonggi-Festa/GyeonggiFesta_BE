package gyeonggi.gyeonggifesta.event.service.review;

import gyeonggi.gyeonggifesta.aws.service.S3Service;
import gyeonggi.gyeonggifesta.event.dto.review.request.EventReviewCreateRequest;
import gyeonggi.gyeonggifesta.event.dto.review.request.EventReviewUpdateRequest;
import gyeonggi.gyeonggifesta.event.dto.review.request.MediaRequest;
import gyeonggi.gyeonggifesta.event.dto.review.response.EventReviewResponse;
import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.entity.EventReview;
import gyeonggi.gyeonggifesta.event.entity.EventReviewMedia;
import gyeonggi.gyeonggifesta.event.exception.EventErrorCode;
import gyeonggi.gyeonggifesta.event.repository.EventRepository;
import gyeonggi.gyeonggifesta.event.repository.EventReviewMediaRepository;
import gyeonggi.gyeonggifesta.event.repository.EventReviewRepository;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventReviewServiceImpl implements EventReviewService {

	private final EventReviewRepository eventReviewRepository;
	private final EventReviewMediaRepository eventReviewMediaRepository;
	private final EventRepository eventRepository;
	private final S3Service s3Service;
	private final SecurityUtil securityUtil;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	@Override
	@Transactional
	public EventReviewResponse createReview(EventReviewCreateRequest request) {
		Member member = securityUtil.getCurrentMember();

		// 이벤트 조회
		Event event = eventRepository.findById(request.getEventId())
				.orElseThrow(() -> new BusinessException(EventErrorCode.NOT_EXIST_EVENT));

		// 별점 검증
		validateRating(request.getRating());

		EventReview eventReview = EventReview.builder()
				.event(event)
				.member(member)
				.content(request.getContent())
				.rating(request.getRating())
				.build();

		// 이벤트에 리뷰 추가 + 평점 재계산
		event.addEventReview(eventReview);

		// 리뷰 저장
		EventReview savedReview = eventReviewRepository.save(eventReview);

		if (request.getMediaList() != null && !request.getMediaList().isEmpty()) {
			for (MediaRequest mediaRequest : request.getMediaList()) {
				String imageUrl = getImageUrlFromS3Key(mediaRequest.getS3Key());
				eventReview.addMedia(mediaRequest.getS3Key(), imageUrl, mediaRequest.getOrder());
			}
		}

		return EventReviewResponse.from(savedReview);
	}

	@Override
	@Transactional
	public EventReviewResponse updateReview(Long reviewId, EventReviewUpdateRequest request) {
		Member member = securityUtil.getCurrentMember();

		// 리뷰 조회 및 권한 검증
		EventReview eventReview = getReviewWithPermissionCheck(reviewId, member);
		Event event = eventReview.getEvent();

		// 별점 검증
		if (request.getRating() != null) {
			validateRating(request.getRating());
		}

		// 리뷰 내용/평점 업데이트
		eventReview.update(request.getContent(), request.getRating());

		// 미디어 처리
		if (request.getMediaList() != null) {
			// 기존 미디어의 S3 Key 목록 저장
			List<String> oldS3Keys = eventReview.getMediaList().stream()
					.map(EventReviewMedia::getS3Key)
					.toList();

			// 기존 미디어 엔티티 제거
			eventReview.clearMedia();

			// 새 미디어 추가
			for (MediaRequest mediaRequest : request.getMediaList()) {
				String imageUrl = getImageUrlFromS3Key(mediaRequest.getS3Key());
				eventReview.addMedia(mediaRequest.getS3Key(), imageUrl, mediaRequest.getOrder());
			}

			// 더 이상 사용하지 않는 S3 파일 삭제
			List<String> newS3Keys = request.getMediaList().stream()
					.map(MediaRequest::getS3Key)
					.toList();

			for (String oldS3Key : oldS3Keys) {
				if (!newS3Keys.contains(oldS3Key)) {
					s3Service.deleteObject(oldS3Key);
				}
			}
		}

		// 리뷰 평점이 바뀌었을 수 있으므로, 전체 리뷰 기준으로 다시 평균 계산
		event.recalcRating();

		return EventReviewResponse.from(eventReview);
	}

	@Override
	@Transactional
	public void deleteReview(Long reviewId) {
		Member member = securityUtil.getCurrentMember();

		// 리뷰 조회 및 권한 검증
		EventReview eventReview = getReviewWithPermissionCheck(reviewId, member);
		Event event = eventReview.getEvent();

		// S3에서 이미지 삭제
		for (EventReviewMedia media : eventReview.getMediaList()) {
			s3Service.deleteObject(media.getS3Key());
		}

		// 이벤트에서 리뷰 제거 (내부에서 recalcRating 호출)
		event.removeEventReview(eventReview);

		// 리뷰 삭제
		eventReviewRepository.delete(eventReview);
	}

	@Override
	public Page<EventReviewResponse> getReviewsByEventId(Long eventId, Pageable pageable) {
		Page<EventReview> reviews = eventReviewRepository.findByEventIdOrderByCreatedAtDesc(eventId, pageable);
		return reviews.map(EventReviewResponse::from);
	}

	@Override
	public Page<EventReviewResponse> getReviewsByMemberId(String verifyId, Pageable pageable) {
		Member member = securityUtil.getCurrentMember(verifyId);
		Page<EventReview> reviews = eventReviewRepository.findByMemberIdOrderByCreatedAtDesc(member.getId(), pageable);
		return reviews.map(EventReviewResponse::from);
	}

	@Override
	public EventReviewResponse getReviewById(Long reviewId) {
		EventReview review = eventReviewRepository.findById(reviewId)
				.orElseThrow(() -> new BusinessException(EventErrorCode.NOT_EXIST_REVIEW));
		return EventReviewResponse.from(review);
	}

	@Override
	public EventReviewResponse getReviewByEventAndMember(String verifyId, Long eventId) {
		Member member = securityUtil.getCurrentMember(verifyId);
		return eventReviewRepository.findByEventIdAndMemberId(eventId, member.getId())
				.map(EventReviewResponse::from)
				.orElse(null);
	}

	/**
	 * 리뷰를 조회하고 작성자 권한을 검증합니다.
	 *
	 * @param reviewId 리뷰 ID
	 * @param member   현재 로그인한 사용자
	 * @return 권한이 확인된 리뷰
	 * @throws BusinessException 리뷰가 없거나 권한이 없는 경우
	 */
	private EventReview getReviewWithPermissionCheck(Long reviewId, Member member) {
		EventReview review = eventReviewRepository.findById(reviewId)
				.orElseThrow(() -> new BusinessException(EventErrorCode.NOT_EXIST_REVIEW));

		if (!review.getMember().getId().equals(member.getId())) {
			throw new BusinessException(EventErrorCode.NOT_WRITER);
		}

		return review;
	}

	/**
	 * 별점이 유효한지 검증합니다.
	 *
	 * @param rating 검증할 별점
	 * @throws BusinessException 유효하지 않은 별점인 경우
	 */
	private void validateRating(double rating) {
		if (rating < 0.5 || rating > 5.0 || Math.abs(rating * 2 - Math.round(rating * 2)) > 0.001) {
			throw new BusinessException(EventErrorCode.INVALID_RATING);
		}
	}

	private String getImageUrlFromS3Key(String s3Key) {
		return "https://" + bucketName + ".s3.amazonaws.com/" + s3Key;
	}
}
