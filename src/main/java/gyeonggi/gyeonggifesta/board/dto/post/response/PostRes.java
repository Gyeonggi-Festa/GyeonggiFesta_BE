package gyeonggi.gyeonggifesta.board.dto.post.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRes {
        private Long postId;
        private String title;
        private String content;
        private String writer;
        private long viewCount;
        private long likes;
        private long comments;
        private LocalDateTime updatedAt;
        private Long eventId;
        private String eventTitle;
        private String eventMainImage;
        private LocalDate eventStartDate;
        private LocalDate eventEndDate;
        private List<LocalDate> visitDates;
        private Integer recruitmentTotal;
        private Integer recruitmentPeriodDays;
        private String preferredGender;
        private Integer preferredMinAge;
        private Integer preferredMaxAge;
}
