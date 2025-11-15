package gyeonggi.gyeonggifesta.event.dto.event.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRes {
	private Long eventId;
	private String title;
	private String category;      // codename
	private String isFree;        // "Y"/"N"
	private String status;        // NOT_STARTED/PROGRESS/END
	private LocalDate startDate;
	private LocalDate endDate;
	private String mainImg;
	private int likes;
	private int favorites;
	private int comments;
	private double rating;
	private int ratingCount;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private String roadAddress;
}
