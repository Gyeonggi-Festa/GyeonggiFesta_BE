package gyeonggi.gyeonggifesta.util.security;

import gyeonggi.gyeonggifesta.auth.custom.CustomUserDetails;
import gyeonggi.gyeonggifesta.auth.exception.AuthErrorCode;
import gyeonggi.gyeonggifesta.exception.BusinessException;
import gyeonggi.gyeonggifesta.member.entity.Member;
import gyeonggi.gyeonggifesta.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

	private final MemberRepository memberRepository;

	public Member getCurrentMember() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
			String verifyId = userDetails.getName();

			return memberRepository.findByVerifyId(verifyId)
				.orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));
		}

		throw new BusinessException(AuthErrorCode.USER_NOT_FOUND);
	}

	public Member getCurrentMember(String verifyId) {
		return memberRepository.findByVerifyId(verifyId)
			.orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));
	}

	/**
	 * 현재 인증된 사용자의 Member 객체를 Optional로 반환
	 * 사용자를 찾을 수 없는 경우 빈 Optional 반환
	 *
	 * @return 현재 인증된 사용자의 Member를 담은 Optional 객체
	 */
	public Optional<Member> getCurrentMemberOpt() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
			String verifyId = userDetails.getName();
			return memberRepository.findByVerifyId(verifyId);
		}

		return Optional.empty();
	}

}
