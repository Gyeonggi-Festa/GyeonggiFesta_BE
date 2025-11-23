package gyeonggi.gyeonggifesta.member.service;

import gyeonggi.gyeonggifesta.auth.custom.CustomUserDetails;
import gyeonggi.gyeonggifesta.auth.dto.LoginDto;
import gyeonggi.gyeonggifesta.auth.exception.AuthErrorCode;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.dto.request.InputFeatureReq;
import gyeonggi.gyeonggifesta.member.dto.request.UpdateFeatureReq;
import gyeonggi.gyeonggifesta.member.dto.response.InputFeatureRes;
import gyeonggi.gyeonggifesta.member.dto.response.MemberInfoRes;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.member.enums.Role;
import gyeonggi.gyeonggifesta.member.repository.MemberRepository;
import gyeonggi.gyeonggifesta.util.jwt.JwtTokenProvider;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

	private final MemberRepository memberRepository;
	private final SecurityUtil securityUtil;
	private final JwtTokenProvider jwtTokenProvider;

	/**
	 * 유저 피처 입력
	 *
	 * @param userDetails 로그인된 유저W
	 * @param request     유저 정보
	 * @return AT, RT
	 */
	@Override
	@Transactional
	public InputFeatureRes inputFeature(CustomUserDetails userDetails, InputFeatureReq request) {

		Member currentMember = securityUtil.getCurrentMember();

		validRoleSemi(currentMember);

		validEmail(userDetails, request.getEmail());

		inputUserInfo(currentMember, request);
		currentMember.setRole(Role.ROLE_USER);

		jwtTokenProvider.deleteRefreshToken(currentMember.getVerifyId());

		LoginDto updatedLoginDto = LoginDto.builder()
				.verifyId(currentMember.getVerifyId())
				.role(currentMember.getRole().name())
				.email(currentMember.getEmail())
				.build();

		CustomUserDetails updatedUserDetails = CustomUserDetails.create(updatedLoginDto);

		String accessToken = jwtTokenProvider.generateAccessToken(updatedUserDetails);
		String refreshToken = jwtTokenProvider.generateRefreshToken(updatedUserDetails);

		return InputFeatureRes.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();
	}

	private void validRoleSemi(Member member) {
		if (!member.getRole().equals(Role.ROLE_SEMI_USER)) {
			throw new BusinessException(AuthErrorCode.INVALID_ROLE);
		}
	}

	@Override
	public void validEmail(CustomUserDetails userDetails, String email) {
		String currentEmail = userDetails.getEmail();
		if (currentEmail != null && currentEmail.equals(email)) {
			return;
		}

		// 다른 사용자와 이메일 중복 검사
		if (memberRepository.existsByEmail(email)) {
			throw new BusinessException(AuthErrorCode.EMAIL_DUPLICATED);
		}
	}

	private void inputUserInfo(Member member, InputFeatureReq request) {
		member.setUsername(request.getUsername());
		member.setGender(request.getGender());
		member.setBirthDay(request.getBirthday());
		member.setEmail(request.getEmail());
	}

	/**
	 * 유저 정보 업데이트
	 * @param request 새 유저 정보
	 */
	@Override
	@Transactional
	public void updateFeature(UpdateFeatureReq request) {
		Member currentMember = securityUtil.getCurrentMember();

		updateUserInfo(currentMember, request);
	}

	private void updateUserInfo(Member member, UpdateFeatureReq request) {
		member.setUsername(request.getUsername());
		member.setGender(request.getGender());
		member.setBirthDay(request.getBirthday());
		member.setEmail(request.getEmail());
	}

	@Override
	public MemberInfoRes getMemberInfo() {

		Member member = securityUtil.getCurrentMember();

		return MemberInfoRes.builder()
				.memberId(member.getId())
				.verifyId(member.getVerifyId())
				.username(member.getUsername())
				.gender(member.getGender())
				.email(member.getEmail())
				.build();
	}

	/**
	 * 회원 탈퇴 (소프트 삭제 + 익명 처리)
	 *
	 * - RT 삭제
	 * - 개인정보 익명화
	 * - ROLE_DELETED 설정
	 * - SecurityContext 정리
	 */
	@Override
	@Transactional
	public void withdraw(CustomUserDetails userDetails) {
		Member member = securityUtil.getCurrentMember();

		// 1) Refresh Token 삭제
		jwtTokenProvider.deleteRefreshToken(member.getVerifyId());

		// 2) 개인정보 익명 처리
		member.setUsername("탈퇴한 회원");
		// 동일 이메일 재사용 방지를 위해 dummy 이메일로 변경
		member.setEmail("deleted-user-" + member.getId() + "@deleted.local");
		member.setBirthDay(null);
		member.setGender(null);
		member.setRole(Role.ROLE_DELETED);

		// 3) SecurityContext 정리
		SecurityContextHolder.clearContext();
	}
}
