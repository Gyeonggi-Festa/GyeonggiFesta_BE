package gyeonggi.gyeonggifesta.calendar.service;

import gyeonggi.gyeonggifesta.calendar.config.CalendarProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CalendarApiClient {

    private final CalendarProperties props;
    private final RestTemplate rt;

    @Autowired
    public CalendarApiClient(CalendarProperties props,
                             @Qualifier("lineWorksRestTemplate") RestTemplate rt) {
        this.props = props;
        this.rt = rt;
    }

    public String createByBody(String lineWorksUserId, String accessToken, Map<String,Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = props.getApiBase() + "/users/" + lineWorksUserId + "/calendar/events";
        try {
            ResponseEntity<String> resp =
                    rt.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            return resp.getBody();
        } catch (HttpStatusCodeException e) {
            throw new IllegalStateException("LINE WORKS create event failed: " +
                    e.getStatusCode() + " " + e.getResponseBodyAsString());
        }
    }
}
