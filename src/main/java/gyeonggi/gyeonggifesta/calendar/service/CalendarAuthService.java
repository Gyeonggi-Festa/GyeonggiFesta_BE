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
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Map;

@Service
public class CalendarAuthService {

    private final CalendarProperties props;
    private final CalendarCredentialRepository repo;
    private final RestTemplate rt;

    @Autowired
    public CalendarAuthService(
            CalendarProperties props,
            CalendarCredentialRepository repo,
            @Qualifier("lineWorksRestTemplate") RestTemplate rt) {
        this.props = props;
        this.repo = repo;
        this.rt = rt;
    }

    public String buildAuthorizeUrl(String state) {
        return UriComponentsBuilder.fromHttpUrl(props.getAuthBase() + "/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", props.getClientId())
                .queryParam("redirect_uri", props.getRedirectUri())
                .queryParam("scope", props.getScope())
                .queryParam("state", state)
                .build(true).toUriString();
    }

    public void exchangeAndStore(Long memberId, String code) {
        String url = props.getAuthBase() + "/token";
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", props.getClientId());
        form.add("client_secret", props.getClientSecret());
        form.add("redirect_uri", props.getRedirectUri());

        ResponseEntity<Map> resp = rt.postForEntity(url, new HttpEntity<>(form, h), Map.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new IllegalStateException("LINE WORKS token exchange failed: " + resp.getStatusCode());
        }
        Map body = resp.getBody();
        String access  = (String) body.get("access_token");
        String refresh = (String) body.get("refresh_token");
        String tokenType = (String) body.get("token_type");
        String scope = (String) body.get("scope");
        Number expiresIn = (Number) body.get("expires_in");
        Instant expAt = Instant.now().plusSeconds(expiresIn != null ? expiresIn.longValue() : 3600);

        CalendarCredential cred = repo.findByMemberId(memberId)
                .orElse(CalendarCredential.builder().memberId(memberId).build());
        cred.setAccessToken(access);
        cred.setRefreshToken(refresh);
        cred.setAccessTokenExpiresAt(expAt);
        cred.setTokenType(tokenType);
        cred.setScope(scope);
        repo.save(cred);
    }
}
