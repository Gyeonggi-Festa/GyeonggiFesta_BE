package gyeonggi.gyeonggifesta.board.entity;

import gyeonggi.gyeonggifesta.board.enums.AgeRange;
import gyeonggi.gyeonggifesta.board.enums.GenderPreference;
import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    @Setter
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @Setter
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @Setter
    private Event event;

    @Setter
    private String title;

    @Column(columnDefinition = "TEXT")
    @Setter
    private String content;

    @ElementCollection
    @CollectionTable(name = "post_visit_dates", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "visit_date")
    private List<LocalDate> visitDates = new ArrayList<>();

    @Setter
    private Integer recruitPeople;

    @Setter
    private Integer recruitPeriod;

    @Enumerated(EnumType.STRING)
    @Setter
    private GenderPreference genderPreference;

    @Enumerated(EnumType.STRING)
    @Setter
    private AgeRange ageRange;

    private long viewCount = 0L;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> postComments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostMedia> postMedias = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    @Builder
    public Post(
            Board board,
            Member member,
            Event event,
            String title,
            String content,
            List<LocalDate> visitDates,
            Integer recruitPeople,
            Integer recruitPeriod,
            GenderPreference genderPreference,
            AgeRange ageRange
    ) {
        this.board = board;
        this.member = member;
        this.event = event;
        this.title = title;
        this.content = content;
        updateVisitDates(visitDates);
        this.recruitPeople = recruitPeople;
        this.recruitPeriod = recruitPeriod;
        this.genderPreference = genderPreference;
        this.ageRange = ageRange;
    }

    public void updateVisitDates(List<LocalDate> visitDates) {
        this.visitDates.clear();
        if (visitDates != null) {
            this.visitDates.addAll(visitDates);
        }
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    // 연관관계 편의 메서드: PostComment 추가 (빌더에서 이미 관계 설정이 되어 있으므로 단순 추가)
    public void addPostComment(PostComment postComment) {
        postComments.add(postComment);
    }

    // 연관관계 편의 메서드: PostComment 제거 (제거 시 setter로 null 처리)
    public void removePostComment(PostComment postComment) {
        postComments.remove(postComment);
        postComment.setPost(null);
    }

    // 연관관계 편의 메서드: PostMedia 추가
    public void addPostMedia(PostMedia postMedia) {
        postMedias.add(postMedia);
    }

    // 연관관계 편의 메서드: PostMedia 제거
    public void removePostMedia(PostMedia postMedia) {
        postMedias.remove(postMedia);
        postMedia.setPost(null);
    }

    // 연관관계 편의 메서드: PostLike 추가
    public void addPostLike(PostLike postLike) {
        postLikes.add(postLike);
    }

    // 연관관계 편의 메서드: PostLike 제거
    public void removePostLike(PostLike postLike) {
        postLikes.remove(postLike);
        postLike.setPost(null);
    }
}
