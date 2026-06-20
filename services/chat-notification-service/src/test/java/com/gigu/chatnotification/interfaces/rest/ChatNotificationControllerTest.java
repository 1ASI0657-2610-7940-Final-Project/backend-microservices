package com.gigu.chatnotification.interfaces.rest;

import com.gigu.chatnotification.application.dto.CreateConversationCommand;
import com.gigu.chatnotification.application.port.in.ChatNotificationUseCase;
import com.gigu.chatnotification.application.service.ParticipantDisplayNameResolver;
import com.gigu.chatnotification.domain.model.Conversation;
import com.gigu.chatnotification.infrastructure.security.AuthUser;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.List;

class ChatNotificationControllerTest {

    @Test
    void createConversationAllowsNullProjectId() {
        ChatNotificationUseCase service = mock(ChatNotificationUseCase.class);
        ParticipantDisplayNameResolver resolver = mock(ParticipantDisplayNameResolver.class);
        UUID actorId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        Conversation conversation = new Conversation(UUID.randomUUID(), actorId, otherId, null, Instant.parse("2026-06-06T22:00:00Z"));
        when(service.createOrOpenConversation(any(CreateConversationCommand.class))).thenReturn(conversation);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new AuthUser(actorId, Set.of("CLIENT")));

        ChatNotificationController controller = new ChatNotificationController(service, resolver, "svc-token");
        var response = controller.createConversation(authentication, new com.gigu.chatnotification.interfaces.rest.dto.CreateConversationBody(otherId, null));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(conversation.id(), response.getBody().get("id"));
        assertNull(response.getBody().get("projectId"));
        assertEquals(List.of(actorId, otherId), response.getBody().get("participants"));
    }

    @Test
    void conversationDetailAllowsNullProjectId() {
        ChatNotificationUseCase service = mock(ChatNotificationUseCase.class);
        ParticipantDisplayNameResolver resolver = mock(ParticipantDisplayNameResolver.class);
        UUID actorId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        Conversation conversation = new Conversation(conversationId, actorId, otherId, null, Instant.parse("2026-06-06T22:00:00Z"));
        when(service.getConversation(conversationId, actorId)).thenReturn(conversation);
        when(resolver.resolve(otherId)).thenReturn(new ParticipantDisplayNameResolver.ParticipantView(otherId, "Carlos Rojas", "FREELANCER", null));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new AuthUser(actorId, Set.of("CLIENT")));

        ChatNotificationController controller = new ChatNotificationController(service, resolver, "svc-token");
        Map<String, Object> response = controller.conversationDetail(authentication, conversationId);

        assertEquals(conversationId, response.get("id"));
        assertNull(response.get("projectId"));
        assertEquals(1, ((java.util.List<?>) response.get("participants")).size());
        assertEquals("Carlos Rojas", ((Map<?, ?>) ((java.util.List<?>) response.get("participants")).getFirst()).get("displayName"));
    }
}
