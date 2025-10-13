package gyeonggi.gyeonggifesta.auth.custom;

import gyeonggi.gyeonggifesta.auth.dto.LoginDto;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomUserDetails implements OAuth2User {

	private final LoginDto loginDto;

	// OAuth2 로그인 시 제공되는 추가 속성들
	private Map<String, Object> attributes;

	public CustomUserDetails(LoginDto loginDto) {
		this.loginDto = loginDto;
	}

	/**
	 * 자체 로그인 시 사용할 팩토리 메서드
	 *
	 * @param loginDto 로그인에 필요한 사용자 정보
	 * @return CustomUserDetails 객체
	 */
	public static CustomUserDetails create(LoginDto loginDto) {
		return new CustomUserDetails(loginDto);
	}

	/**
	 * OAuth2 로그인 시 사용할 팩토리 메서드
	 *
	 * @param loginDto   로그인에 필요한 사용자 정보
	 * @param attributes OAuth2 제공자로부터 받은 속성들
	 * @return CustomUserDetails 객체
	 */
	public static CustomUserDetails create(LoginDto loginDto, Map<String, Object> attributes) {
		CustomUserDetails userDetails = new CustomUserDetails(loginDto);
		userDetails.setAttributes(attributes);
		return userDetails;
	}

	/**
	 * JWT Claims를 이용하여 CustomUserDetails 객체를 생성하는 메서드.
	 * 토큰에는 verifyId가 subject로, email과 role은 클레임으로 저장되었다고 가정합니다.
	 *
	 * @param claims JWT 토큰에서 추출한 Claims 객체
	 * @return CustomUserDetails 객체
	 */
	public static CustomUserDetails createCustomUserDetailsFromClaims(Claims claims) {
		String verifyId = claims.getSubject();
		String email = (String)claims.get("email");
		String role = (String)claims.get("role");

		LoginDto loginDto = LoginDto.builder()
			.verifyId(verifyId)
			.email(email)
			.role(role)
			.build();

		return create(loginDto);
	}

	public String getEmail() {
		return loginDto.getEmail();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	/**
	 * OAuth2 속성들을 설정
	 *
	 * @param attributes OAuth2 제공자로부터 받은 속성들
	 */
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String getName() {
		return String.valueOf(loginDto.getVerifyId());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singleton(new SimpleGrantedAuthority(loginDto.getRole()));
	}
}
