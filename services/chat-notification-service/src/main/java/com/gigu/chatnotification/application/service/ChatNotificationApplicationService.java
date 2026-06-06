package com.gigu.chatnotification.application.service;

import com.gigu.chatnotification.application.dto.*;
import com.gigu.chatnotification.application.event.ChatMessageCreatedEvent;
import com.gigu.chatnotification.application.port.in.ChatNotificationUseCase;
import com.gigu.chatnotification.application.port.out.ChatEventPublisherPort;
import com.gigu.chatnotification.application.port.out.ChatNotificationRepositoryPort;
import com.gigu.chatnotification.domain.model.*;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Transactional
public class ChatNotificationApplicationService implements ChatNotificationUseCase {
    private static final Logger log = LoggerFactory.getLogger(ChatNotificationApplicationService.class);

    private final ChatNotificationRepositoryPort repo;
    private final ChatEventPublisherPort chatEventPublisher;

    public ChatNotificationApplicationService(ChatNotificationRepositoryPort repo, ChatEventPublisherPort chatEventPublisher){ this.repo=repo; this.chatEventPublisher=chatEventPublisher; }

    public Conversation createOrOpenConversation(CreateConversationCommand c){
        if(c.participantId()==null) throw new IllegalArgumentException("participantId required");
        if(c.actorId().equals(c.participantId())) throw new IllegalArgumentException("participantId cannot be self");
        UUID a = c.actorId().compareTo(c.participantId()) < 0 ? c.actorId() : c.participantId();
        UUID b = c.actorId().compareTo(c.participantId()) < 0 ? c.participantId() : c.actorId();
        return repo.findConversation(a,b,c.projectId()).orElseGet(() -> repo.saveConversation(new Conversation(UUID.randomUUID(),a,b,c.projectId(),Instant.now())));
    }
    @Transactional(readOnly = true) public List<Conversation> listConversations(UUID userId){ return repo.listConversations(userId); }
    @Transactional(readOnly = true) public Conversation getConversation(UUID id, UUID userId){ var c=repo.getConversation(id).orElseThrow(() -> new IllegalArgumentException("conversation not found")); if(!repo.isParticipant(id,userId)) throw new SecurityException("forbidden"); return c; }
    @Transactional(readOnly = true) public PagedMessages listMessages(UUID cid, UUID uid, int page, int pageSize){ if(!repo.isParticipant(cid,uid)) throw new SecurityException("forbidden"); var p=repo.listMessages(cid,page,pageSize); return new PagedMessages(p.data(),page,pageSize,p.total()); }
    public Message sendMessage(UUID cid, SendMessageCommand c){
        if(!repo.isParticipant(cid,c.actorId())) throw new SecurityException("forbidden");
        if(c.content()==null || c.content().isBlank()) throw new IllegalArgumentException("content required");
        if(c.content().length()>2000) throw new IllegalArgumentException("content too long");

        Instant occurredAt = Instant.now();
        Message message = repo.saveMessage(new Message(UUID.randomUUID(),cid,c.actorId(),c.content(),occurredAt));
        var conv=repo.getConversation(cid).orElseThrow();
        UUID other=conv.participantA().equals(c.actorId())?conv.participantB():conv.participantA();
        repo.saveNotification(new Notification(UUID.randomUUID(),other,"NEW_MESSAGE","Nuevo mensaje",c.content(),"CONVERSATION",cid,false,occurredAt,null));

        ChatMessageCreatedEvent event = new ChatMessageCreatedEvent(
            "ChatMessageCreated",
            message.id(),
            message.conversationId(),
            message.senderId(),
            other,
            message.content(),
            preview(message.content()),
            message.sentAt(),
            buildMetadata(conv.projectId())
        );
        publishAfterCommit(event);
        return message;
    }
    @Transactional(readOnly = true) public PagedNotifications listNotifications(UUID userId, Boolean unreadOnly, int page, int pageSize){ var p=repo.listNotifications(userId,unreadOnly,page,pageSize); return new PagedNotifications(p.data(),page,pageSize,p.total()); }
    public Notification markRead(UUID nid, UUID uid){ var n=repo.getNotification(nid).orElseThrow(() -> new IllegalArgumentException("notification not found")); if(!n.recipientId().equals(uid)) throw new SecurityException("forbidden"); return repo.updateNotification(new Notification(n.id(),n.recipientId(),n.type(),n.title(),n.message(),n.resourceType(),n.resourceId(),true,n.createdAt(),Instant.now())); }
    public int markAllRead(UUID uid){ return repo.markAllRead(uid); }
    public UserReport createReport(CreateReportCommand c){ if(c.reporterId().equals(c.reportedUserId())) throw new IllegalArgumentException("self report not allowed"); return repo.saveReport(new UserReport(UUID.randomUUID(),c.reporterId(),c.reportedUserId(),c.reason(),c.description(),"OPEN",Instant.now())); }
    public SupportTicket createSupportTicket(CreateTicketCommand c){ if(c.subject()==null || c.subject().isBlank() || c.description()==null || c.description().isBlank()) throw new IllegalArgumentException("subject and description required"); return repo.saveTicket(new SupportTicket(UUID.randomUUID(),c.userId(),c.subject(),c.description(),"OPEN",Instant.now())); }
    public Notification createInternalNotification(CreateInternalNotificationCommand c){ return repo.saveNotification(new Notification(UUID.randomUUID(),c.recipientId(),c.type(),c.title(),c.message(),c.resourceType(),c.resourceId(),false,Instant.now(),null)); }

    private void publishAfterCommit(ChatMessageCreatedEvent event) {
        Runnable publish = () -> {
            try {
                chatEventPublisher.publish(event);
            } catch (Exception e) {
                log.error("Failed to publish chat message created event", e);
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish.run();
                }
            });
            return;
        }

        publish.run();
    }

    private static String preview(String content) {
        if (content == null) {
            return "";
        }
        return content.length() <= 160 ? content : content.substring(0, 157) + "...";
    }

    private static Map<String, String> buildMetadata(UUID projectId) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("resourceType", "CONVERSATION");
        if (projectId != null) {
            metadata.put("projectId", projectId.toString());
        }
        return metadata;
    }
}
