package gyeonggi.gyeonggifesta.board.dto.post.request;

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
public class CreatePostReq {

        private Long eventId;
        private String title;
        private String content;
        private List<String> keyList;
        private List<LocalDate> visitDates;
        private Integer recruitmentTotal;
        private Integer recruitmentPeriodDays;
        private String preferredGender;
        private Integer preferredMinAge;
        private Integer preferredMaxAge;

}
