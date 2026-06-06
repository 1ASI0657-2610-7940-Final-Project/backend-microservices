package com.gigu.chatnotification.infrastructure.pubsub;

import com.gigu.chatnotification.application.event.NotificationCreatedEvent;
import com.gigu.chatnotification.application.port.out.NotificationBroadcastPort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationRealtimeBroadcaster implements NotificationBroadcastPort {
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationRealtimeBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void broadcast(NotificationCreatedEvent event) {
        messagingTemplate.convertAndSend("/topic/notifications/" + event.recipientId(), event);
    }
}
