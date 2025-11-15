package gyeonggi.gyeonggifesta.board.dto.post.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListRes {

        private Long postId;
        private String title;
        private String writer;
        private long viewCount;
        private int likes;
        private int comments;
        private LocalDate updatedAt;

        // 이벤트 정보
        private Long eventId;
        private String eventTitle;
        private String eventMainImage;
        private LocalDate eventStartDate;
        private LocalDate eventEndDate;

        // 방문 가능 날짜들
        private List<LocalDate> visitDates;

        // 모집 정보
        private Integer recruitmentTotal;
        private Integer recruitmentPeriodDays;

        // 선호 조건
        private String preferredGender;
        private Integer preferredMinAge;
        private Integer preferredMaxAge;
}
