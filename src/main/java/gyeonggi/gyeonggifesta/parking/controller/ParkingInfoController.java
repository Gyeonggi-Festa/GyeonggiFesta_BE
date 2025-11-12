package gyeonggi.gyeonggifesta.parking.controller;

import gyeonggi.gyeonggifesta.parking.dto.response.ParkingDetailDto;
import gyeonggi.gyeonggifesta.parking.dto.response.ParkingMapDto;
import gyeonggi.gyeonggifesta.parking.service.ParkingInfoService;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/user/parking")
@RequiredArgsConstructor
public class ParkingInfoController {

	private final ParkingInfoService service;

	// ===== 기존 시군 단위 =====
	/** 지도용 목록(시군) */
	@GetMapping("/map/{sigunNm}")
	public ResponseEntity<Response<List<ParkingMapDto>>> map(@PathVariable String sigunNm) {
		return Response.ok(service.getMap(sigunNm)).toResponseEntity();
	}

	/** 상세(시군 + parkingId) */
	@GetMapping("/detail/{sigunNm}/{parkingId}")
	public ResponseEntity<Response<ParkingDetailDto>> detail(@PathVariable String sigunNm,
															 @PathVariable String parkingId) {
		return Response.ok(service.getDetail(sigunNm, parkingId)).toResponseEntity();
	}

	/** 목록(시군) + 옵션 필터 */
	@GetMapping("/list/{sigunNm}")
	public ResponseEntity<Response<List<ParkingMapDto>>> list(@PathVariable String sigunNm,
															  @RequestParam(required = false) String q,
															  @RequestParam(required = false) String paid, // Y or N
															  @RequestParam(defaultValue = "false") boolean geoOnly) {
		return Response.ok(service.getList(sigunNm, q, paid, geoOnly)).toResponseEntity();
	}

	// ===== 경기도 전체 보기(시군 없이) =====
	/** 지도용 전체 */
	@GetMapping("/map")
	public ResponseEntity<Response<List<ParkingMapDto>>> mapAll(@RequestParam(required = false) String paid, // Y or N
																@RequestParam(required = false) String q,
																@RequestParam(defaultValue = "true") boolean geoOnly) {
		return Response.ok(service.getMapAll(paid, q, geoOnly)).toResponseEntity();
	}

	/** 목록 전체 */
	@GetMapping("/list")
	public ResponseEntity<Response<List<ParkingMapDto>>> listAll(@RequestParam(required = false) String paid, // Y or N
																 @RequestParam(required = false) String q,
																 @RequestParam(defaultValue = "false") boolean geoOnly) {
		return Response.ok(service.getListAll(paid, q, geoOnly)).toResponseEntity();
	}

	/** 상세 전체(시군 불문) */
	@GetMapping("/detail/{parkingId}")
	public ResponseEntity<Response<ParkingDetailDto>> detailGlobal(@PathVariable String parkingId) {
		return Response.ok(service.getDetailGlobal(parkingId)).toResponseEntity();
	}
}
