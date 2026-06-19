package com.gigu.chatnotification.infrastructure.eda;

import org.springframework.core.env.Environment;

public record ExternalEdaConfig(
    String provider,
    String projectId,
    String topicName,
    String pushSubscriptionName,
    String pushWebhookPath,
    String chatMessageTopic,
    String notificationTopic,
    String webSocketPath,
    String credentialsStrategy,
    Secrets secrets
) {
    public ExternalEdaConfig {
        secrets = secrets == null ? new Secrets(null) : secrets;
    }

    public void validateStrict() {
        requireEquals("provider", "google-pubsub", provider);
        requireText("projectId", projectId);
        requireText("topicName", topicName);
        requireText("pushSubscriptionName", pushSubscriptionName);
        requireText("pushWebhookPath", pushWebhookPath);
        requireText("chatMessageTopic", chatMessageTopic);
        requireText("notificationTopic", notificationTopic);
        requireText("webSocketPath", webSocketPath);
        requireEquals("credentialsStrategy", "cloud-run-service-account", credentialsStrategy);
        requireText("secrets.pubsubPushTokenEnv", secrets.pubsubPushTokenEnv());
    }

    public boolean hasPublisherSettings() {
        return hasText(projectId) && hasText(topicName);
    }

    public String pubsubPushTokenEnvName() {
        return hasText(secrets.pubsubPushTokenEnv()) ? secrets.pubsubPushTokenEnv().trim() : "PUBSUB_PUSH_TOKEN";
    }

    public static ExternalEdaConfig fromEnvironment(Environment environment) {
        return new ExternalEdaConfig(
            "google-pubsub",
            trimmed(environment.getProperty("GCP_PROJECT_ID")),
            trimmed(environment.getProperty("PUBSUB_CHAT_TOPIC")),
            "gigu-chat-events-push-chat",
            "/internal/pubsub/chat-events",
            "/topic/conversations/{conversationId}",
            "/topic/notifications/{userId}",
            "/ws",
            "cloud-run-service-account",
            new Secrets("PUBSUB_PUSH_TOKEN")
        );
    }

    private static void requireText(String field, String value) {
        if (!hasText(value)) {
            throw new IllegalArgumentException("EDA config field '" + field + "' must not be blank");
        }
    }

    private static void requireEquals(String field, String expected, String value) {
        if (!expected.equals(value == null ? null : value.trim())) {
            throw new IllegalArgumentException("EDA config field '" + field + "' must be '" + expected + "'");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String trimmed(String value) {
        return value == null ? null : value.trim();
    }

    public record Secrets(String pubsubPushTokenEnv) {}
}
