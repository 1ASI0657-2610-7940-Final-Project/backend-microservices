package com.gigu.chatnotification.infrastructure.eda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExternalEdaConfigLoaderTest {

    @Test
    void loadsValidExternalConfigFromCloudStorage() throws Exception {
        Storage storage = mock(Storage.class);
        Blob blob = mock(Blob.class);
        ObjectMapper objectMapper = new ObjectMapper();
        MockEnvironment environment = new MockEnvironment()
            .withProperty("EDA_CONFIG_BUCKET", "gigu-external-config")
            .withProperty("EDA_CONFIG_OBJECT", "prod/eda-pubsub-config.json");

        String json = """
            {
              "provider": "google-pubsub",
              "projectId": "dosys-rest-api",
              "topicName": "gigu-chat-events",
              "pushSubscriptionName": "gigu-chat-events-push-chat",
              "pushWebhookPath": "/internal/pubsub/chat-events",
              "chatMessageTopic": "/topic/conversations/{conversationId}",
              "notificationTopic": "/topic/notifications/{userId}",
              "webSocketPath": "/ws",
              "credentialsStrategy": "cloud-run-service-account",
              "secrets": {
                "pubsubPushTokenEnv": "PUBSUB_PUSH_TOKEN"
              }
            }
            """;

        when(storage.get(BlobId.of("gigu-external-config", "prod/eda-pubsub-config.json"))).thenReturn(blob);
        when(blob.getContent()).thenReturn(json.getBytes(StandardCharsets.UTF_8));

        ExternalEdaConfigLoader loader = new ExternalEdaConfigLoader(storage, objectMapper, environment);
        ExternalEdaConfig config = loader.load();

        assertEquals("google-pubsub", config.provider());
        assertEquals("dosys-rest-api", config.projectId());
        assertEquals("gigu-chat-events", config.topicName());
        assertEquals("gigu-chat-events-push-chat", config.pushSubscriptionName());
        assertEquals("/internal/pubsub/chat-events", config.pushWebhookPath());
        assertEquals("/topic/conversations/{conversationId}", config.chatMessageTopic());
        assertEquals("/topic/notifications/{userId}", config.notificationTopic());
        assertEquals("/ws", config.webSocketPath());
        assertEquals("cloud-run-service-account", config.credentialsStrategy());
        assertEquals("PUBSUB_PUSH_TOKEN", config.pubsubPushTokenEnvName());

        verify(storage).get(BlobId.of("gigu-external-config", "prod/eda-pubsub-config.json"));
    }

    @Test
    void rejectsMissingRequiredField() throws Exception {
        Storage storage = mock(Storage.class);
        Blob blob = mock(Blob.class);
        ObjectMapper objectMapper = new ObjectMapper();
        MockEnvironment environment = new MockEnvironment()
            .withProperty("EDA_CONFIG_BUCKET", "gigu-external-config")
            .withProperty("EDA_CONFIG_OBJECT", "prod/eda-pubsub-config.json");

        String json = """
            {
              "provider": "google-pubsub",
              "projectId": "dosys-rest-api",
              "pushSubscriptionName": "gigu-chat-events-push-chat",
              "pushWebhookPath": "/internal/pubsub/chat-events",
              "chatMessageTopic": "/topic/conversations/{conversationId}",
              "notificationTopic": "/topic/notifications/{userId}",
              "webSocketPath": "/ws",
              "credentialsStrategy": "cloud-run-service-account",
              "secrets": {
                "pubsubPushTokenEnv": "PUBSUB_PUSH_TOKEN"
              }
            }
            """;

        when(storage.get(any(BlobId.class))).thenReturn(blob);
        when(blob.getContent()).thenReturn(json.getBytes(StandardCharsets.UTF_8));

        ExternalEdaConfigLoader loader = new ExternalEdaConfigLoader(storage, objectMapper, environment);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, loader::load);
        assertTrue(exception.getMessage().contains("topicName"));
    }

    @Test
    void fallsBackToLegacyEnvironmentWhenExternalConfigAbsent() {
        Storage storage = mock(Storage.class);
        ObjectMapper objectMapper = new ObjectMapper();
        MockEnvironment environment = new MockEnvironment()
            .withProperty("GCP_PROJECT_ID", "dosys-rest-api")
            .withProperty("PUBSUB_CHAT_TOPIC", "gigu-chat-events");

        ExternalEdaConfigLoader loader = new ExternalEdaConfigLoader(storage, objectMapper, environment);
        ExternalEdaConfig config = loader.load();

        assertEquals("dosys-rest-api", config.projectId());
        assertEquals("gigu-chat-events", config.topicName());
        assertEquals("/internal/pubsub/chat-events", config.pushWebhookPath());
        assertEquals("/topic/conversations/{conversationId}", config.chatMessageTopic());
        assertEquals("/topic/notifications/{userId}", config.notificationTopic());
        assertEquals("/ws", config.webSocketPath());
        assertEquals("PUBSUB_PUSH_TOKEN", config.pubsubPushTokenEnvName());
        verifyNoInteractions(storage);
    }

    @Test
    void resolvesTokenEnvNameFromSecrets() {
        ExternalEdaConfig config = new ExternalEdaConfig(
            "google-pubsub",
            "dosys-rest-api",
            "gigu-chat-events",
            "gigu-chat-events-push-chat",
            "/internal/pubsub/chat-events",
            "/topic/conversations/{conversationId}",
            "/topic/notifications/{userId}",
            "/ws",
            "cloud-run-service-account",
            new ExternalEdaConfig.Secrets("CUSTOM_PUSH_TOKEN")
        );

        assertEquals("CUSTOM_PUSH_TOKEN", config.pubsubPushTokenEnvName());
    }
}
