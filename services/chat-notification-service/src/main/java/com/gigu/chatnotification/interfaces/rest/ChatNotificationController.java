package com.gigu.chatnotification.interfaces.rest;

import com.gigu.chatnotification.application.dto.*;
import com.gigu.chatnotification.application.port.in.ChatNotificationUseCase;
import com.gigu.chatnotification.application.service.ParticipantDisplayNameResolver;
import com.gigu.chatnotification.infrastructure.security.AuthUser;
import com.gigu.chatnotification.interfaces.rest.dto.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatNotificationController {
    private final ChatNotificationUseCase service;
    private final ParticipantDisplayNameResolver participantDisplayNameResolver;
    private final String serviceToken;
    public ChatNotificationController(ChatNotificationUseCase service, ParticipantDisplayNameResolver participantDisplayNameResolver, @Value("${SERVICE_TOKEN:internal-token}") String serviceToken){this.service=service;this.participantDisplayNameResolver=participantDisplayNameResolver;this.serviceToken=serviceToken;}

    @PostMapping("/conversations") @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<Map<String,Object>> createConversation(Authentication auth, @Valid @RequestBody CreateConversationBody body){ AuthUser u=(AuthUser)auth.getPrincipal(); var c=service.createOrOpenConversation(new CreateConversationCommand(body.participantId(),body.projectId(),u.id())); Map<String,Object> response=new LinkedHashMap<>(); response.put("id",c.id()); response.put("participants",List.of(c.participantA(),c.participantB())); response.put("projectId",c.projectId()); response.put("createdAt",DateTimeFormatter.ISO_INSTANT.format(c.createdAt())); return ResponseEntity.status(HttpStatus.CREATED).body(response); }

    @GetMapping("/conversations") @SecurityRequirement(name="bearerAuth")
    public List<Map<String,Object>> conversations(Authentication auth){ AuthUser u=(AuthUser)auth.getPrincipal(); List<Map<String,Object>> out=new ArrayList<>(); for(var c:service.listConversations(u.id())){ UUID other=c.participantA().equals(u.id())?c.participantB():c.participantA(); var participant=participantDisplayNameResolver.resolve(other); Map<String,Object> response=new LinkedHashMap<>(); response.put("id",c.id()); response.put("lastMessage",""); response.put("lastMessageAt",DateTimeFormatter.ISO_INSTANT.format(c.createdAt())); response.put("unreadCount",0); response.put("projectId",c.projectId()); response.put("participants",List.of(participantResponse(participant))); out.add(response); } return out; }

    @GetMapping("/conversations/{id}") @SecurityRequirement(name="bearerAuth")
    public Map<String,Object> conversationDetail(Authentication auth,@PathVariable UUID id){ AuthUser u=(AuthUser)auth.getPrincipal(); var c=service.getConversation(id,u.id()); UUID other=c.participantA().equals(u.id())?c.participantB():c.participantA(); var participant=participantDisplayNameResolver.resolve(other); Map<String,Object> response=new LinkedHashMap<>(); response.put("id",c.id()); response.put("projectId",c.projectId()); response.put("participants",List.of(participantResponse(participant))); response.put("createdAt",DateTimeFormatter.ISO_INSTANT.format(c.createdAt())); return response; }

    @GetMapping("/conversations/{id}/messages") @SecurityRequirement(name="bearerAuth")
    public Map<String,Object> messages(Authentication auth,@PathVariable UUID id,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="30") int pageSize){ AuthUser u=(AuthUser)auth.getPrincipal(); var m=service.listMessages(id,u.id(),page,pageSize); var data=m.data().stream().map(x->Map.of("id",x.id(),"conversationId",x.conversationId(),"senderId",x.senderId(),"content",x.content(),"sentAt",DateTimeFormatter.ISO_INSTANT.format(x.sentAt()))).toList(); return Map.of("data",data,"page",m.page(),"pageSize",m.pageSize(),"total",m.total()); }

    @PostMapping("/conversations/{id}/messages") @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<Map<String,Object>> send(Authentication auth,@PathVariable UUID id,@Valid @RequestBody SendMessageBody body){ AuthUser u=(AuthUser)auth.getPrincipal(); var m=service.sendMessage(id,new SendMessageCommand(body.content(),u.id())); return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id",m.id(),"conversationId",m.conversationId(),"senderId",m.senderId(),"content",m.content(),"sentAt",DateTimeFormatter.ISO_INSTANT.format(m.sentAt()))); }

    @GetMapping("/notifications") @SecurityRequirement(name="bearerAuth")
    public Map<String,Object> notifications(Authentication auth,@RequestParam(required=false) Boolean unreadOnly,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int pageSize){ AuthUser u=(AuthUser)auth.getPrincipal(); var n=service.listNotifications(u.id(),unreadOnly,page,pageSize); var data=n.data().stream().map(this::notificationResponse).toList(); return Map.of("data",data,"page",n.page(),"pageSize",n.pageSize(),"total",n.total()); }

    @PatchMapping("/notifications/{id}/read") @SecurityRequirement(name="bearerAuth")
    public Map<String,Object> read(Authentication auth,@PathVariable UUID id){ AuthUser u=(AuthUser)auth.getPrincipal(); var n=service.markRead(id,u.id()); return Map.of("id",n.id(),"read",n.read(),"readAt",DateTimeFormatter.ISO_INSTANT.format(n.readAt())); }

    @PatchMapping("/notifications/read-all") @SecurityRequirement(name="bearerAuth")
    public Map<String,Object> readAll(Authentication auth){ AuthUser u=(AuthUser)auth.getPrincipal(); int count=service.markAllRead(u.id()); return Map.of("updated",true,"readCount",count); }

    @PostMapping("/reports") @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<Map<String,Object>> report(Authentication auth,@Valid @RequestBody ReportBody body){ AuthUser u=(AuthUser)auth.getPrincipal(); var r=service.createReport(new CreateReportCommand(body.reportedUserId(),body.reason(),body.description(),u.id())); return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id",r.id(),"reportedUserId",r.reportedUserId(),"status",r.status(),"createdAt",DateTimeFormatter.ISO_INSTANT.format(r.createdAt()))); }

    @PostMapping("/support-tickets") @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<Map<String,Object>> ticket(Authentication auth,@Valid @RequestBody TicketBody body){ AuthUser u=(AuthUser)auth.getPrincipal(); var t=service.createSupportTicket(new CreateTicketCommand(body.subject(),body.description(),u.id())); return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id",t.id(),"status",t.status(),"createdAt",DateTimeFormatter.ISO_INSTANT.format(t.createdAt()))); }

    @PostMapping("/internal/notifications")
    public ResponseEntity<Map<String,Object>> internalNotify(@RequestHeader(name="X-Service-Token",required=false) String token,@Valid @RequestBody InternalNotificationBody body){ if(token==null || !token.equals(serviceToken)) throw new SecurityException("forbidden"); var n=service.createInternalNotification(new CreateInternalNotificationCommand(body.recipientId(),body.type(),body.title(),body.message(),body.resourceType(),body.resourceId())); return ResponseEntity.status(HttpStatus.CREATED).body(notificationResponse(n)); }

    private Map<String,Object> notificationResponse(com.gigu.chatnotification.domain.model.Notification n) {
        Map<String,Object> response = new LinkedHashMap<>();
        response.put("id", n.id());
        response.put("type", n.type());
        response.put("title", n.title());
        response.put("message", n.message());
        response.put("read", n.read());
        response.put("createdAt", DateTimeFormatter.ISO_INSTANT.format(n.createdAt()));
        if (n.resourceType() != null) {
            response.put("resourceType", n.resourceType());
        }
        if (n.resourceId() != null) {
            response.put("resourceId", n.resourceId());
        }
        return response;
    }

    private Map<String, Object> participantResponse(ParticipantDisplayNameResolver.ParticipantView participant) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", participant.id());
        response.put("displayName", participant.displayName());
        if (participant.role() != null && !participant.role().isBlank()) {
            response.put("role", participant.role());
        }
        if (participant.avatarUrl() != null && !participant.avatarUrl().isBlank()) {
            response.put("avatarUrl", participant.avatarUrl());
        }
        return response;
    }
}
