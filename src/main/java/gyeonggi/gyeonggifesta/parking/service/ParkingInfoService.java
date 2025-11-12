package gyeonggi.gyeonggifesta.parking.service;

import gyeonggi.gyeonggifesta.parking.dto.response.ParkingDetailDto;
import gyeonggi.gyeonggifesta.parking.dto.response.ParkingMapDto;

import java.util.List;

public interface ParkingInfoService {
	// 기존 시군 단위
	List<ParkingMapDto> getMap(String sigunNm);
	List<ParkingMapDto> getList(String sigunNm, String q, String paid, boolean geoOnly);
	ParkingDetailDto getDetail(String sigunNm, String parkingId);

	// 경기도 전체
	List<ParkingMapDto> getMapAll(String paid, String q, boolean geoOnly);
	List<ParkingMapDto> getListAll(String paid, String q, boolean geoOnly);
	ParkingDetailDto getDetailGlobal(String parkingId);
}
