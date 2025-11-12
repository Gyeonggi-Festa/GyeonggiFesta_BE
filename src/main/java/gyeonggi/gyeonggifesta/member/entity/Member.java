package gyeonggi.gyeonggifesta.member.entity;

import gyeonggi.gyeonggifesta.board.entity.Post;
import gyeonggi.gyeonggifesta.board.entity.PostComment;
import gyeonggi.gyeonggifesta.board.entity.PostLike;
import gyeonggi.gyeonggifesta.event.entity.*;
import gyeonggi.gyeonggifesta.member.enums.Role;
import gyeonggi.gyeonggifesta.recommand.entity.AiRecommendation;
import gyeonggi.gyeonggifesta.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Long id;

	private String verifyId;

	@Setter
	private String username;

	@Setter
	private String email;

	@Enumerated(value = EnumType.STRING)
	@Setter
	private Role role;

	@Setter
	private String gender;

	@Column(name = "birthday")
	@Setter
	private LocalDate birthDay;

	// 관계 매핑
	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventComment> eventComments = new ArrayList<>();

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventLike> eventLikes = new ArrayList<>();

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventFavorite> eventFavorites = new ArrayList<>();

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventSearchHistory> eventSearchHistories = new ArrayList<>();

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Post> posts = new ArrayList<>();

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PostComment> postComments = new ArrayList<>();

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PostLike> postLikes = new ArrayList<>();

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AiRecommendation> recommendations = new ArrayList<>();

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventReview> eventReviews = new ArrayList<>();

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventViewHistory> eventViewHistories = new ArrayList<>();

	@Builder
	public Member(String verifyId, String username, String email, Role role, String gender, LocalDate birthDay) {
		this.verifyId = verifyId;
		this.username = username;
		this.email = email;
		this.role = role;
		this.gender = gender;
		this.birthDay = birthDay;
	}

	// NPE 가드 추가
	public int getAge() {
		return birthDay == null ? 0 : Period.between(this.birthDay, LocalDate.now()).getYears();
	}

	// 연관관계 편의 메서드
	public void addEventComment(EventComment eventComment) {
		this.eventComments.add(eventComment);
	}

	public void removeEventComment(EventComment eventComment) {
		this.eventComments.remove(eventComment);
		eventComment.setMember(null);
	}

	public void addEventLike(EventLike eventLike) {
		this.eventLikes.add(eventLike);
	}

	public void removeEventLike(EventLike eventLike) {
		this.eventLikes.remove(eventLike);
		eventLike.setMember(null);
	}

	// Post
	public void addPost(Post post) {
		posts.add(post);
		post.setMember(this);
	}

	public void removePost(Post post) {
		posts.remove(post);
		post.setMember(null);
	}

	// PostComment
	public void addPostComment(PostComment postComment) {
		postComments.add(postComment);
		postComment.setMember(this);
	}

	public void removePostComment(PostComment postComment) {
		postComments.remove(postComment);
		postComment.setMember(null);
	}

	// PostLike
	public void addPostLike(PostLike postLike) {
		postLikes.add(postLike);
		postLike.setMember(this);
	}

	public void removePostLike(PostLike postLike) {
		postLikes.remove(postLike);
		postLike.setMember(null);
	}

	public void addEventFavorite(EventFavorite eventFavorite) {
		this.eventFavorites.add(eventFavorite);
	}

	public void removeEventFavorite(EventFavorite eventFavorite) {
		this.eventFavorites.remove(eventFavorite);
	}

	// SearchHistory
	public void addSearchHistory(EventSearchHistory searchHistory) {
		eventSearchHistories.add(searchHistory);
		searchHistory.setMember(this);
	}

	public void removeSearchHistory(EventSearchHistory searchHistory) {
		eventSearchHistories.remove(searchHistory);
		searchHistory.setMember(null);
	}

	public void addViewHistory(EventViewHistory viewHistory) {
		eventViewHistories.add(viewHistory);
	}

	public void removeViewHistory(EventViewHistory viewHistory) {
		eventViewHistories.remove(viewHistory);
	}

	// AiRecommendation
	public void addRecommendation(AiRecommendation recommendation) {
		recommendations.add(recommendation);
	}

	public void removeRecommendation(AiRecommendation recommendation) {
		recommendations.remove(recommendation);
	}

	// EventReview
	public void addEventReview(EventReview eventReview) {
		this.eventReviews.add(eventReview);
		eventReview.setMember(this); // EventReview에는 @Setter 존재
	}

	public void removeEventReview(EventReview eventReview) {
		this.eventReviews.remove(eventReview);
		eventReview.setMember(null);
	}
}
