package com.gigu.chatnotification.application.port.out;

import com.gigu.chatnotification.application.event.ChatMessageCreatedEvent;

public interface ChatEventPublisherPort {
    void publish(ChatMessageCreatedEvent event);
}
