package gyeonggi.gyeonggifesta.parking.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParkingApiInfoResponse {

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Root {
		@JsonProperty("ParkingPlace")
		private List<Section> parkingPlace;

		/** head 섹션 반환 (없으면 null) */
		public Head getHead() {
			if (parkingPlace == null) return null;
			for (Section s : parkingPlace) {
				if (s.head != null && !s.head.isEmpty()) return s.head.get(0);
			}
			return null;
		}

		/** row(실데이터 리스트) 반환 (없으면 null) */
		public List<ParkingInfo> getRow() {
			if (parkingPlace == null) return null;
			for (Section s : parkingPlace) {
				if (s.row != null) return s.row;
			}
			return null;
		}

		/** 결과코드/메시지 반환 (없으면 null) */
		public Result getResult() {
			Head h = getHead();
			return h != null ? h.getResult() : null;
		}
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Section {
		private List<Head> head;
		private List<ParkingInfo> row;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Head {
		@JsonProperty("list_total_count")
		private Integer listTotalCount;
		@JsonProperty("RESULT")
		private Result result;
		@JsonProperty("api_version")
		private String apiVersion;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Result {
		@JsonProperty("CODE")
		private String code;     // "INFO-000" 정상
		@JsonProperty("MESSAGE")
		private String message;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ParkingInfo {
		@JsonProperty("PARKPLC_MANAGE_NO") private String manageNo;
		@JsonProperty("PARKPLC_NM")        private String name;
		@JsonProperty("PARKPLC_DIV_NM")    private String division;
		@JsonProperty("PARKPLC_TYPE")      private String type;
		@JsonProperty("LOCPLC_ROADNM_ADDR")private String roadAddr;
		@JsonProperty("LOCPLC_LOTNO_ADDR") private String lotnoAddr;
		@JsonProperty("PARKNG_COMPRT_PLANE_CNT") private Integer totalCnt;

		@JsonProperty("WKDAY_OPERT_BEGIN_TM") private String wkdayBegin; // "HH:mm"
		@JsonProperty("WKDAY_OPERT_END_TM")   private String wkdayEnd;
		@JsonProperty("SAT_OPERT_BEGIN_TM")   private String satBegin;
		@JsonProperty("SAT_OPERT_END_TM")     private String satEnd;
		@JsonProperty("HOLIDAY_OPERT_BEGIN_TM") private String holBegin;
		@JsonProperty("HOLIDAY_OPERT_END_TM")   private String holEnd;

		@JsonProperty("CHRG_INFO")              private String chargeInfo; // 유료/무료/혼합
		@JsonProperty("PARKNG_BASIS_TM")        private Integer baseTime;
		@JsonProperty("PARKNG_BASIS_USE_CHRG")  private Integer baseRate;
		@JsonProperty("ADD_UNIT_TM")            private Integer addTime;
		@JsonProperty("ADD_UNIT_TM2_WITHIN_USE_CHRG") private Integer addRate;
		@JsonProperty("DAY1_PARKTK_USE_CHRG")   private Integer dayMax;
		@JsonProperty("CONTCT_NO")              private String tel;

		public String getParkingCode() { return manageNo; }
		public String getParkingName() { return name; }
		public String getAddress() {
			return (roadAddr != null && !roadAddr.isBlank()) ? roadAddr : lotnoAddr;
		}
		public String getParkingTypeName() { return type; }     // 노상/노외
		public String getOperationName()   { return division; } // 공영/민영/혼합
		public String getTELNO() { return tel; }
		public int    getTotalParkingCount() { return totalCnt == null ? 0 : totalCnt; }
		public String getPayYn() { return "무료".equals(chargeInfo) ? "N" : "Y"; }
		public String getNightPayYn() { return "N"; } // 미제공 → 기본 N

		public String getWeekdayOpenTime()  { return wkdayBegin; } // "HH:mm"
		public String getWeekdayCloseTime() { return wkdayEnd;   }
		public String getWeekendOpenTime()  { return satBegin;   }
		public String getWeekendCloseTime() { return satEnd;     }
		public String getHolidayOpenTime()  { return holBegin;   }
		public String getHolidayCloseTime() { return holEnd;     }

		public String getSaturdayFeeName() { return chargeInfo; }
		public String getHolidayFeeName()  { return chargeInfo; }

		public int getBaseRate()        { return baseRate == null ? 0 : baseRate; }
		public int getBaseTime()        { return baseTime == null ? 0 : baseTime; }
		public int getAdditionalRate()  { return addRate  == null ? 0 : addRate; }
		public int getAdditionalTime()  { return addTime  == null ? 0 : addTime; }
		public int getDailyMaxRate()    { return dayMax   == null ? 0 : dayMax;  }
	}
}
