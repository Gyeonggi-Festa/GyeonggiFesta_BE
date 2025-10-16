package gyeonggi.gyeonggifesta.parking.service;

import gyeonggi.gyeonggifesta.parking.dto.response.ParkingDetailDto;
import gyeonggi.gyeonggifesta.parking.dto.response.ParkingMapDto;

import java.util.List;

public interface ParkingInfoService {
	List<ParkingMapDto> getMap(String sigunNm);
	ParkingDetailDto getDetail(String sigunNm, String parkingId);
}
