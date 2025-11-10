package gyeonggi.gyeonggifesta.auth.oauth2.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import gyeonggi.gyeonggifesta.auth.custom.CustomUserDetails;
import gyeonggi.gyeonggifesta.util.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;

	@Value("${redirect-url.frontend}")
	private String REDIRECT_URL;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();

		String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
		String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

		String tempCode = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

		Map<String, String> tokenData = Map.of(
			"accessToken", accessToken,
			"refreshToken", refreshToken
		);
		redisTemplate.opsForValue().set(tempCode, objectMapper.writeValueAsString(tokenData),
			5, TimeUnit.MINUTES);

		getRedirectStrategy().sendRedirect(request, response, REDIRECT_URL + "/login-success?code=" + tempCode);
	}
}
