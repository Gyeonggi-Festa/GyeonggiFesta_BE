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

    /**
     * 경기도 전체 row를 10분 캐시
     * - 캐시 키는 항상 'ALL' 고정
     * - 시군별 필터링은 서비스 레벨에서 수행
     */
    @Cacheable(
            value = "parkingRows",
            key = "'ALL'",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<JsonNode> fetchAllRowsCached(String sigunNm) {
        // sigunNm 은 외부 호출에는 사용하지 않고, 시그니처만 유지
        return client.fetchAllRows();
    }
}
