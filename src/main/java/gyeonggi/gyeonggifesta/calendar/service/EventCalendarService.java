package gyeonggi.gyeonggifesta.calendar.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gyeonggi.gyeonggifesta.calendar.domain.CalendarCredential;
import gyeonggi.gyeonggifesta.calendar.domain.CalendarEventLink;
import gyeonggi.gyeonggifesta.calendar.repository.CalendarEventLinkRepository;
import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventCalendarService {

    private final EventRepository eventRepository;
    private final EventToCalendarPayloadMapper mapper;
    private final CalendarTokenService tokenService;
    private final CalendarApiClient apiClient;
    private final CalendarEventLinkRepository linkRepository;
    private final ObjectMapper om = new ObjectMapper();

    @Transactional
    public String createMyCalendarEvent(Long memberId, Long eventId) {
        var ex = linkRepository.findByMemberIdAndEventId(memberId, eventId);
        if (ex.isPresent()) return ex.get().getExternalEventId();

        Event e = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("행사를 찾을 수 없습니다. eventId=" + eventId));

        Map<String,Object> payload = mapper.toPayload(e);

        CalendarCredential cred = tokenService.getValidCredential(memberId);
        String lwUserId = (cred.getLineWorksUserId() == null || cred.getLineWorksUserId().isBlank())
                ? "me" : cred.getLineWorksUserId();

        String raw = apiClient.createByBody(lwUserId, cred.getAccessToken(), payload);

        String externalEventId = extractExternalEventId(raw);
        linkRepository.save(CalendarEventLink.builder()
                .memberId(memberId)
                .eventId(eventId)
                .externalEventId(externalEventId)
                .build());

        return externalEventId;
    }

    private String extractExternalEventId(String rawJson) {
        try {
            JsonNode n = om.readTree(rawJson);
            if (n.has("eventId")) return n.get("eventId").asText();
            if (n.has("id")) return n.get("id").asText();
        } catch (Exception ignore) {}
        return null;
    }
}
