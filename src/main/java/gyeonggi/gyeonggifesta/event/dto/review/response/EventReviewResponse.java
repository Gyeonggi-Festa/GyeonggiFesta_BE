package gyeonggi.gyeonggifesta.event.dto.review.response;

import gyeonggi.gyeonggifesta.event.entity.EventReview;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class EventReviewResponse {

	private Long id;
	private Long eventId;
	private String eventTitle;
	private Long memberId;
	private String memberName;
	private String content;
	private double rating;
	private List<MediaResponse> mediaList;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	public EventReviewResponse(Long id, Long eventId, String eventTitle, Long memberId, String memberName,
		String content, double rating, List<MediaResponse> mediaList,
		LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.id = id;
		this.eventId = eventId;
		this.eventTitle = eventTitle;
		this.memberId = memberId;
		this.memberName = memberName;
		this.content = content;
		this.rating = rating;
		this.mediaList = mediaList;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static EventReviewResponse from(EventReview eventReview) {
		List<MediaResponse> mediaResponses = eventReview.getMediaList().stream()
			.sorted(Comparator.comparingInt(media -> media.getOrder()))
			.map(MediaResponse::from)
			.collect(Collectors.toList());

		return EventReviewResponse.builder()
			.id(eventReview.getId())
			.eventId(eventReview.getEvent().getId())
			.eventTitle(eventReview.getEvent().getTitle())
			.memberId(eventReview.getMember().getId())
			.memberName(eventReview.getMember().getUsername())
			.content(eventReview.getContent())
			.rating(eventReview.getRating())
			.mediaList(mediaResponses)
			.createdAt(eventReview.getCreatedAt())
			.updatedAt(eventReview.getUpdatedAt())
			.build();
	}
}
