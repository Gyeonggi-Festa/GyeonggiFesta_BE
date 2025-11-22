package gyeonggi.gyeonggifesta.member.service;

import gyeonggi.gyeonggifesta.auth.custom.CustomUserDetails;
import gyeonggi.gyeonggifesta.member.dto.request.InputFeatureReq;
import gyeonggi.gyeonggifesta.member.dto.request.UpdateFeatureReq;
import gyeonggi.gyeonggifesta.member.dto.response.InputFeatureRes;
import gyeonggi.gyeonggifesta.member.dto.response.MemberInfoRes;

public interface MemberService {

	/**
	 * 유저 피처 입력
	 *
	 * @param userDetails 로그인된 유저
	 * @param request     유저 정보
	 * @return AT, RT
	 */
	InputFeatureRes inputFeature(CustomUserDetails userDetails, InputFeatureReq request);

	/**
	 * 유저 정보 업데이트
	 * @param request 새 유저 정보
	 */
	void updateFeature(UpdateFeatureReq request);

	MemberInfoRes getMemberInfo();

	void validEmail(CustomUserDetails userDetails, String email);

	/**
	 * 회원 탈퇴
	 *
	 * @param userDetails 현재 로그인된 유저
	 */
	void withdraw(CustomUserDetails userDetails);
}
