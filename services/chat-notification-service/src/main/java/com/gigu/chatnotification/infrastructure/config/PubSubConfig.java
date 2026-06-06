package com.gigu.chatnotification.infrastructure.config;

import com.gigu.chatnotification.application.port.out.ChatEventPublisherPort;
import com.gigu.chatnotification.infrastructure.pubsub.GoogleCloudPubSubChatEventPublisher;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.TopicName;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PubSubConfig {
    @Bean
    ChatEventPublisherPort chatEventPublisher(
        @Value("${GCP_PROJECT_ID:}") String projectId,
        @Value("${PUBSUB_CHAT_TOPIC:}") String topic,
        ObjectMapper objectMapper
    ) {
        if (projectId == null || projectId.isBlank() || topic == null || topic.isBlank()) {
            return event -> {};
        }
        try {
            TopicName topicName = TopicName.of(projectId.trim(), topic.trim());
            Publisher publisher = Publisher.newBuilder(topicName).build();
            return new GoogleCloudPubSubChatEventPublisher(publisher, objectMapper);
        } catch (Exception e) {
            return event -> {};
        }
    }
}
