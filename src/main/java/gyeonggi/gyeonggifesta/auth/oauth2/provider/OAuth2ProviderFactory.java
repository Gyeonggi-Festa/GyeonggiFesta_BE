package gyeonggi.gyeonggifesta.auth.oauth2.provider;

import gyeonggi.gyeonggifesta.auth.exception.AuthErrorCode;
import gyeonggi.gyeonggifesta.auth.oauth2.provider.kakao.KakaoUser;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class OAuth2ProviderFactory {
	public static OAuth2ProviderUser getOAuth2UserInfo(ClientRegistration clientRegistration, OAuth2User oAuth2User) {

		String registrationId = clientRegistration.getRegistrationId();

		if (registrationId.equals(OAuth2Provider.KAKAO.getRegistrationId())) {
			return new KakaoUser(oAuth2User, clientRegistration);
		} else {
			throw new BusinessException(AuthErrorCode.INVALID_OAUTH2_PROVIDER);
		}
	}
}
