package gyeonggi.gyeonggifesta.auth.oauth2.provider;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;

public interface ProviderUser {
    String getProvider();

    String getVerifyId();

    String getEmail();

    List<? extends GrantedAuthority> getAuthorities();

    Map<String, Object> getAttributes();
}
