package gyeonggi.gyeonggifesta.calendar.controller;

import gyeonggi.gyeonggifesta.calendar.service.CalendarAuthService;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarAuthService authService;
    private final SecurityUtil securityUtil;

    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        Long memberId = securityUtil.getCurrentMember().getId();
        String state = memberId + ":" + UUID.randomUUID();
        response.sendRedirect(authService.buildAuthorizeUrl(state));
    }

    @GetMapping(value = "/callback", produces = MediaType.TEXT_HTML_VALUE)
    public String callback(@RequestParam String code,
                           @RequestParam(required = false) String state) {
        Long memberId = parseMemberIdFromState(state, securityUtil.getCurrentMember().getId());
        authService.exchangeAndStore(memberId, code);
        return "<script>alert('캘린더 연동이 완료되었습니다.');window.close();</script>";
    }

    private Long parseMemberIdFromState(String state, Long fallback) {
        try {
            if (state == null) return fallback;
            String s = state.split(":")[0];
            return Long.parseLong(s);
        } catch (Exception e) {
            return fallback;
        }
    }
}
