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
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final MemberRepository memberRepository;

	/**
	 * OAuth2 로그인 진입점
	 * - @Transactional 로 감싸서, 재가입/재활성화 시 엔티티 변경이 DB에 반영되도록 함
	 */
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

		String email = oAuth2UserInfo.getEmail();
		String verifyId = oAuth2UserInfo.getVerifyId();

		// 1. verifyId 기준 기존 회원 조회
		Optional<Member> memberOpt = memberRepository.findByVerifyId(verifyId);

		Member member;
		if (memberOpt.isPresent()) {
			member = memberOpt.get();

			// 재가입/재활성화: ROLE_DELETED → ROLE_SEMI_USER 로 되돌리기
			if (member.getRole() == Role.ROLE_DELETED) {
				// 온보딩 전 상태로 초기화
				member.setRole(Role.ROLE_SEMI_USER);
				member.setEmail(email);     // withdraw 때 dummy 로 바꿨던 이메일 복구
				member.setUsername(null);
				member.setGender(null);
				member.setBirthDay(null);
			}

		} else {
			// 2. 존재하지 않으면 신규 회원 등록
			member = register(oAuth2UserInfo);
		}

		// 3. 로그인 정보 → CustomUserDetails
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
	 * - 같은 이메일이 이미 다른 계정(ROLE_DELETED 아님)에서 사용 중이면 막기
	 * - 탈퇴 계정(ROLE_DELETED) 또는 같은 verifyId 는 재가입/이어쓰기 허용
	 */
	private Member register(OAuth2ProviderUser userInfo) {
		String email = userInfo.getEmail();
		String verifyId = userInfo.getVerifyId();

		// 이메일 기준 기존 회원 조회
		Optional<Member> existingOpt = memberRepository.findByEmail(email);

		if (existingOpt.isPresent()) {
			Member existing = existingOpt.get();

			boolean isDeleted = existing.getRole() == Role.ROLE_DELETED;
			boolean sameVerifyId = verifyId.equals(existing.getVerifyId());

			// 이미 다른 활성 계정이 그 이메일을 쓰고 있으면 차단
			if (!isDeleted && !sameVerifyId) {
				OAuth2Error oauth2Error = new OAuth2Error(
						"email_duplicated",
						"이 이메일은 이미 사용 중입니다.",
						null
				);
				throw new OAuth2AuthenticationException(oauth2Error, "이메일 중복 오류");
			}
			// ROLE_DELETED 이거나 동일 verifyId 면 밑에서 새 row 생성 허용(재가입/이어쓰기)
		}

		Member newMember = Member.builder()
				.email(email)
				.verifyId(verifyId)
				.role(Role.ROLE_SEMI_USER) // 온보딩 전 상태
				.build();

		return memberRepository.save(newMember);
	}
}
