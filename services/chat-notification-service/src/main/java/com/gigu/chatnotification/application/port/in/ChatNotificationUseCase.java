package com.gigu.chatnotification.application.port.in;
import com.gigu.chatnotification.application.dto.*;
import com.gigu.chatnotification.domain.model.*;
import java.util.*;
public interface ChatNotificationUseCase {
    Conversation createOrOpenConversation(CreateConversationCommand command);
    List<Conversation> listConversations(UUID userId);
    Conversation getConversation(UUID conversationId, UUID userId);
    record PagedMessages(List<Message> data, int page, int pageSize, long total) {}
    PagedMessages listMessages(UUID conversationId, UUID userId, int page, int pageSize);
    Message sendMessage(UUID conversationId, SendMessageCommand command);
    record PagedNotifications(List<Notification> data, int page, int pageSize, long total) {}
    PagedNotifications listNotifications(UUID userId, Boolean unreadOnly, int page, int pageSize);
    Notification markRead(UUID notificationId, UUID userId);
    int markAllRead(UUID userId);
    UserReport createReport(CreateReportCommand command);
    SupportTicket createSupportTicket(CreateTicketCommand command);
    Notification createInternalNotification(CreateInternalNotificationCommand command);
}
