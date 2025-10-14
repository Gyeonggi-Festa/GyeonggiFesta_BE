package gyeonggi.gyeonggifesta.parking.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParkingMapDto {
	private String parkingId;
	private String name;
	private String roadAddress;
	private String lotnoAddress;
	private Double lat;
	private Double lon;
}
