package com.gigu.chatnotification.infrastructure.persistence.adapter;

import com.gigu.chatnotification.application.port.out.ChatNotificationRepositoryPort;
import com.gigu.chatnotification.domain.model.*;
import com.gigu.chatnotification.infrastructure.persistence.entity.*;
import com.gigu.chatnotification.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class ChatNotificationRepositoryAdapter implements ChatNotificationRepositoryPort {
    private final ConversationJpaRepository conversations; private final ConversationParticipantJpaRepository participants; private final MessageJpaRepository messages; private final NotificationJpaRepository notifications; private final UserReportJpaRepository reports; private final SupportTicketJpaRepository tickets;
    public ChatNotificationRepositoryAdapter(ConversationJpaRepository conversations, ConversationParticipantJpaRepository participants, MessageJpaRepository messages, NotificationJpaRepository notifications, UserReportJpaRepository reports, SupportTicketJpaRepository tickets){this.conversations=conversations;this.participants=participants;this.messages=messages;this.notifications=notifications;this.reports=reports;this.tickets=tickets;}

    public Optional<Conversation> findConversation(UUID a, UUID b, UUID projectId){ return conversations.findByParticipantAAndParticipantBAndProjectId(a,b,projectId).map(this::toDomain); }
    public Conversation saveConversation(Conversation c){ var e=new ConversationEntity(); e.id=c.id();e.participantA=c.participantA();e.participantB=c.participantB();e.projectId=c.projectId();e.createdAt=c.createdAt(); var saved=conversations.save(e); ConversationParticipantEntity p1=new ConversationParticipantEntity(); p1.conversationId=saved.id; p1.userId=saved.participantA; participants.save(p1); ConversationParticipantEntity p2=new ConversationParticipantEntity(); p2.conversationId=saved.id; p2.userId=saved.participantB; participants.save(p2); return toDomain(saved); }
    public List<Conversation> listConversations(UUID userId){ return conversations.listForUser(userId).stream().map(this::toDomain).toList(); }
    public Optional<Conversation> getConversation(UUID id){ return conversations.findById(id).map(this::toDomain); }
    public boolean isParticipant(UUID conversationId, UUID userId){ return participants.existsByConversationIdAndUserId(conversationId,userId); }

    public Message saveMessage(Message m){ MessageEntity e=new MessageEntity(); e.id=m.id();e.conversationId=m.conversationId();e.senderId=m.senderId();e.content=m.content();e.sentAt=m.sentAt(); var s=messages.save(e); return new Message(s.id,s.conversationId,s.senderId,s.content,s.sentAt); }
    public MessagePage listMessages(UUID cid, int page, int pageSize){ var p=messages.findByConversationIdOrderBySentAtDesc(cid, PageRequest.of(Math.max(0,page-1),pageSize)); var list=p.getContent().stream().map(x->new Message(x.id,x.conversationId,x.senderId,x.content,x.sentAt)).toList(); return new MessagePage(list,p.getTotalElements()); }

    public Notification saveNotification(Notification n){ NotificationEntity e=new NotificationEntity(); e.id=n.id();e.recipientId=n.recipientId();e.type=n.type();e.title=n.title();e.message=n.message();e.resourceType=n.resourceType();e.resourceId=n.resourceId();e.read=n.read();e.createdAt=n.createdAt();e.readAt=n.readAt(); var s=notifications.save(e); return toNotification(s); }
    public NotificationPage listNotifications(UUID userId, Boolean unreadOnly, int page, int pageSize){ var p=notifications.findUserNotifications(userId,unreadOnly,PageRequest.of(Math.max(0,page-1),pageSize)); return new NotificationPage(p.getContent().stream().map(this::toNotification).toList(), p.getTotalElements()); }
    public Optional<Notification> getNotification(UUID id){ return notifications.findById(id).map(this::toNotification); }
    public Notification updateNotification(Notification n){ NotificationEntity e=notifications.findById(n.id()).orElseThrow(); e.read=n.read();e.readAt=n.readAt(); return toNotification(notifications.save(e)); }
    public int markAllRead(UUID userId){ return notifications.markAllRead(userId, Instant.now()); }

    public UserReport saveReport(UserReport r){ UserReportEntity e=new UserReportEntity(); e.id=r.id();e.reporterId=r.reporterId();e.reportedUserId=r.reportedUserId();e.reason=r.reason();e.description=r.description();e.status=r.status();e.createdAt=r.createdAt(); var s=reports.save(e); return new UserReport(s.id,s.reporterId,s.reportedUserId,s.reason,s.description,s.status,s.createdAt); }
    public SupportTicket saveTicket(SupportTicket t){ SupportTicketEntity e=new SupportTicketEntity(); e.id=t.id();e.userId=t.userId();e.subject=t.subject();e.description=t.description();e.status=t.status();e.createdAt=t.createdAt(); var s=tickets.save(e); return new SupportTicket(s.id,s.userId,s.subject,s.description,s.status,s.createdAt); }

    private Conversation toDomain(ConversationEntity e){ return new Conversation(e.id,e.participantA,e.participantB,e.projectId,e.createdAt); }
    private Notification toNotification(NotificationEntity e){ return new Notification(e.id,e.recipientId,e.type,e.title,e.message,e.resourceType,e.resourceId,e.read,e.createdAt,e.readAt); }
}
