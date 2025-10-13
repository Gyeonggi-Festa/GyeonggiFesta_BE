package gyeonggi.gyeonggifesta.auth.oauth2.provider;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Map;

public interface ProviderUser {
    String getProvider();

    String getVerifyId();

    String getEmail();

    List<? extends GrantedAuthority> getAuthorities();

    Map<String, Object> getAttributes();
}
