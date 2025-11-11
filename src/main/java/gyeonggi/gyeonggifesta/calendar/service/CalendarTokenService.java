package gyeonggi.gyeonggifesta.calendar.service;

import gyeonggi.gyeonggifesta.calendar.config.CalendarProperties;
import gyeonggi.gyeonggifesta.calendar.domain.CalendarCredential;
import gyeonggi.gyeonggifesta.calendar.repository.CalendarCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
public class CalendarTokenService {

    private final CalendarProperties props;
    private final CalendarCredentialRepository repo;
    private final RestTemplate rt;

    @Autowired
    public CalendarTokenService(
            CalendarProperties props,
            CalendarCredentialRepository repo,
            @Qualifier("lineWorksRestTemplate") RestTemplate rt) {
        this.props = props;
        this.repo = repo;
        this.rt = rt;
    }

    public CalendarCredential getValidCredential(Long memberId) {
        CalendarCredential cred = repo.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalStateException("LINE WORKS 미연동 사용자입니다."));
        if (cred.getAccessTokenExpiresAt() == null ||
                cred.getAccessTokenExpiresAt().minusSeconds(60).isBefore(Instant.now())) {
            refresh(cred);
        }
        return cred;
    }

    private void refresh(CalendarCredential cred) {
        String url = props.getAuthBase() + "/token";
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", cred.getRefreshToken());
        form.add("client_id", props.getClientId());
        form.add("client_secret", props.getClientSecret());

        ResponseEntity<java.util.Map> resp = rt.postForEntity(url, new HttpEntity<>(form, h), java.util.Map.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new IllegalStateException("LINE WORKS token refresh failed: " + resp.getStatusCode());
        }
        java.util.Map body = resp.getBody();
        String newAccess = (String) body.get("access_token");
        Number expiresIn = (Number) body.get("expires_in");
        cred.setAccessToken(newAccess);
        cred.setAccessTokenExpiresAt(Instant.now().plusSeconds(expiresIn != null ? expiresIn.longValue() : 3600));
        if (body.get("refresh_token") != null) cred.setRefreshToken((String) body.get("refresh_token"));
        repo.save(cred);
    }
}
