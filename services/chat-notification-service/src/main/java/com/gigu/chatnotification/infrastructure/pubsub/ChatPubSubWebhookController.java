package com.gigu.chatnotification.infrastructure.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.gigu.chatnotification.application.event.ChatMessageCreatedEvent;
import com.gigu.chatnotification.infrastructure.eda.ExternalEdaConfig;
import java.util.Base64;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/pubsub")
public class ChatPubSubWebhookController {
    private static final Logger log = LoggerFactory.getLogger(ChatPubSubWebhookController.class);
    private static final Pattern RELAXED_JSON_KEY_PATTERN = Pattern.compile("(?<=\\{|,)\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*:");

    private final String pushToken;
    private final ObjectMapper objectMapper;
    private final ChatPubSubProcessingService processingService;

    public ChatPubSubWebhookController(
        ExternalEdaConfig edaConfig,
        Environment environment,
        ObjectMapper objectMapper,
        ChatPubSubProcessingService processingService
    ) {
        String tokenEnvName = edaConfig.pubsubPushTokenEnvName();
        this.pushToken = environment.getProperty(tokenEnvName, "");
        this.objectMapper = objectMapper;
        this.processingService = processingService;
    }

    @PostMapping("/chat-events")
    public ResponseEntity<Void> receive(
        @RequestParam(value = "token", required = false) String queryToken,
        @RequestHeader(value = "X-PubSub-Token", required = false) String headerToken,
        @org.springframework.web.bind.annotation.RequestBody(required = false) JsonNode request
    ) {
        String providedToken = queryToken != null && !queryToken.isBlank() ? queryToken : headerToken;
        if (pushToken.isBlank() || providedToken == null || !pushToken.equals(providedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request == null || request.path("message").isMissingNode() || request.path("message").path("data").isMissingNode() || request.path("message").path("messageId").isMissingNode()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String data = request.path("message").path("data").asText();
            String messageId = request.path("message").path("messageId").asText();
            byte[] decoded = decodeBase64Payload(data);
            ChatMessageCreatedEvent event = parseEvent(decoded);
            processingService.broadcastIfNew(messageId, event);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            String payloadPreview = preview(request.path("message").path("data").asText(null));
            log.error(
                "Failed to process Pub/Sub chat event messageId={} subscription={} payloadPreview={}",
                request.path("message").path("messageId").asText(null),
                request.path("subscription").asText(null),
                payloadPreview,
                e
            );
            throw new IllegalArgumentException("Invalid Pub/Sub chat event payload");
        }
    }

    private static byte[] decodeBase64Payload(String data) {
        try {
            return Base64.getDecoder().decode(data);
        } catch (IllegalArgumentException standardDecoderFailure) {
            return Base64.getUrlDecoder().decode(data);
        }
    }

    private ChatMessageCreatedEvent parseEvent(byte[] decoded) throws IOException {
        try {
            return objectMapper.readValue(decoded, ChatMessageCreatedEvent.class);
        } catch (IOException parseException) {
            String raw = new String(decoded, StandardCharsets.UTF_8);
            String relaxed = normalizeRelaxedJson(raw);
            if (!relaxed.equals(raw)) {
                return objectMapper.readValue(relaxed, ChatMessageCreatedEvent.class);
            }
            throw parseException;
        }
    }

    private static String normalizeRelaxedJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        return RELAXED_JSON_KEY_PATTERN.matcher(raw).replaceAll("\"$1\":");
    }

    private static String preview(String data) {
        if (data == null) {
            return "";
        }
        return data.length() <= 40 ? data : data.substring(0, 40) + "...";
    }
}
