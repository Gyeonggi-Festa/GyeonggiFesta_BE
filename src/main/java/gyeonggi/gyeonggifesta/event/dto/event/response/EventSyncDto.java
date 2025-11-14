package gyeonggi.gyeonggifesta.event.dto.event.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gyeonggi.gyeonggifesta.event.entity.Event;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter @Setter @NoArgsConstructor
public class EventSyncDto {

	@JsonProperty("eventid")
	private Long id;
	private String status;
	private String category;
	private String title;
	private String orgName;
	private String useFee;
	private String timeInfo;
	private String orgLink;
	private String mainImg;
	private LocalDate registerDate;
	private LocalDate startDate;
	private LocalDate endDate;
	private String isFree;
	private String portal;
	private int likes;
	private int favorites;
	private int comments;

	@Builder
	public EventSyncDto(Long id, String status, String category, String title,
						String orgName, String useFee, String timeInfo, String orgLink,
						String mainImg, LocalDate registerDate, LocalDate startDate,
						LocalDate endDate, String isFree, String portal,
						int likes, int favorites, int comments) {
		this.id = id;
		this.status = status;
		this.category = category;
		this.title = title;
		this.orgName = orgName;
		this.useFee = useFee;
		this.timeInfo = timeInfo;
		this.orgLink = orgLink;
		this.mainImg = mainImg;
		this.registerDate = registerDate;
		this.startDate = startDate;
		this.endDate = endDate;
		this.isFree = isFree;
		this.portal = portal;
		this.likes = likes;
		this.favorites = favorites;
		this.comments = comments;
	}

	public static EventSyncDto fromEntity(Event e) {
		return EventSyncDto.builder()
				.id(e.getId())
				.status(e.getStatus() != null ? e.getStatus().name() : null)
				.category(e.getCodename())
				.title(e.getTitle())
				.orgName(e.getOrgName())
				.useFee(e.getUseFee())
				.timeInfo(e.getTimeInfo())
				.orgLink(e.getOrgLink())
				.mainImg(e.getMainImg())
				.registerDate(e.getRegisterDate())
				.startDate(e.getStartDate())
				.endDate(e.getEndDate())
				.isFree(e.getIsFree())
				.portal(e.getPortal())
				.likes(e.getLikes())
				.favorites(e.getFavorites())
				.comments(e.getComments())
				.build();
	}
}
