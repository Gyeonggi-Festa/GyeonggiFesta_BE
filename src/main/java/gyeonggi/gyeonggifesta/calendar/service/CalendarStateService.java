package gyeonggi.gyeonggifesta.calendar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * OAuth state 발급/검증 서비스
 * - memberId 바인딩 + TTL 부여
 * - one-time 검증(소모형)
 */
@Service
@RequiredArgsConstructor
public class CalendarStateService {

    private static final String KEY_PREFIX = "lw:state:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redis;

    /** memberId에 바인딩된 랜덤 state 생성 후 TTL 저장 */
    public String issueStateFor(Long memberId) {
        String state = UUID.randomUUID().toString();
        String key = KEY_PREFIX + state;
        redis.opsForValue().set(key, String.valueOf(memberId), TTL);
        return state;
    }

    /** state를 소모적으로 검증: state→memberId 매칭 확인 후 즉시 삭제 */
    public boolean validateAndConsume(String state, Long requesterMemberId) {
        if (state == null || state.isBlank()) return false;
        String key = KEY_PREFIX + state;
        String owner = redis.opsForValue().get(key);
        if (owner == null) return false;
        redis.delete(key); // one-time
        try {
            return Long.parseLong(owner) == requesterMemberId;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
