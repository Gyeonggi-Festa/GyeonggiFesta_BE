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

	/**
	 * 소셜 로그인 공통 처리
	 *
	 * - verifyId 기준으로 기존 계정 조회
	 * - ROLE_DELETED 인 경우: 재가입 플로우 (계정 복구 + ROLE_SEMI_USER 전환)
	 * - 없으면 신규 가입
	 */
	private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
		ClientRegistration clientRegistration = userRequest.getClientRegistration();
		OAuth2ProviderUser oAuth2UserInfo =
				OAuth2ProviderFactory.getOAuth2UserInfo(clientRegistration, oAuth2User);

		String verifyId = oAuth2UserInfo.getVerifyId();
		String email = oAuth2UserInfo.getEmail();

		// 1) verifyId 로 기존 계정 조회 (소셜 계정 1:1 매핑)
		Member member = memberRepository.findByVerifyId(verifyId).orElse(null);

		if (member != null) {
			// 1-1) 탈퇴한 계정이면 → 재가입 플로우
			if (member.getRole() == Role.ROLE_DELETED) {

				// 다른 활성 계정이 이미 이 이메일을 쓰고 있으면 재가입 불가
				// (탈퇴 시 이메일을 dummy 로 바꿔두기 때문에, 여기서 true 면 진짜 다른 사람 계정임)
				if (memberRepository.existsByEmail(email)) {
					OAuth2Error oauth2Error = new OAuth2Error(
							"email_duplicated",
							"이 이메일은 이미 사용 중입니다.",
							null
					);
					throw new OAuth2AuthenticationException(oauth2Error, "이메일 중복 오류(재가입)");
				}

				// 기존 row 재활성화: 온보딩 전 상태(ROLE_SEMI_USER)로 되돌리기
				member.setRole(Role.ROLE_SEMI_USER);
				member.setEmail(email);
				member.setUsername(null);
				member.setGender(null);
				member.setBirthDay(null);

				log.info("[OAuth2] withdrawn user re-joined. verifyId={}, memberId={}",
						verifyId, member.getId());
			}
			// ROLE_USER / ROLE_SEMI_USER / ROLE_ADMIN 등 active 계정이면 그대로 로그인
		} else {
			// 2) 처음 로그인하는 소셜 계정 → 신규 회원 생성
			member = register(oAuth2UserInfo);
			log.info("[OAuth2] new social user registered. verifyId={}, memberId={}",
					verifyId, member.getId());
		}

		// 여기까지 오면 ROLE_DELETED 인 상태는 더 이상 없음(재가입 시 ROLE_SEMI_USER로 바꿈)
		LoginDto loginDto = LoginDto.builder()
				.email(member.getEmail())
				.verifyId(member.getVerifyId())
				.role(member.getRole().name())
				.build();

		return CustomUserDetails.create(loginDto, oAuth2User.getAttributes());
	}

	/**
	 * 새 소셜 사용자 등록
	 *
	 * - 같은 이메일이 이미 다른 활성 계정에서 사용 중이면 막기
	 *   (탈퇴 계정은 withdraw 시 dummy 이메일로 바뀌므로 여기서 안 걸림)
	 */
	private Member register(OAuth2ProviderUser userInfo) {
		String email = userInfo.getEmail();
		String verifyId = userInfo.getVerifyId();

		// 이미 다른 활성 유저가 사용 중인 이메일이면 가입 불가
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
				.verifyId(verifyId)
				.role(Role.ROLE_SEMI_USER)  // 온보딩 전 상태
				.build();

		memberRepository.save(newMember);
		return newMember;
	}
}
