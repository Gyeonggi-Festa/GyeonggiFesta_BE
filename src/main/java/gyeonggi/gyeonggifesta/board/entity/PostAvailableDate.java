package gyeonggi.gyeonggifesta.board.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostAvailableDate {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "post_available_date_id")
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "post_id", nullable = false)
        private Post post;

        @Column(nullable = false)
        private LocalDate visitDate;

        @Builder
        public PostAvailableDate(Post post, LocalDate visitDate) {
                this.post = post;
                this.visitDate = visitDate;
        }

        public void setPost(Post post) {
                this.post = post;
        }
}
