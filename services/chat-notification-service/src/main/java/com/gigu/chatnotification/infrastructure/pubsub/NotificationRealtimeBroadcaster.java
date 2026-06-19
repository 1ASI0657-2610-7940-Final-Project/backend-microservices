package com.gigu.chatnotification.infrastructure.pubsub;

import com.gigu.chatnotification.application.event.NotificationCreatedEvent;
import com.gigu.chatnotification.infrastructure.eda.ExternalEdaConfig;
import com.gigu.chatnotification.application.port.out.NotificationBroadcastPort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationRealtimeBroadcaster implements NotificationBroadcastPort {
    private final SimpMessagingTemplate messagingTemplate;
    private final ExternalEdaConfig edaConfig;

    public NotificationRealtimeBroadcaster(SimpMessagingTemplate messagingTemplate, ExternalEdaConfig edaConfig) {
        this.messagingTemplate = messagingTemplate;
        this.edaConfig = edaConfig;
    }

    @Override
    public void broadcast(NotificationCreatedEvent event) {
        messagingTemplate.convertAndSend(edaConfig.notificationTopic().replace("{userId}", event.recipientId().toString()), event);
    }
}
