package gyeonggi.gyeonggifesta.event.dto.event.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDetailRes {

	private Long eventId;
	private String status;
	private String category;      // codename
	private String title;
	private String orgName;       // HOST_INST_NM
	private String useFee;        // PARTCPT_EXPN_INFO
	private String timeInfo;      // EVENT_TM_INFO
	private String orgLink;       // HMPG_URL or URL
	private String mainImg;       // IMAGE_URL
	private LocalDate startDate;  // BEGIN_DE
	private LocalDate endDate;    // END_DE
	private String isFree;        // "Y"/"N"
	private int likes;
	private int favorites;
	private int comments;
	private double rating;
	private boolean isFavorite;
}
