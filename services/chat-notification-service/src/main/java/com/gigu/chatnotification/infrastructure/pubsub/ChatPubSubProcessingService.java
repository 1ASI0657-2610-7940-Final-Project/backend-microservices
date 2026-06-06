package com.gigu.chatnotification.infrastructure.pubsub;

import com.gigu.chatnotification.application.event.ChatMessageCreatedEvent;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ChatPubSubProcessingService {
    private final Set<String> processedMessageIds = ConcurrentHashMap.newKeySet();
    private final ChatRealtimeBroadcaster broadcaster;

    public ChatPubSubProcessingService(ChatRealtimeBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    public boolean hasSeen(String messageId) {
        return processedMessageIds.contains(messageId);
    }

    public void broadcastIfNew(String messageId, ChatMessageCreatedEvent event) {
        if (!processedMessageIds.add(messageId)) {
            return;
        }

        broadcaster.broadcast(event);
    }
}
