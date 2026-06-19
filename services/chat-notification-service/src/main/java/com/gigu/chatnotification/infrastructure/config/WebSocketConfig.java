package com.gigu.chatnotification.infrastructure.config;

import com.gigu.chatnotification.infrastructure.eda.ExternalEdaConfig;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173,https://*.vercel.app}")
    private String corsAllowedOrigins;

    private final ExternalEdaConfig edaConfig;

    public WebSocketConfig(ExternalEdaConfig edaConfig) {
        this.edaConfig = edaConfig;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(edaConfig.webSocketPath())
            .setAllowedOriginPatterns(Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toArray(String[]::new));
    }
}
