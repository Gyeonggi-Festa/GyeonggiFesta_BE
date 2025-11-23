package gyeonggi.gyeonggifesta.member.controller;

import gyeonggi.gyeonggifesta.auth.custom.CustomUserDetails;
import gyeonggi.gyeonggifesta.member.dto.request.InputFeatureReq;
import gyeonggi.gyeonggifesta.member.dto.request.UpdateFeatureReq;
import gyeonggi.gyeonggifesta.member.dto.response.InputFeatureRes;
import gyeonggi.gyeonggifesta.member.dto.response.MemberInfoRes;
import gyeonggi.gyeonggifesta.member.service.MemberService;
import gyeonggi.gyeonggifesta.util.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

	private final MemberService memberService;

	/**
	 * 유저 피처 입력
	 *
	 * @param userDetails 로그인된 유저
	 * @param request     유저 정보
	 * @return AT, RT
	 */
	@PostMapping("/auth/semi/feature")
	public ResponseEntity<Response<InputFeatureRes>> inputFeature(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestBody InputFeatureReq request
	) {
		InputFeatureRes response = memberService.inputFeature(userDetails, request);

		return Response.ok(response).toResponseEntity();
	}

	/**
	 * 유저 정보 업데이트
	 *
	 * @param request 새 유저 정보
	 */
	@PatchMapping("/auth/user/feature")
	public ResponseEntity<Response<Void>> updateFeature(@RequestBody UpdateFeatureReq request) {
		memberService.updateFeature(request);

		return Response.ok().toResponseEntity();
	}

	@GetMapping("/auth/user/info")
	public ResponseEntity<Response<MemberInfoRes>> getInfo() {
		MemberInfoRes memberInfoRes = memberService.getMemberInfo();

		return Response.ok(memberInfoRes).toResponseEntity();
	}

	@GetMapping("/auth/all-user/email/{email}")
	public ResponseEntity<Response<Void>> checkEmailDup(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable String email
	) {
		memberService.validEmail(userDetails, email);

		return Response.ok().toResponseEntity();
	}

	/**
	 * 회원 탈퇴
	 *
	 * @param userDetails 현재 로그인된 유저
	 */
	@DeleteMapping("/auth/user/withdraw")
	public ResponseEntity<Response<Void>> withdraw(@AuthenticationPrincipal CustomUserDetails userDetails) {
		memberService.withdraw(userDetails);
		return Response.ok().toResponseEntity();
	}
}
