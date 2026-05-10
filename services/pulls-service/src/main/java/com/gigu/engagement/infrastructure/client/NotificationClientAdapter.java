package com.gigu.engagement.infrastructure.client;
import com.gigu.engagement.application.port.out.NotificationClientPort;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
@Component
public class NotificationClientAdapter implements NotificationClientPort {
    private static final Logger log = LoggerFactory.getLogger(NotificationClientAdapter.class);
    private final String chatNotificationUrl;
    private final String serviceToken;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public NotificationClientAdapter(@Value("${CHAT_NOTIFICATION_SERVICE_URL:http://localhost:8084}") String chatNotificationUrl, @Value("${INTERNAL_SERVICE_TOKEN:}") String serviceToken) {
        this.chatNotificationUrl = chatNotificationUrl;
        this.serviceToken = serviceToken;
    }

    public void notifyBestEffort(String type, String recipientId, String message){
        if (serviceToken == null || serviceToken.isBlank()) {
            log.warn("internal service token not configured, skipping notification");
            return;
        }
        String body = "{\"recipientId\":\"" + recipientId + "\",\"type\":\"" + type + "\",\"title\":\"Notification\",\"message\":\"" + message.replace("\"","\\\"") + "\"}";
        HttpRequest request = HttpRequest.newBuilder(URI.create(chatNotificationUrl + "/api/v1/chat/internal/notifications"))
                .header("Content-Type", "application/json")
                .header("X-Service-Token", serviceToken)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("notification call failed with status {}", response.statusCode());
            }
        } catch (Exception e) {
            log.warn("notification call failed", e);
        }
    }
}
