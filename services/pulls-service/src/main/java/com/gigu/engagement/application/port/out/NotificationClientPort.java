package com.gigu.engagement.application.port.out;

import java.util.UUID;

public interface NotificationClientPort {
    default void notifyBestEffort(String type, String recipientId, String message) {
        notifyBestEffort(type, recipientId, message, null, null);
    }

    void notifyBestEffort(String type, String recipientId, String message, String resourceType, UUID resourceId);
}
