package com.gigu.chatnotification.application.service;

import com.gigu.chatnotification.application.dto.*;
import com.gigu.chatnotification.application.port.in.ChatNotificationUseCase;
import com.gigu.chatnotification.application.port.out.ChatNotificationRepositoryPort;
import com.gigu.chatnotification.domain.model.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChatNotificationApplicationService implements ChatNotificationUseCase {
    private final ChatNotificationRepositoryPort repo;
    public ChatNotificationApplicationService(ChatNotificationRepositoryPort repo){ this.repo=repo; }

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
    public Message sendMessage(UUID cid, SendMessageCommand c){ if(!repo.isParticipant(cid,c.actorId())) throw new SecurityException("forbidden"); if(c.content()==null || c.content().isBlank()) throw new IllegalArgumentException("content required"); if(c.content().length()>2000) throw new IllegalArgumentException("content too long"); var m=repo.saveMessage(new Message(UUID.randomUUID(),cid,c.actorId(),c.content(),Instant.now())); var conv=repo.getConversation(cid).orElseThrow(); UUID other=conv.participantA().equals(c.actorId())?conv.participantB():conv.participantA(); repo.saveNotification(new Notification(UUID.randomUUID(),other,"NEW_MESSAGE","Nuevo mensaje",c.content(),"CONVERSATION",cid,false,Instant.now(),null)); return m; }
    @Transactional(readOnly = true) public PagedNotifications listNotifications(UUID userId, Boolean unreadOnly, int page, int pageSize){ var p=repo.listNotifications(userId,unreadOnly,page,pageSize); return new PagedNotifications(p.data(),page,pageSize,p.total()); }
    public Notification markRead(UUID nid, UUID uid){ var n=repo.getNotification(nid).orElseThrow(() -> new IllegalArgumentException("notification not found")); if(!n.recipientId().equals(uid)) throw new SecurityException("forbidden"); return repo.updateNotification(new Notification(n.id(),n.recipientId(),n.type(),n.title(),n.message(),n.resourceType(),n.resourceId(),true,n.createdAt(),Instant.now())); }
    public int markAllRead(UUID uid){ return repo.markAllRead(uid); }
    public UserReport createReport(CreateReportCommand c){ if(c.reporterId().equals(c.reportedUserId())) throw new IllegalArgumentException("self report not allowed"); return repo.saveReport(new UserReport(UUID.randomUUID(),c.reporterId(),c.reportedUserId(),c.reason(),c.description(),"OPEN",Instant.now())); }
    public SupportTicket createSupportTicket(CreateTicketCommand c){ if(c.subject()==null || c.subject().isBlank() || c.description()==null || c.description().isBlank()) throw new IllegalArgumentException("subject and description required"); return repo.saveTicket(new SupportTicket(UUID.randomUUID(),c.userId(),c.subject(),c.description(),"OPEN",Instant.now())); }
    public Notification createInternalNotification(CreateInternalNotificationCommand c){ return repo.saveNotification(new Notification(UUID.randomUUID(),c.recipientId(),c.type(),c.title(),c.message(),c.resourceType(),c.resourceId(),false,Instant.now(),null)); }
}
