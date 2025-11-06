package gyeonggi.gyeonggifesta.event.dto.batch.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 경기도 문화행사 Open API 응답 DTO (서비스명: GGCULTUREVENTSTUS)
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenApiEventListRes {

	@JsonProperty("GGCULTUREVENTSTUS")
	private Container ggCultureEventStus;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Container {
		@JsonProperty("list_total_count")
		private int listTotalCount;

		@JsonProperty("RESULT")
		private Result result;

		@JsonProperty("row")
		private List<GyeonggiEventRow> row;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Result {
		@JsonProperty("CODE")    private String code;
		@JsonProperty("MESSAGE") private String message;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class GyeonggiEventRow {
		@JsonProperty("INST_NM")            private String instNm;
		@JsonProperty("TITLE")              private String title;
		@JsonProperty("CATEGORY_NM")        private String categoryNm;
		@JsonProperty("URL")                private String url;
		@JsonProperty("IMAGE_URL")          private String imageUrl;
		@JsonProperty("BEGIN_DE")           private String beginDate;   // yyyy-MM-dd 또는 yyyyMMdd 가능
		@JsonProperty("END_DE")             private String endDate;
		@JsonProperty("EVENT_TM_INFO")      private String timeInfo;
		@JsonProperty("PARTCPT_EXPN_INFO")  private String feeInfo;
		@JsonProperty("TELNO_INFO")         private String telInfo;
		@JsonProperty("HOST_INST_NM")       private String hostInstNm;
		@JsonProperty("HMPG_URL")           private String homepageUrl;
		@JsonProperty("WRITNG_DE")          private String writingDate;
	}
}
