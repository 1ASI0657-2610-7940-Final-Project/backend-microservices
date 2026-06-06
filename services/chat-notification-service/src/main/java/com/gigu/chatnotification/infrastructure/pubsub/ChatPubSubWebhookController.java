package com.gigu.chatnotification.infrastructure.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigu.chatnotification.application.event.ChatMessageCreatedEvent;
import com.gigu.chatnotification.infrastructure.pubsub.dto.PubSubPushRequest;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/pubsub")
public class ChatPubSubWebhookController {
    private final String pushToken;
    private final ObjectMapper objectMapper;
    private final ChatPubSubProcessingService processingService;

    public ChatPubSubWebhookController(
        @Value("${PUBSUB_PUSH_TOKEN:}") String pushToken,
        ObjectMapper objectMapper,
        ChatPubSubProcessingService processingService
    ) {
        this.pushToken = pushToken == null ? "" : pushToken;
        this.objectMapper = objectMapper;
        this.processingService = processingService;
    }

    @PostMapping("/chat-events")
    public ResponseEntity<Void> receive(
        @RequestParam(value = "token", required = false) String queryToken,
        @RequestHeader(value = "X-PubSub-Token", required = false) String headerToken,
        @org.springframework.web.bind.annotation.RequestBody(required = false) PubSubPushRequest request
    ) throws Exception {
        String providedToken = queryToken != null && !queryToken.isBlank() ? queryToken : headerToken;
        if (pushToken.isBlank() || providedToken == null || !pushToken.equals(providedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request == null || request.message() == null || request.message().data() == null || request.message().messageId() == null) {
            return ResponseEntity.badRequest().build();
        }

        byte[] decoded = Base64.getDecoder().decode(request.message().data());
        ChatMessageCreatedEvent event = objectMapper.readValue(decoded, ChatMessageCreatedEvent.class);
        processingService.broadcastIfNew(request.message().messageId(), event);
        return ResponseEntity.noContent().build();
    }
}
