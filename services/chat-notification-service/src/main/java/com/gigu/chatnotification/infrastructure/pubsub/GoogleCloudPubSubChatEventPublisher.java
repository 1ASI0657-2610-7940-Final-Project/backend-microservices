package com.gigu.chatnotification.infrastructure.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigu.chatnotification.application.event.ChatMessageCreatedEvent;
import com.gigu.chatnotification.application.port.out.ChatEventPublisherPort;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.PubsubMessage;
import com.google.protobuf.ByteString;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GoogleCloudPubSubChatEventPublisher implements ChatEventPublisherPort {
    private static final Logger log = LoggerFactory.getLogger(GoogleCloudPubSubChatEventPublisher.class);

    private final Publisher publisher;
    private final ObjectMapper objectMapper;

    public GoogleCloudPubSubChatEventPublisher(Publisher publisher, ObjectMapper objectMapper) {
        this.publisher = publisher;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(ChatMessageCreatedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            PubsubMessage message = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(json))
                .putAttributes("eventType", event.eventType())
                .putAttributes("conversationId", event.conversationId().toString())
                .putAttributes("messageId", event.messageId().toString())
                .build();
            publisher.publish(message);
        } catch (Exception e) {
            log.error("Failed to publish chat event to Pub/Sub", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            publisher.shutdown();
            publisher.awaitTermination(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Failed to shutdown Pub/Sub publisher cleanly", e);
        }
    }
}
