package com.gigu.chatnotification.infrastructure.config;

import com.gigu.chatnotification.application.port.out.ChatEventPublisherPort;
import com.gigu.chatnotification.infrastructure.eda.ExternalEdaConfig;
import com.gigu.chatnotification.infrastructure.eda.ExternalEdaConfigLoader;
import com.gigu.chatnotification.infrastructure.pubsub.GoogleCloudPubSubChatEventPublisher;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.pubsub.v1.TopicName;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PubSubConfig {
    @Bean
    Storage storage() {
        return StorageOptions.getDefaultInstance().getService();
    }

    @Bean
    ExternalEdaConfig externalEdaConfig(ExternalEdaConfigLoader loader) {
        return loader.load();
    }

    @Bean
    ChatEventPublisherPort chatEventPublisher(
        ExternalEdaConfig edaConfig,
        Environment environment,
        ObjectMapper objectMapper
    ) {
        if (!edaConfig.hasPublisherSettings()) {
            return event -> {};
        }
        try {
            TopicName topicName = TopicName.of(edaConfig.projectId().trim(), edaConfig.topicName().trim());
            Publisher publisher = Publisher.newBuilder(topicName).build();
            return new GoogleCloudPubSubChatEventPublisher(publisher, objectMapper);
        } catch (Exception e) {
            if (hasText(environment.getProperty("EDA_CONFIG_BUCKET")) && hasText(environment.getProperty("EDA_CONFIG_OBJECT"))) {
                throw new IllegalStateException("Failed to initialize Pub/Sub publisher from external EDA config", e);
            }
            return event -> {};
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
