package gyeonggi.gyeonggifesta.event.entity;

import gyeonggi.gyeonggifesta.event.enums.Status;
import gyeonggi.gyeonggifesta.recommand.entity.AiRecommendation;
import gyeonggi.gyeonggifesta.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

	private double rating = 0;           // 리뷰 평균 점수

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
			String portal
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

	public void addEventReview(EventReview eventReview) {
		this.eventReviews.add(eventReview);
		addRating(eventReview.getRating());
	}

	public void removeEventReview(EventReview eventReview) {
		this.eventReviews.remove(eventReview);
		removeRating(eventReview.getRating());
		eventReview.setEvent(null);
	}

	public void addRating(double newRating) {
		double totalRating = this.rating * (eventReviews.size() - 1) + newRating;
		double avgRating = totalRating / eventReviews.size();
		this.rating = Math.round(avgRating * 2) / 2.0; // 0.5 단위 반올림
	}

	public void removeRating(double oldRating) {
		if (eventReviews.size() <= 1) {
			this.rating = 0;
			return;
		}
		double totalRating = this.rating * eventReviews.size() - oldRating;
		double avgRating = totalRating / (eventReviews.size() - 1);
		this.rating = Math.round(avgRating * 2) / 2.0;
	}
}
