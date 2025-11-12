package gyeonggi.gyeonggifesta.parking.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParkingMapDto {
	private String parkingId;
	private String name;
	private String division;     // 공영/민영/혼합
	private String type;         // 노상/노외
	private String roadAddress;
	private String lotnoAddress;
	private Double lat;
	private Double lon;

	// 필터/표시용
	private Integer slotCount;
	private String  payYn;       // Y(유료/혼합) / N(무료)
}
