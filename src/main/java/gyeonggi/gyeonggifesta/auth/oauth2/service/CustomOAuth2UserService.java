package gyeonggi.gyeonggifesta.auth.oauth2.service;

import gyeonggi.gyeonggifesta.auth.custom.CustomUserDetails;
import gyeonggi.gyeonggifesta.auth.dto.LoginDto;
import gyeonggi.gyeonggifesta.auth.oauth2.provider.OAuth2ProviderFactory;
import gyeonggi.gyeonggifesta.auth.oauth2.provider.OAuth2ProviderUser;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.member.enums.Role;
import gyeonggi.gyeonggifesta.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final MemberRepository memberRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);

		return processOAuth2User(userRequest, oAuth2User);
	}

	private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
		ClientRegistration clientRegistration = userRequest.getClientRegistration();
		OAuth2ProviderUser oAuth2UserInfo = OAuth2ProviderFactory.getOAuth2UserInfo(clientRegistration, oAuth2User);

		Optional<Member> memberOpt = memberRepository.findByVerifyId(oAuth2UserInfo.getVerifyId());
		Member member = memberOpt.orElseGet(() -> register(oAuth2UserInfo));

		// 탈퇴 회원(Role.DELETED)은 로그인 불가
		if (member.getRole() == Role.ROLE_DELETED) {
			OAuth2Error oauth2Error = new OAuth2Error(
					"withdrawn_user",
					"탈퇴한 계정입니다.",
					null
			);
			throw new OAuth2AuthenticationException(oauth2Error, "탈퇴한 계정으로 로그인 시도");
		}

		LoginDto loginDto = LoginDto.builder()
				.email(member.getEmail())
				.verifyId(member.getVerifyId())
				.role(member.getRole().name())
				.build();

		return CustomUserDetails.create(loginDto, oAuth2User.getAttributes());
	}

	private Member register(OAuth2ProviderUser userInfo) {

		String email = userInfo.getEmail();

		if (memberRepository.existsByEmail(email)) {
			OAuth2Error oauth2Error = new OAuth2Error(
					"email_duplicated",
					"이 이메일은 이미 사용 중입니다.",
					null
			);
			throw new OAuth2AuthenticationException(oauth2Error, "이메일 중복 오류");
		}

		Member newMember = Member.builder()
				.email(email)
				.verifyId(userInfo.getVerifyId())
				.role(Role.ROLE_SEMI_USER)
				.build();

		memberRepository.save(newMember);

		return newMember;
	}
}
