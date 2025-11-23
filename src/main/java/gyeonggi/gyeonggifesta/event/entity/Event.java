package gyeonggi.gyeonggifesta.event.entity;

import gyeonggi.gyeonggifesta.event.enums.Status;
import gyeonggi.gyeonggifesta.recommand.entity.AiRecommendation;
import gyeonggi.gyeonggifesta.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Table(
		name = "event",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_event_title_reg_cat_org_end",
						columnNames = {"title", "register_date", "codename", "org_name", "end_date"}
				)
		}
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "event_id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Setter
	private Status status;               // 진행상태 (NOT_STARTED/PROGRESS/END)

	@Column(name = "codename")
	private String codename;             // CATEGORY_NM

	private String title;                // TITLE

	@Column(name = "org_name")
	private String orgName;              // HOST_INST_NM

	@Column(name = "use_fee")
	private String useFee;               // PARTCPT_EXPN_INFO

	@Column(name = "time_info")
	private String timeInfo;             // EVENT_TM_INFO

	@Column(name = "register_date")
	private LocalDate registerDate;      // WRITNG_DE

	@Column(name = "start_date")
	private LocalDate startDate;         // BEGIN_DE

	@Column(name = "end_date")
	private LocalDate endDate;           // END_DE

	// URL 계열은 TEXT로: 매우 긴 URL 대응
	@Column(name = "org_link", columnDefinition = "TEXT")
	private String orgLink;              // HMPG_URL (fallback: URL)

	@Column(name = "main_img", columnDefinition = "TEXT")
	private String mainImg;              // IMAGE_URL

	@Column(name = "is_free")
	private String isFree;               // "Y"/"N" (무료 여부 추론)

	@Column(columnDefinition = "TEXT")
	private String portal;               // URL

	/**
	 * 이벤트의 평균 평점 (0.5 단위 반올림 적용)
	 */
	private double rating = 0;

	@Column(name = "latitude", precision = 10, scale = 7)
	private BigDecimal latitude;

	@Column(name = "longitude", precision = 10, scale = 7)
	private BigDecimal longitude;

	@Column(name = "road_address")
	private String roadAddress;

	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventComment> eventComments = new ArrayList<>();

	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventLike> eventLikes = new ArrayList<>();

	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventFavorite> eventFavorites = new ArrayList<>();

	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AiRecommendation> recommendations = new ArrayList<>();

	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventReview> eventReviews = new ArrayList<>();

	@Builder
	public Event(
			Status status,
			String codename,
			String title,
			String orgName,
			String useFee,
			String timeInfo,
			LocalDate registerDate,
			LocalDate startDate,
			LocalDate endDate,
			String orgLink,
			String mainImg,
			String isFree,
			String portal,
			BigDecimal latitude,
			BigDecimal longitude,
			String roadAddress
	) {
		this.status = status;
		this.codename = codename;
		this.title = title;
		this.orgName = orgName;
		this.useFee = useFee;
		this.timeInfo = timeInfo;
		this.registerDate = registerDate;
		this.startDate = startDate;
		this.endDate = endDate;
		this.orgLink = orgLink;
		this.mainImg = mainImg;
		this.isFree = isFree;
		this.portal = portal;
		this.latitude = latitude;
		this.longitude = longitude;
		this.roadAddress = roadAddress;
	}

	public int getLikes() { return eventLikes.size(); }
	public int getFavorites() { return eventFavorites.size(); }
	public int getComments() { return eventComments.size(); }

	public void addEventComment(EventComment comment) { this.eventComments.add(comment); }
	public void removeEventComment(EventComment eventComment) {
		this.eventComments.remove(eventComment);
		eventComment.setEvent(null);
	}

	public void addEventLike(EventLike eventLike) { this.eventLikes.add(eventLike); }
	public void removeEventLike(EventLike eventLike) {
		this.eventLikes.remove(eventLike);
		eventLike.setMember(null);
	}

	public void addEventFavorite(EventFavorite eventFavorite) { this.eventFavorites.add(eventFavorite); }
	public void removeEventFavorite(EventFavorite eventFavorite) {
		this.eventFavorites.remove(eventFavorite);
		eventFavorite.setMember(null);
	}

	/**
	 * 리뷰 추가 시 리스트에 추가하고,
	 * 전체 리뷰 기준으로 다시 평균 평점을 계산한다.
	 */
	public void addEventReview(EventReview eventReview) {
		this.eventReviews.add(eventReview);
		recalcRating();
	}

	/**
	 * 리뷰 제거 시 리스트에서 제거하고,
	 * 전체 리뷰 기준으로 다시 평균 평점을 계산한다.
	 */
	public void removeEventReview(EventReview eventReview) {
		this.eventReviews.remove(eventReview);
		recalcRating();
		eventReview.setEvent(null);
	}

	/**
	 * 현재 eventReviews 리스트 전체를 기준으로
	 * 평균 평점을 다시 계산한다.
	 *
	 * - 리뷰가 없으면 rating = 0
	 * - 있으면 평균을 구한 뒤 0.5 단위로 반올림
	 */
	public void recalcRating() {
		if (eventReviews == null || eventReviews.isEmpty()) {
			this.rating = 0;
			return;
		}

		double sum = eventReviews.stream()
				.mapToDouble(EventReview::getRating)
				.sum();

		double avg = sum / eventReviews.size();
		this.rating = Math.round(avg * 2) / 2.0;  // 0.5 단위 반올림
	}
}
