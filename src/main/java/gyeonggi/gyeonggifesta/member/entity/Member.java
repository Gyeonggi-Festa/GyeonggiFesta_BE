package gyeonggi.gyeonggifesta.member.entity;

import jakarta.persistence.*;
import lombok.*;
import gyeonggi.gyeonggifesta.event.entity.*;
import gyeonggi.gyeonggifesta.member.enums.Role;
import gyeonggi.gyeonggifesta.util.BaseEntity;

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
	private List<EventReview> eventReviews = new ArrayList<>();

	@Builder
	public Member(String verifyId, String username, String email, Role role, String gender, LocalDate birthDay) {
		this.verifyId = verifyId;
		this.username = username;
		this.email = email;
		this.role = role;
		this.gender = gender;
		this.birthDay = birthDay;
	}

	public int getAge() {
		return Period.between(this.birthDay, LocalDate.now()).getYears();
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

	public void addEventFavorite(EventFavorite eventFavorite) {
		this.eventFavorites.add(eventFavorite);
	}

	public void removeEventFavorite(EventFavorite eventFavorite) {
		this.eventFavorites.remove(eventFavorite);
		eventFavorite.setMember(null);
	}

}
