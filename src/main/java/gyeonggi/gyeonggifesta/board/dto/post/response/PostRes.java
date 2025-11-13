package gyeonggi.gyeonggifesta.board.dto.post.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import gyeonggi.gyeonggifesta.board.enums.AgeRange;
import gyeonggi.gyeonggifesta.board.enums.GenderPreference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRes {
        private Long postId;
        private Long eventId;
        private String eventTitle;
        private String eventMainImage;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate eventStartDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate eventEndDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private List<LocalDate> visitDates;

        private Integer recruitPeople;
        private Integer recruitPeriod;
        private GenderPreference genderPreference;
        private AgeRange ageRange;
        private String title;
        private String content;
        private String writer;
        private long viewCount;
        private long likes;
        private long comments;
        private LocalDateTime updatedAt;
}
