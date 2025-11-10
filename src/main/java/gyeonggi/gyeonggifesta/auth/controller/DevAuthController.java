//package gyeonggi.gyeonggifesta.auth.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import gyeonggi.gyeonggifesta.auth.custom.CustomUserDetails;
//import gyeonggi.gyeonggifesta.auth.dto.LoginDto;
//import gyeonggi.gyeonggifesta.util.jwt.JwtTokenProvider;
//import gyeonggi.gyeonggifesta.util.response.Response;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Profile;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
//@Profile({"local","dev"}) // 운영 배포 시 자동 비활성화
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/dev")
//public class DevAuthController {
//
//    private final JwtTokenProvider jwtTokenProvider;
//    private final StringRedisTemplate redisTemplate;
//    private final ObjectMapper objectMapper;
//
//    /**
//     * 임시코드 생성(교환 플로우 유지)
//     * - body에 email/verifyId/role을 넘기면 AT/RT를 만들어 Redis에 저장하고 code를 반환
//     * - 이후 /api/token/exchange?code=... 로 실제 코드 플로우 그대로 테스트 가능
//     */
//    @PostMapping("/temp-code")
//    public ResponseEntity<Response<Map<String, String>>> issueTempCode(
//            @RequestParam String email,
//            @RequestParam String verifyId,
//            @RequestParam(defaultValue = "ROLE_SEMI_USER") String role
//    ) throws Exception {
//        var user = CustomUserDetails.create(LoginDto.builder()
//                .email(email).verifyId(verifyId).role(role).build());
//        String at = jwtTokenProvider.generateAccessToken(user);
//        String rt = jwtTokenProvider.generateRefreshToken(user);
//
//        try {
//            String code = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
//            var tokenData = Map.of("accessToken", at, "refreshToken", rt);
//            redisTemplate.opsForValue().set(code, objectMapper.writeValueAsString(tokenData),
//                    5, TimeUnit.MINUTES);
//            return Response.ok(Map.of("code", code)).toResponseEntity();
//        } catch (org.springframework.data.redis.RedisConnectionFailureException e) {
//            // 개발 편의: Redis 없으면 바로 토큰 반환
//            return Response.ok(Map.of(
//                    "accessToken", at,
//                    "refreshToken", rt,
//                    "role", role,
//                    "devNote", "redis-off"
//            )).toResponseEntity();
//        }
//    }
//
//    /**
//     * (옵션) AT/RT 바로 발급 (교환 과정 생략 테스트용)
//     */
//    @PostMapping("/login")
//    public ResponseEntity<Response<Map<String, String>>> devLogin(
//            @RequestParam String email,
//            @RequestParam String verifyId,
//            @RequestParam(defaultValue = "ROLE_SEMI_USER") String role
//    ) {
//        LoginDto login = LoginDto.builder()
//                .email(email)
//                .verifyId(verifyId)
//                .role(role)
//                .build();
//
//        CustomUserDetails user = CustomUserDetails.create(login);
//        String accessToken = jwtTokenProvider.generateAccessToken(user);
//        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
//
//        return Response.ok(Map.of(
//                "accessToken", accessToken,
//                "refreshToken", refreshToken,
//                "role", role
//        )).toResponseEntity();
//    }
//}
