package gyeonggi.gyeonggifesta.parking.component;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ParkingApiCache {

    private final ParkingApiClient client;

    /** 시군별 전체 row를 10분 캐시 */
    @Cacheable(value = "parkingRows", key = "#sigunNm", unless = "#result == null || #result.isEmpty()")
    public List<JsonNode> fetchAllRowsCached(String sigunNm) {
        return client.fetchAllRows(sigunNm);
    }
}
