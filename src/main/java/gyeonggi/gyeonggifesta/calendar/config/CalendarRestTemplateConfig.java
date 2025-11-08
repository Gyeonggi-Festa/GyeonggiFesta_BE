package gyeonggi.gyeonggifesta.calendar.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Configuration
public class CalendarRestTemplateConfig {

    @Bean(name = "lineWorksRestTemplate")
    public RestTemplate lineWorksRestTemplate(CalendarProperties props,
                                              RestTemplateBuilder builder) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getConnectTimeoutMs());
        factory.setReadTimeout(props.getReadTimeoutMs());

        return builder
                .requestFactory(() -> factory)
                .errorHandler(new DefaultResponseErrorHandler() {
                    @Override
                    public void handleError(ClientHttpResponse response) throws IOException {
                        // 예외 던지지 않고 service 쪽에서 처리하도록
                    }
                })
                .build();
    }
}
