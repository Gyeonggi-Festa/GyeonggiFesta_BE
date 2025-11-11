package gyeonggi.gyeonggifesta.calendar.controller;

import gyeonggi.gyeonggifesta.calendar.dto.CalendarAddRes;
import gyeonggi.gyeonggifesta.calendar.service.EventCalendarService;
import gyeonggi.gyeonggifesta.util.response.Response;
import gyeonggi.gyeonggifesta.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarEventController {

    private final EventCalendarService service;
    private final SecurityUtil securityUtil;

    @PostMapping("/events/{eventId}")
    public ResponseEntity<Response<CalendarAddRes>> addEvent(@PathVariable Long eventId) {
        Long memberId = securityUtil.getCurrentMember().getId();
        String externalId = service.createMyCalendarEvent(memberId, eventId);
        CalendarAddRes body = new CalendarAddRes(externalId);
        return Response.ok(body).toResponseEntity();
    }
}