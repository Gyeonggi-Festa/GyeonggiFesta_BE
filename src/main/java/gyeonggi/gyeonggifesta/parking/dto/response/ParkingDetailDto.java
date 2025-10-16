package gyeonggi.gyeonggifesta.parking.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParkingDetailDto {
	private String parkingId;
	private String name;
	private String division;        // PARKPLC_DIV_NM
	private String type;            // PARKPLC_TYPE
	private String roadAddress;
	private String lotnoAddress;
	private Integer slotCount;      // PARKNG_COMPRT_PLANE_CNT
	private String weekdayStart;    // WKDAY_OPERT_BEGIN_TM
	private String weekdayEnd;      // WKDAY_OPERT_END_TM
	private String satStart;        // SAT_OPERT_BEGIN_TM
	private String satEnd;          // SAT_OPERT_END_TM
	private String holStart;        // HOLIDAY_OPERT_BEGIN_TM
	private String holEnd;          // HOLIDAY_OPERT_END_TM
	private String chargeInfo;      // CHRG_INFO
	private Integer baseMinutes;    // PARKNG_BASIS_TM
	private Integer baseCharge;     // PARKNG_BASIS_USE_CHRG
	private Integer addUnitMinutes; // ADD_UNIT_TM
	private Integer addUnitCharge;  // ADD_UNIT_TM2_WITHIN_USE_CHRG
	private String phone;           // CONTCT_NO
	private Double lat;             // REFINE_WGS84_LAT
	private Double lon;             // REFINE_WGS84_LOGT
}
