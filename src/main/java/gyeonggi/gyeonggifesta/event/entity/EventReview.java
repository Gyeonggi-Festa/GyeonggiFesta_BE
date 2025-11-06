package gyeonggi.gyeonggifesta.event.entity;

import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventReview extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "review_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id", nullable = false)
	@Setter
	private Event event;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	@Setter
	private Member member;

	@Column(columnDefinition = "TEXT")
	@Setter
	private String content;

	@Setter
	private double rating;

	@OneToMany(mappedBy = "eventReview", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventReviewMedia> mediaList = new ArrayList<>();

	@Builder
	public EventReview(Event event, Member member, String content, double rating) {
		this.event = event;
		this.member = member;
		this.content = content;
		this.rating = rating;
	}

	/**
	 * 리뷰에 미디어를 추가합니다.
	 *
	 * @param s3Key 이미지의 S3 키
	 * @param imageUrl 이미지 URL
	 * @param order 이미지 순서
	 * @return 생성된 EventReviewMedia
	 */
	public EventReviewMedia addMedia(String s3Key, String imageUrl, int order) {
		EventReviewMedia media = EventReviewMedia.builder()
			.eventReview(this)
			.s3Key(s3Key)
			.imageUrl(imageUrl)
			.order(order)
			.build();
		this.mediaList.add(media);
		return media;
	}

	/**
	 * 리뷰의 미디어를 모두 삭제합니다.
	 */
	public void clearMedia() {
		this.mediaList.clear();
	}

	/**
	 * 리뷰 내용을 업데이트합니다.
	 *
	 * @param content 업데이트할 내용
	 * @param rating 업데이트할 별점
	 */
	public void update(String content, Double rating) {
		if (content != null) {
			this.content = content;
		}

		if (rating != null && this.rating != rating) {
			double oldRating = this.rating;
			this.rating = rating;

			// 이벤트의 평균 별점 업데이트
			if (this.event != null) {
				this.event.removeRating(oldRating);
				this.event.addRating(rating);
			}
		}
	}
}