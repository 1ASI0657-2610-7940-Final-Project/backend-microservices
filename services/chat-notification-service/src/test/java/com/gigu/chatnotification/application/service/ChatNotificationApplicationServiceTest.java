package com.gigu.chatnotification.application.service;

import com.gigu.chatnotification.application.dto.*;
import com.gigu.chatnotification.application.port.out.ChatNotificationRepositoryPort;
import com.gigu.chatnotification.domain.model.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatNotificationApplicationServiceTest {
    @Mock ChatNotificationRepositoryPort repo;
    @InjectMocks ChatNotificationApplicationService service;

    @Test void createConversationCreatesNewConversation(){
        UUID a=UUID.randomUUID(), b=UUID.randomUUID();
        when(repo.findConversation(any(),any(),isNull())).thenReturn(Optional.empty());
        when(repo.saveConversation(any())).thenAnswer(i->i.getArgument(0));
        var c=service.createOrOpenConversation(new CreateConversationCommand(b,null,a));
        assertNotNull(c.id());
    }

    @Test void createConversationReturnsExistingOne(){
        UUID a=UUID.randomUUID(), b=UUID.randomUUID();
        Conversation existing = new Conversation(UUID.randomUUID(), a.compareTo(b)<0?a:b, a.compareTo(b)<0?b:a, null, Instant.now());
        when(repo.findConversation(any(),any(),isNull())).thenReturn(Optional.of(existing));
        var c=service.createOrOpenConversation(new CreateConversationCommand(b,null,a));
        assertEquals(existing.id(), c.id());
        verify(repo, never()).saveConversation(any());
    }
    @Test void createConversationRejectsSelf(){
        UUID a=UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> service.createOrOpenConversation(new CreateConversationCommand(a,null,a)));
    }
    @Test void createConversationRequiresParticipantId(){
        assertThrows(IllegalArgumentException.class, () -> service.createOrOpenConversation(new CreateConversationCommand(null,null,UUID.randomUUID())));
    }

    @Test void sendMessageValidatesParticipant(){
        UUID cid=UUID.randomUUID(), uid=UUID.randomUUID();
        when(repo.isParticipant(cid,uid)).thenReturn(false);
        assertThrows(SecurityException.class, () -> service.sendMessage(cid,new SendMessageCommand("hi",uid)));
    }
    @Test void sendMessageRejectsBlankAndLong(){
        UUID cid=UUID.randomUUID(), uid=UUID.randomUUID();
        when(repo.isParticipant(cid,uid)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.sendMessage(cid,new SendMessageCommand(" ",uid)));
        String longMsg = "x".repeat(2001);
        assertThrows(IllegalArgumentException.class, () -> service.sendMessage(cid,new SendMessageCommand(longMsg,uid)));
    }
    @Test void listMessagesHappyPath(){
        UUID cid=UUID.randomUUID(), uid=UUID.randomUUID();
        when(repo.isParticipant(cid,uid)).thenReturn(true);
        when(repo.listMessages(cid,1,30)).thenReturn(new ChatNotificationRepositoryPort.MessagePage(List.of(),0));
        assertEquals(0, service.listMessages(cid,uid,1,30).total());
    }
    @Test void listAndGetConversationAuthChecks(){
        UUID cid=UUID.randomUUID(), uid=UUID.randomUUID();
        when(repo.getConversation(cid)).thenReturn(Optional.of(new Conversation(cid,uid,UUID.randomUUID(),null,Instant.now())));
        when(repo.isParticipant(cid,uid)).thenReturn(true);
        assertEquals(cid, service.getConversation(cid,uid).id());
        when(repo.isParticipant(cid,uid)).thenReturn(false);
        assertThrows(SecurityException.class, () -> service.getConversation(cid,uid));
    }
    @Test void getConversationNotFound(){
        UUID cid=UUID.randomUUID(), uid=UUID.randomUUID();
        when(repo.getConversation(cid)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getConversation(cid,uid));
    }
    @Test void listConversationsAndNotificationsAndInternalCreate(){
        UUID u=UUID.randomUUID();
        when(repo.listConversations(u)).thenReturn(List.of());
        when(repo.listNotifications(u,null,1,20)).thenReturn(new ChatNotificationRepositoryPort.NotificationPage(List.of(),0));
        when(repo.saveNotification(any())).thenAnswer(i->i.getArgument(0));
        assertEquals(0, service.listConversations(u).size());
        assertEquals(0, service.listNotifications(u,null,1,20).total());
        var created = service.createInternalNotification(new CreateInternalNotificationCommand(u,"T","t","m","R",UUID.randomUUID()));
        assertEquals(u, created.recipientId());
    }

    @Test void listMessagesRejectsNonParticipant(){
        UUID cid=UUID.randomUUID(), uid=UUID.randomUUID();
        when(repo.isParticipant(cid,uid)).thenReturn(false);
        assertThrows(SecurityException.class, () -> service.listMessages(cid,uid,1,30));
    }

    @Test void sendMessageCreatesNotification(){
        UUID cid=UUID.randomUUID(), a=UUID.randomUUID(), b=UUID.randomUUID();
        when(repo.isParticipant(cid,a)).thenReturn(true);
        when(repo.saveMessage(any())).thenAnswer(i->i.getArgument(0));
        when(repo.getConversation(cid)).thenReturn(Optional.of(new Conversation(cid,a,b,null,Instant.now())));
        service.sendMessage(cid,new SendMessageCommand("hello",a));
        verify(repo).saveNotification(any());
    }

    @Test void markNotificationReadValidatesOwner(){
        UUID n=UUID.randomUUID(), owner=UUID.randomUUID(), other=UUID.randomUUID();
        when(repo.getNotification(n)).thenReturn(Optional.of(new Notification(n,owner,"T","t","m",null,null,false,Instant.now(),null)));
        assertThrows(SecurityException.class, () -> service.markRead(n,other));
    }
    @Test void markNotificationReadSuccessAndNotFound(){
        UUID n=UUID.randomUUID(), owner=UUID.randomUUID();
        when(repo.getNotification(n)).thenReturn(Optional.of(new Notification(n,owner,"T","t","m",null,null,false,Instant.now(),null)));
        when(repo.updateNotification(any())).thenAnswer(i->i.getArgument(0));
        assertTrue(service.markRead(n,owner).read());
        when(repo.getNotification(n)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.markRead(n,owner));
    }

    @Test void readAllUpdatesOnlyUser(){
        UUID u=UUID.randomUUID();
        when(repo.markAllRead(u)).thenReturn(4);
        assertEquals(4, service.markAllRead(u));
    }

    @Test void reportUserRejectsSelfReport(){
        UUID u=UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> service.createReport(new CreateReportCommand(u,"INAPPROPRIATE","x",u)));
    }

    @Test void supportTicketRequiresSubjectAndDescription(){
        assertThrows(IllegalArgumentException.class, () -> service.createSupportTicket(new CreateTicketCommand("","",UUID.randomUUID())));
    }
    @Test void supportTicketAndReportSuccess(){
        UUID u=UUID.randomUUID(), other=UUID.randomUUID();
        when(repo.saveTicket(any())).thenAnswer(i->i.getArgument(0));
        when(repo.saveReport(any())).thenAnswer(i->i.getArgument(0));
        assertEquals("OPEN", service.createSupportTicket(new CreateTicketCommand("s","d",u)).status());
        assertEquals("OPEN", service.createReport(new CreateReportCommand(other,"REASON","desc",u)).status());
    }
}
