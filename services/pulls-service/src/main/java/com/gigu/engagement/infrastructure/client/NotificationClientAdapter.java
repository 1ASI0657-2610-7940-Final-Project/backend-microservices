package com.gigu.engagement.infrastructure.client;

import com.gigu.engagement.application.port.out.NotificationClientPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class NotificationClientAdapter implements NotificationClientPort {
    private static final Logger log = LoggerFactory.getLogger(NotificationClientAdapter.class);
    private static final String CIRCUIT_BREAKER_NAME = "chatNotificationService";

    private final String chatNotificationUrl;
    private final String serviceToken;
    private final HttpClient httpClient;

    public NotificationClientAdapter(
            @Value("${CHAT_NOTIFICATION_SERVICE_URL:http://localhost:8084}") String chatNotificationUrl,
            @Value("${INTERNAL_SERVICE_TOKEN:}") String serviceToken) {
        this.chatNotificationUrl = chatNotificationUrl;
        this.serviceToken = serviceToken;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "notifyBestEffortFallback")
    public void notifyBestEffort(String type, String recipientId, String message) {
        if (serviceToken == null || serviceToken.isBlank()) {
            log.warn("internal service token not configured, skipping notification");
            return;
        }
        String body = "{\"recipientId\":\"" + recipientId + "\",\"type\":\"" + type + "\",\"title\":\"Notification\",\"message\":\"" + message.replace("\"", "\\\"") + "\"}";
        HttpRequest request = HttpRequest.newBuilder(URI.create(chatNotificationUrl + "/api/v1/chat/internal/notifications"))
                .header("Content-Type", "application/json")
                .header("X-Service-Token", serviceToken)
                .timeout(Duration.ofSeconds(3))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RestClientException("notification call failed with status " + response.statusCode());
            }
        } catch (IOException e) {
            throw new RestClientException("notification call failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RestClientException("notification call interrupted", e);
        }
    }

    void notifyBestEffortFallback(String type, String recipientId, String message, Throwable throwable) {
        log.warn(
                "circuit breaker fallback triggered target=chat-notification-service operation=create internal notification recipientId={} type={} cause={}",
                recipientId,
                type,
                safeCause(throwable));
    }

    private static String safeCause(Throwable throwable) {
        if (throwable == null) {
            return "unknown";
        }
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }
        return throwable.getClass().getSimpleName() + ": " + message.replaceAll("\\s+", " ").trim();
    }
}
