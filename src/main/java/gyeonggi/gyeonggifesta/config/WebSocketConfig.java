package gyeonggi.gyeonggifesta.config;

import gyeonggi.gyeonggifesta.chat.service.chatting.StompInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final StompInterceptor stompInterceptor;

	/**
	 * STOMP 엔드포인트 설정
	 * 클라이언트가 웹소켓 연결을 맺을 때 사용할 URL 경로 설정
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws-stomp")   // 웹소켓 연결 엔드포인트
			.setAllowedOriginPatterns("*")  // CORS 설정
			.withSockJS();
	}

	/**
	 * 메시지 브로커 설정
	 * 메시지 라우팅에 사용될 prefix 설정
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 클라이언트로 메시지를 보낼 때 사용할 prefix
		registry.enableSimpleBroker("/topic", "/queue");

		// 메시지를 수신할 때 사용할 prefix (애플리케이션으로 라우팅)
		registry.setApplicationDestinationPrefixes("/app");

		// 특정 사용자에게 메시지 전송시 사용할 prefix
		registry.setUserDestinationPrefix("/user");
	}

	/**
	 * 웹소켓 메시지 인터셉터 등록
	 * 사용자 인증/인가 및 추가 처리를 위한 인터셉터 설정
	 */
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompInterceptor);
	}
}
