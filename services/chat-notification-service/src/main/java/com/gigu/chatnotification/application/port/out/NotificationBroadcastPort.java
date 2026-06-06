package com.gigu.chatnotification.application.port.out;

import com.gigu.chatnotification.application.event.NotificationCreatedEvent;

public interface NotificationBroadcastPort {
    void broadcast(NotificationCreatedEvent event);
}
