package gyeonggi.gyeonggifesta.calendar.controller;

import gyeonggi.gyeonggifesta.calendar.dto.CalendarCodeExchangeReq;
import gyeonggi.gyeonggifesta.calendar.error.CalendarErrorCode;
import gyeonggi.gyeonggifesta.calendar.service.CalendarAuthService;
import gyeonggi.gyeonggifesta.calendar.service.CalendarStateService;
import gyeonggi.gyeonggifesta.util.response.Response;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * LINE WORKS 캘린더 OAuth 인가/콜백 컨트롤러
 * - /authorize : 서버에서 state 발급 & 권한동의 페이지로 리다이렉트
 * - /callback/exchange : 프런트 HTTPS 콜백에서 받은 code/state를 서버로 전달해 교환
 */
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarAuthService authService;
    private final CalendarStateService stateService;
    private final SecurityUtil securityUtil;

    /**
     * 권한동의 페이지로 리다이렉트
     * - 서버가 memberId에 바인딩된 state를 발급하여 저장(TTL) 후, authorize URL로 이동
     * - 프런트에서 window.open() 팝업으로 여는 것을 권장
     */
    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        Long memberId = securityUtil.getCurrentMember().getId();
        String state = stateService.issueStateFor(memberId); // 서버 보관용 state (one-time)
        response.sendRedirect(authService.buildAuthorizeUrl(state));
    }

    /**
     * 프런트 HTTPS 콜백 → 서버 교환 엔드포인트
     * - 프런트(Vercel)에서 받은 code/state를 POST로 전달
     * - 이 엔드포인트는 '인증 필요'가 안전 (화이트리스트에 넣지 않기)
     */
    @PostMapping("/callback/exchange")
    public ResponseEntity<Response<Void>> exchange(@RequestBody CalendarCodeExchangeReq req) {
        Long memberId = securityUtil.getCurrentMember().getId();

        if (!stateService.validateAndConsume(req.getState(), memberId)) {
            return Response.<Void>errorResponse(CalendarErrorCode.CAL_STATE_INVALID)
                    .toResponseEntity();
        }


        authService.exchangeAndStore(memberId, req.getCode());
        return Response.ok().toResponseEntity();
    }
}
