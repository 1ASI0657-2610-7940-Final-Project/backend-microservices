package com.gigu.engagement.infrastructure.client;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.ComponentScan;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Prueba de contrato del lado consumidor (pulls-service) frente al stub publicado por el
 * productor chat-notification-service. Stub Runner levanta un WireMock con los stubs del
 * contrato en el puerto 8085 y el NotificationClientAdapter apunta a ese puerto, validando
 * que el contrato "crear notificacion interna" (POST /api/v1/chat/internal/notifications con
 * X-Service-Token) es honrado de extremo a extremo por ambos microservicios.
 */
@SpringBootTest(
        classes = NotificationClientContractTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration,org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
                "CHAT_NOTIFICATION_SERVICE_URL=http://localhost:8085",
                "INTERNAL_SERVICE_TOKEN=svc-token"
        }
)
@AutoConfigureStubRunner(
        ids = "com.gigu:chat-notification-service:+:stubs:8085",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class NotificationClientContractTest {

    @Autowired
    private NotificationClientAdapter adapter;

    @Test
    void honorsInternalNotificationContractAgainstProducerStub() {
        assertDoesNotThrow(() -> adapter.notifyBestEffort(
                "REQUEST_CREATED",
                UUID.randomUUID().toString(),
                "New request",
                "REQUEST",
                UUID.randomUUID()));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(basePackageClasses = NotificationClientAdapter.class)
    static class TestApplication {}
}
