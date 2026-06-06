package com.gigu.chatnotification.infrastructure.pubsub;

import com.gigu.chatnotification.application.event.ChatMessageCreatedEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatRealtimeBroadcaster {
    private final SimpMessagingTemplate messagingTemplate;

    public ChatRealtimeBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcast(ChatMessageCreatedEvent event) {
        messagingTemplate.convertAndSend("/topic/conversations/" + event.conversationId(), event);
    }
}
