package gyeonggi.gyeonggifesta.parking.service;

import com.fasterxml.jackson.databind.JsonNode;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.parking.component.ParkingApiClient;
import gyeonggi.gyeonggifesta.parking.dto.response.ParkingDetailDto;
import gyeonggi.gyeonggifesta.parking.dto.response.ParkingMapDto;
import gyeonggi.gyeonggifesta.parking.exception.ParkingErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingInfoServiceImpl implements ParkingInfoService {

	private final ParkingApiClient client;

	@Override
	public List<ParkingMapDto> getMap(String sigunNm) {
		List<JsonNode> rows = client.fetchAllRows(sigunNm);
		return rows.stream()
			.map(this::toMapDto)
			.filter(d -> d.getLat() != null && d.getLon() != null) // 지도에서 쓸 수 있는 것만
			.toList();
	}

	@Override
	public ParkingDetailDto getDetail(String sigunNm, String parkingId) {
		List<JsonNode> rows = client.fetchAllRows(sigunNm);
		return rows.stream()
			.filter(n -> parkingId.equals(asText(n, "PARKPLC_MANAGE_NO")))
			.findFirst()
			.map(this::toDetailDto)
			.orElseThrow(() -> new BusinessException(ParkingErrorCode.NOT_FOUND));
	}

	// ====== mapping & safe converters ======
	private ParkingMapDto toMapDto(JsonNode n) {
		return ParkingMapDto.builder()
			.parkingId(asText(n, "PARKPLC_MANAGE_NO"))
			.name(asText(n, "PARKPLC_NM"))
			.roadAddress(asText(n, "LOCPLC_ROADNM_ADDR"))
			.lotnoAddress(asText(n, "LOCPLC_LOTNO_ADDR"))
			.lat(asDouble(n, "REFINE_WGS84_LAT"))
			.lon(asDouble(n, "REFINE_WGS84_LOGT"))
			.build();
	}

	private ParkingDetailDto toDetailDto(JsonNode n) {
		return ParkingDetailDto.builder()
			.parkingId(asText(n, "PARKPLC_MANAGE_NO"))
			.name(asText(n, "PARKPLC_NM"))
			.division(asText(n, "PARKPLC_DIV_NM"))
			.type(asText(n, "PARKPLC_TYPE"))
			.roadAddress(asText(n, "LOCPLC_ROADNM_ADDR"))
			.lotnoAddress(asText(n, "LOCPLC_LOTNO_ADDR"))
			.slotCount(asInt(n, "PARKNG_COMPRT_PLANE_CNT"))
			.weekdayStart(asText(n, "WKDAY_OPERT_BEGIN_TM"))
			.weekdayEnd(asText(n, "WKDAY_OPERT_END_TM"))
			.satStart(asText(n, "SAT_OPERT_BEGIN_TM"))
			.satEnd(asText(n, "SAT_OPERT_END_TM"))
			.holStart(asText(n, "HOLIDAY_OPERT_BEGIN_TM"))
			.holEnd(asText(n, "HOLIDAY_OPERT_END_TM"))
			.chargeInfo(asText(n, "CHRG_INFO"))
			.baseMinutes(asInt(n, "PARKNG_BASIS_TM"))
			.baseCharge(asInt(n, "PARKNG_BASIS_USE_CHRG"))
			.addUnitMinutes(asInt(n, "ADD_UNIT_TM"))
			.addUnitCharge(asInt(n, "ADD_UNIT_TM2_WITHIN_USE_CHRG"))
			.phone(asText(n, "CONTCT_NO"))
			.lat(asDouble(n, "REFINE_WGS84_LAT"))
			.lon(asDouble(n, "REFINE_WGS84_LOGT"))
			.build();
	}

	private static String asText(JsonNode n, String f) {
		JsonNode v = n.get(f);
		return (v == null || v.isNull()) ? null : v.asText();
	}
	private static Integer asInt(JsonNode n, String f) {
		String t = asText(n, f);
		if (t == null || t.isBlank()) return null;
		try { return Integer.parseInt(t.replaceAll("[^0-9]", "")); } catch (Exception e) { return null; }
	}
	private static Double asDouble(JsonNode n, String f) {
		String t = asText(n, f);
		if (t == null || t.isBlank()) return null;
		try { return Double.parseDouble(t); } catch (Exception e) { return null; }
	}
}
