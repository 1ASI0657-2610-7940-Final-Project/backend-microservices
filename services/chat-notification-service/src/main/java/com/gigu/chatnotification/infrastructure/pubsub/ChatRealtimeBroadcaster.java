package com.gigu.chatnotification.infrastructure.pubsub;

import com.gigu.chatnotification.application.event.ChatMessageCreatedEvent;
import com.gigu.chatnotification.infrastructure.eda.ExternalEdaConfig;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatRealtimeBroadcaster {
    private final SimpMessagingTemplate messagingTemplate;
    private final ExternalEdaConfig edaConfig;

    public ChatRealtimeBroadcaster(SimpMessagingTemplate messagingTemplate, ExternalEdaConfig edaConfig) {
        this.messagingTemplate = messagingTemplate;
        this.edaConfig = edaConfig;
    }

    public void broadcast(ChatMessageCreatedEvent event) {
        messagingTemplate.convertAndSend(edaConfig.chatMessageTopic().replace("{conversationId}", event.conversationId().toString()), event);
    }
}
