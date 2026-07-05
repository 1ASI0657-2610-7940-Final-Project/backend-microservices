package com.gigu.chatnotification.contract;

import com.gigu.chatnotification.application.dto.CreateInternalNotificationCommand;
import com.gigu.chatnotification.application.port.in.ChatNotificationUseCase;
import com.gigu.chatnotification.application.service.ParticipantDisplayNameResolver;
import com.gigu.chatnotification.domain.model.Notification;
import com.gigu.chatnotification.interfaces.rest.ChatNotificationController;
import com.gigu.chatnotification.interfaces.rest.RestExceptionHandler;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Clase base para las pruebas de contrato (Spring Cloud Contract) del productor
 * chat-notification-service. Levanta el controlador REST con MockMvc en modo standalone,
 * con el caso de uso mockeado, de modo que los contratos se verifican sin base de datos,
 * sin Pub/Sub y sin necesidad de Docker. El token de servicio se fija en "internal-token",
 * el mismo valor que envían los contratos en la cabecera X-Service-Token.
 */
public abstract class ContractVerifierBase {

    @BeforeEach
    void setup() {
        ChatNotificationUseCase service = mock(ChatNotificationUseCase.class);
        ParticipantDisplayNameResolver resolver = mock(ParticipantDisplayNameResolver.class);

        // El caso de uso refleja el comando recibido en la notificación creada,
        // de modo que la respuesta reproduce los datos de la solicitud del contrato.
        when(service.createInternalNotification(any(CreateInternalNotificationCommand.class)))
                .thenAnswer(invocation -> {
                    CreateInternalNotificationCommand c = invocation.getArgument(0);
                    return new Notification(
                            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"),
                            c.recipientId(),
                            c.type(),
                            c.title(),
                            c.message(),
                            c.resourceType(),
                            c.resourceId(),
                            false,
                            Instant.parse("2026-06-27T10:15:30Z"),
                            null);
                });

        ChatNotificationController controller =
                new ChatNotificationController(service, resolver, "internal-token");

        RestAssuredMockMvc.standaloneSetup(
                MockMvcBuilders.standaloneSetup(controller)
                        .setControllerAdvice(new RestExceptionHandler()));
    }
}
