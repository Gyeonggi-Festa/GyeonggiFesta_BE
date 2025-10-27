package gyeonggi.gyeonggifesta.parking.controller;

import gyeonggi.gyeonggifesta.parking.dto.response.ParkingDetailDto;
import gyeonggi.gyeonggifesta.parking.dto.response.ParkingMapDto;
import gyeonggi.gyeonggifesta.parking.service.ParkingInfoService;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth/user/parking")
@RequiredArgsConstructor
public class ParkingInfoController {

	private final ParkingInfoService service;

	// 지도용 목록
	@GetMapping("/map/{sigunNm}")
	public ResponseEntity<Response<List<ParkingMapDto>>> map(@PathVariable String sigunNm) {
		return Response.ok(service.getMap(sigunNm)).toResponseEntity();
	}

	// 상세
	@GetMapping("/detail/{sigunNm}/{parkingId}")
	public ResponseEntity<Response<ParkingDetailDto>> detail(@PathVariable String sigunNm,
															 @PathVariable String parkingId) {
		return Response.ok(service.getDetail(sigunNm, parkingId)).toResponseEntity();
	}
}
