package gyeonggi.gyeonggifesta.parking.service;

import com.fasterxml.jackson.databind.JsonNode;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.parking.component.ParkingApiCache;
import gyeonggi.gyeonggifesta.parking.dto.response.ParkingDetailDto;
import gyeonggi.gyeonggifesta.parking.dto.response.ParkingMapDto;
import gyeonggi.gyeonggifesta.parking.exception.ParkingErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ParkingInfoServiceImpl implements ParkingInfoService {

	private final ParkingApiCache cache;

	// ========= 기존 시군 단위 =========

	@Override
	public List<ParkingMapDto> getMap(String sigunNm) {
		List<JsonNode> all = cache.fetchAllRowsCached(sigunNm);
		List<JsonNode> rows = filterBySigun(all, sigunNm);

		return rows.stream()
				.map(this::toSimpleDto)
				.filter(d -> d.getLat() != null && d.getLon() != null)
				.toList();
	}

	@Override
	public List<ParkingMapDto> getList(String sigunNm, String q, String paid, boolean geoOnly) {
		List<JsonNode> all = cache.fetchAllRowsCached(sigunNm);
		List<JsonNode> rows = filterBySigun(all, sigunNm);

		return applyFilters(rows.stream().map(this::toSimpleDto), q, paid, geoOnly)
				.sorted(Comparator.comparing(ParkingMapDto::getName, Comparator.nullsLast(String::compareTo)))
				.toList();
	}

	@Override
	public ParkingDetailDto getDetail(String sigunNm, String parkingId) {
		List<JsonNode> all = cache.fetchAllRowsCached(sigunNm);
		List<JsonNode> rows = filterBySigun(all, sigunNm);

		return rows.stream()
				.filter(n -> parkingId.equals(asText(n, "PARKPLC_MANAGE_NO")))
				.findFirst()
				.map(this::toDetailDto)
				.orElseThrow(() -> new BusinessException(ParkingErrorCode.NOT_FOUND));
	}

	// ========= 경기도 전체 =========

	@Override
	public List<ParkingMapDto> getMapAll(String paid, String q, boolean geoOnly) {
		return applyFilters(mergedAll(), q, paid, geoOnly)
				.filter(d -> !geoOnly || (d.getLat() != null && d.getLon() != null))
				.sorted(Comparator.comparing(ParkingMapDto::getName, Comparator.nullsLast(String::compareTo)))
				.toList();
	}

	@Override
	public List<ParkingMapDto> getListAll(String paid, String q, boolean geoOnly) {
		return applyFilters(mergedAll(), q, paid, geoOnly)
				.sorted(Comparator.comparing(ParkingMapDto::getName, Comparator.nullsLast(String::compareTo)))
				.toList();
	}

	@Override
	public ParkingDetailDto getDetailGlobal(String parkingId) {
		List<JsonNode> all = cache.fetchAllRowsCached("ALL");

		return all.stream()
				.filter(n -> parkingId.equals(asText(n, "PARKPLC_MANAGE_NO")))
				.findFirst()
				.map(this::toDetailDto)
				.orElseThrow(() -> new BusinessException(ParkingErrorCode.NOT_FOUND));
	}

	// ========= 내부 유틸 =========

	/** 경기도 전 시군 합치기(중복은 관리번호로 제거) */
	private Stream<ParkingMapDto> mergedAll() {
		List<JsonNode> all = cache.fetchAllRowsCached("ALL");

		Map<String, ParkingMapDto> uniq = new ConcurrentHashMap<>();
		all.stream()
				.map(this::toSimpleDto)
				.filter(dto -> dto != null && dto.getParkingId() != null)
				.forEach(dto -> uniq.put(dto.getParkingId(), dto));

		return uniq.values().stream();
	}

	/** 응답 row 에서 SIGUN_NM 으로 시군 필터링 */
	private List<JsonNode> filterBySigun(List<JsonNode> rows, String sigunNm) {
		if (rows == null || sigunNm == null || sigunNm.isBlank()) {
			return rows;
		}
		return rows.stream()
				.filter(n -> sigunNm.equals(asText(n, "SIGUN_NM")))
				.toList();
	}

	private static Stream<ParkingMapDto> applyFilters(Stream<ParkingMapDto> stream,
													  String q, String paid, boolean geoOnly) {
		String keyword = (q == null) ? null : q.trim().toLowerCase(Locale.ROOT);
		String paidUpper = (paid == null) ? null : paid.trim().toUpperCase(Locale.ROOT);
		return stream.filter(dto -> {
			if (dto == null) return false;
			if (geoOnly && (dto.getLat() == null || dto.getLon() == null)) return false;
			if (paidUpper != null && !paidUpper.isEmpty()) {
				if (!"Y".equals(paidUpper) && !"N".equals(paidUpper)) return false;
				if (!paidUpper.equals(dto.getPayYn())) return false;
			}
			if (keyword != null && !keyword.isEmpty()) {
				String name = dto.getName() == null ? "" : dto.getName();
				String addr = (dto.getRoadAddress() != null) ? dto.getRoadAddress()
						: (dto.getLotnoAddress() != null ? dto.getLotnoAddress() : "");
				String s = (name + " " + addr).toLowerCase(Locale.ROOT);
				return s.contains(keyword);
			}
			return true;
		});
	}

	// ========= 매핑 =========

	private ParkingMapDto toSimpleDto(JsonNode n) {
		String chargeInfo = asText(n, "CHRG_INFO");
		String payYn = "무료".equals(chargeInfo) ? "N" : "Y";

		return ParkingMapDto.builder()
				.parkingId(asText(n, "PARKPLC_MANAGE_NO"))
				.name(asText(n, "PARKPLC_NM"))
				.division(asText(n, "PARKPLC_DIV_NM"))
				.type(asText(n, "PARKPLC_TYPE"))
				.roadAddress(asText(n, "LOCPLC_ROADNM_ADDR"))
				.lotnoAddress(asText(n, "LOCPLC_LOTNO_ADDR"))
				.lat(asDouble(n, "REFINE_WGS84_LAT"))
				.lon(asDouble(n, "REFINE_WGS84_LOGT"))
				.slotCount(asInt(n, "PARKNG_COMPRT_PLANE_CNT"))
				.payYn(payYn)
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

	// ========= safe converters =========
	private static String asText(JsonNode n, String f) {
		if (n == null) return null;
		JsonNode v = n.get(f);
		return (v == null || v.isNull()) ? null : v.asText();
	}

	private static Integer asInt(JsonNode n, String f) {
		String t = asText(n, f);
		if (t == null || t.isBlank()) return null;
		try { return Integer.parseInt(t.replaceAll("[^\\d]", "")); } catch (Exception e) { return null; }
	}

	private static Double asDouble(JsonNode n, String f) {
		String t = asText(n, f);
		if (t == null || t.isBlank()) return null;
		try { return Double.parseDouble(t); } catch (Exception e) { return null; }
	}
}
