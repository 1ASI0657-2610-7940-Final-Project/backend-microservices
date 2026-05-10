package com.gigu.chatnotification.application.port.out;
import com.gigu.chatnotification.domain.model.*;
import java.util.*;
public interface ChatNotificationRepositoryPort {
    Optional<Conversation> findConversation(UUID participantA, UUID participantB, UUID projectId);
    Conversation saveConversation(Conversation conversation);
    List<Conversation> listConversations(UUID userId);
    Optional<Conversation> getConversation(UUID id);
    boolean isParticipant(UUID conversationId, UUID userId);
    Message saveMessage(Message message);
    record MessagePage(List<Message> data, long total) {}
    MessagePage listMessages(UUID conversationId, int page, int pageSize);
    Notification saveNotification(Notification notification);
    record NotificationPage(List<Notification> data, long total) {}
    NotificationPage listNotifications(UUID userId, Boolean unreadOnly, int page, int pageSize);
    Optional<Notification> getNotification(UUID id);
    Notification updateNotification(Notification notification);
    int markAllRead(UUID userId);
    UserReport saveReport(UserReport report);
    SupportTicket saveTicket(SupportTicket ticket);
}
