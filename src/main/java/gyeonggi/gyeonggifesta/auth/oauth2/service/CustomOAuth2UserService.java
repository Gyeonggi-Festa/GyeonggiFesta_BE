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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final MemberRepository memberRepository;

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);
		return processOAuth2User(userRequest, oAuth2User);
	}

	private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
		ClientRegistration clientRegistration = userRequest.getClientRegistration();
		OAuth2ProviderUser oAuth2UserInfo =
				OAuth2ProviderFactory.getOAuth2UserInfo(clientRegistration, oAuth2User);

		Optional<Member> memberOpt =
				memberRepository.findByVerifyId(oAuth2UserInfo.getVerifyId());

		Member member = memberOpt
				.map(m -> handleExistingMember(m, oAuth2UserInfo)) // 기존 회원(재가입 포함)
				.orElseGet(() -> register(oAuth2UserInfo));        // 최초 가입

		// 여기까지 오면 ROLE_SEMI_USER 또는 ROLE_USER 상태
		LoginDto loginDto = LoginDto.builder()
				.email(member.getEmail())
				.verifyId(member.getVerifyId())
				.role(member.getRole().name())
				.build();

		return CustomUserDetails.create(loginDto, oAuth2User.getAttributes());
	}

	/**
	 * 기존 회원 처리
	 * - ROLE_DELETED  : 재가입으로 간주, ROLE_SEMI_USER로 되살림
	 * - 그 외 ROLE_*  : 그대로 로그인
	 */
	private Member handleExistingMember(Member member, OAuth2ProviderUser userInfo) {
		if (member.getRole() == Role.ROLE_DELETED) {
			log.info("[OAuth2] withdrawn member 재가입 처리. memberId={}, verifyId={}",
					member.getId(), member.getVerifyId());

			// 카카오에서 받은 이메일로 복구
			member.setEmail(userInfo.getEmail());
			// 온보딩 다시 태우기 위해 ROLE_SEMI_USER 로 되돌림
			member.setRole(Role.ROLE_SEMI_USER);

			// 이전 탈퇴시 남겨둔 값들 초기화
			member.setUsername(null);
			member.setGender(null);
			member.setBirthDay(null);

			// 영속 엔티티라 @Transactional 안에서 flush 됨
		}

		return member;
	}

	/**
	 * 최초 소셜 로그인 회원 가입
	 */
	private Member register(OAuth2ProviderUser userInfo) {
		String email = userInfo.getEmail();

		if (memberRepository.existsByEmail(email)) {
			// 이미 다른 계정에서 사용 중인 이메일이면 예외
			throw new OAuth2AuthenticationException("이 이메일은 이미 사용 중입니다.");
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
