package gyeonggi.gyeonggifesta.config;

import gyeonggi.gyeonggifesta.auth.jwt.JwtFilter;
import gyeonggi.gyeonggifesta.auth.oauth2.handler.OAuth2FailureHandler;
import gyeonggi.gyeonggifesta.auth.oauth2.handler.OAuth2SuccessHandler;
import gyeonggi.gyeonggifesta.auth.oauth2.service.CustomOAuth2UserService;
import gyeonggi.gyeonggifesta.util.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private static final List<String> CORS_WHITELIST = List.of(
			"http://localhost:5173",
			"http://127.0.0.1:5500"
	);

	private static final List<String> WHITELIST = List.of(
			"/login",
			"/api/register",
			"/api/login",
			"/api/token/**",
			"/api/dev/**",  // 개발용 임시 로그인
			"/ws-stomp/**",
			"/batch/event-sync/run", // 수동 배치 -> 개발때문에 잠시 넣은거
			"/api/calendar/authorize",
			"/api/calendar/callback"
	);

	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final OAuth2FailureHandler oAuth2FailureHandler;
	private final JwtTokenProvider jwtTokenProvider;

	@Bean
	@Profile("local")
	public SecurityFilterChain localFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.oauth2Login(AbstractHttpConfigurer::disable) // 완전 비활성화

				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth
						.requestMatchers(WHITELIST.toArray(new String[0])).permitAll()
						.anyRequest().authenticated())

				.addFilterBefore(new JwtFilter(jwtTokenProvider),
						UsernamePasswordAuthenticationFilter.class)

				.cors(cors -> cors.configurationSource(corsConfigurationSource()));

		return http.build();
	}

	@Bean
	@Profile("!local") // 운영/개발 서버
	public SecurityFilterChain prodFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

				.oauth2Login(oauth2 -> oauth2
						.userInfoEndpoint(config -> config.userService(customOAuth2UserService))
						.successHandler(oAuth2SuccessHandler)
						.failureHandler(oAuth2FailureHandler))

				.authorizeHttpRequests(auth -> auth
						.requestMatchers(WHITELIST.toArray(new String[0])).permitAll()
						.anyRequest().authenticated())

				.addFilterBefore(new JwtFilter(jwtTokenProvider),
						UsernamePasswordAuthenticationFilter.class)
				.cors(cors -> cors.configurationSource(corsConfigurationSource()));

		return http.build();
	}

	private CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(CORS_WHITELIST);
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		configuration.setAllowCredentials(true);
		configuration.setAllowedHeaders(Collections.singletonList("*"));
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
