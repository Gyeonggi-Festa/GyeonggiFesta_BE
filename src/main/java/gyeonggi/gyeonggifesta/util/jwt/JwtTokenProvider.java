package gyeonggi.gyeonggifesta.util.jwt;

import gyeonggi.gyeonggifesta.auth.custom.CustomUserDetails;
import gyeonggi.gyeonggifesta.auth.exception.AuthErrorCode;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.member.enums.Role;
import gyeonggi.gyeonggifesta.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private final MemberRepository memberRepository;

	/**
	 * 로컬에서는 Redis 미구동일 수 있으므로 선택 주입.
	 * 운영에서는 정상 주입되어 기존 로직 그대로 동작합니다.
	 */
	@Autowired(required = false)
	private StringRedisTemplate redisTemplate;

	// 액세스 토큰 만료 시간 (1시간)
	private static final long ACCESS_TOKEN_EXPIRE_TIME = 60 * 60 * 1000L;
	// 리프레시 토큰 만료 시간 (7일)
	private static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

	@Value("${jwt.secret}")
	private String JWT_SECRET;

	public static Key getKeyFromString(String secret) {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 액세스 토큰 생성
	 */
	public String generateAccessToken(CustomUserDetails userDetails) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);
		Map<String, Object> claims = Map.of(
				"email", userDetails.getEmail(),
				"role", userDetails.getAuthorities().iterator().next().getAuthority()
		);

		return createToken(userDetails.getName(), now, expiryDate, claims);
	}

	/**
	 * 리프레시 토큰 생성 (+ Redis 저장; 로컬/미구동시 무시)
	 */
	public String generateRefreshToken(CustomUserDetails userDetails) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME);

		String refreshToken = createToken(userDetails.getName(), now, expiryDate, null);

		// 키: verifyId, 값: refreshToken
		safeSet(userDetails.getName(), refreshToken, REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);

		return refreshToken;
	}

	/**
	 * 리프레시 토큰을 이용해 새 토큰 발급 (토큰 로테이션)
	 */
	public Map<String, String> refreshTokens(String refreshToken) {

		// 1) 토큰 자체 유효성 검증 (서명, 만료 등)
		if (!validateToken(refreshToken)) {
			throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		String verifyId = getClaims(refreshToken).getSubject();

		// 2) 현재 Member 조회
		Member member = memberRepository.findByVerifyId(verifyId)
				.orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));

		// 3) 탈퇴 회원(ROLE_DELETED)은 리프레시 불가
		if (member.getRole() == Role.ROLE_DELETED) {
			throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		// 4) Redis 에 저장된 RT와 비교 (운영: 필수, 로컬/미사용: 스킵)
		if (redisTemplate != null) {
			String storedRefreshToken = safeGet(verifyId);

			// withdraw() / logout 등으로 이미 삭제된 RT → 재사용 불가
			if (storedRefreshToken == null) {
				throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
			}

			checkStoredRefreshToken(storedRefreshToken, refreshToken);
		} else {
			log.warn("[refreshTokens] Redis 미사용/미구동으로 저장된 RT가 없습니다. 로컬 모드로 간주하고 저장 검증을 스킵합니다.");
		}

		// 5) 기존 RT 제거(로컬/미구동 시 무시)
		safeDelete(verifyId);

		// 6) 새 AT/RT 생성
		Date now = new Date();
		Date accessExpiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);
		Date refreshExpiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME);

		Map<String, Object> accessClaims = Map.of(
				"email", member.getEmail(),
				"role", member.getRole()
		);

		String newAccessToken = createToken(verifyId, now, accessExpiryDate, accessClaims);
		String newRefreshToken = createToken(verifyId, now, refreshExpiryDate, null);

		// 7) 새 RT 저장(로컬/미구동 시 무시)
		safeSet(verifyId, newRefreshToken, REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);

		return Map.of(
				"accessToken", newAccessToken,
				"refreshToken", newRefreshToken
		);
	}

	private void checkStoredRefreshToken(String storedRefreshToken, String refreshToken) {
		if (!storedRefreshToken.equals(refreshToken)) {
			throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}
	}

	public boolean validateToken(String token) {
		try {
			Key key = getKeyFromString(JWT_SECRET);
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public Claims getClaims(String token) {
		Key key = getKeyFromString(JWT_SECRET);
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
	}

	public void deleteRefreshToken(String verifyId) {
		safeDelete(verifyId);
	}

	/**
	 * 공통 토큰 생성
	 */
	private String createToken(String subject, Date issuedAt, Date expiration, Map<String, Object> claims) {
		Key key = getKeyFromString(JWT_SECRET);
		var builder = Jwts.builder()
				.setSubject(subject)
				.setIssuedAt(issuedAt)
				.setExpiration(expiration);

		if (claims != null) {
			claims.forEach(builder::claim);
		}
		return builder.signWith(key, SignatureAlgorithm.HS512).compact();
	}

	/* ======================== Redis-safe helpers ======================== */

	private void safeSet(String key, String value, long ttl, TimeUnit unit) {
		if (redisTemplate == null) return;
		try {
			redisTemplate.opsForValue().set(key, value, ttl, unit);
		} catch (Exception e) {
			log.warn("Redis set 실패(로컬 무시): {}", e.getMessage());
		}
	}

	private String safeGet(String key) {
		if (redisTemplate == null) return null;
		try {
			return redisTemplate.opsForValue().get(key);
		} catch (Exception e) {
			log.warn("Redis get 실패(로컬 무시): {}", e.getMessage());
			return null;
		}
	}

	private void safeDelete(String key) {
		if (redisTemplate == null) return;
		try {
			redisTemplate.delete(key);
		} catch (Exception e) {
			log.warn("Redis delete 실패(로컬 무시): {}", e.getMessage());
		}
	}
}
