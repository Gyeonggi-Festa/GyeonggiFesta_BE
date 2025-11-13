package gyeonggi.gyeonggifesta.board.dto.post.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import gyeonggi.gyeonggifesta.board.enums.AgeRange;
import gyeonggi.gyeonggifesta.board.enums.GenderPreference;
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
public class UpdatePostReq {
    private String title;
    private String content;
    private List<String> keyList;

    private Long eventId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private List<LocalDate> visitDates;

    private Integer recruitPeople;
    private Integer recruitPeriod;
    private GenderPreference genderPreference;
    private AgeRange ageRange;
}
