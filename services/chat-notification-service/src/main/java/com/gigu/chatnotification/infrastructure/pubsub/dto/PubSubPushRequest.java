package com.gigu.chatnotification.infrastructure.pubsub.dto;

import java.util.Map;

public record PubSubPushRequest(String subscription, Message message) {
    public record Message(String data, String messageId, Map<String, String> attributes, String publishTime) {}
}
