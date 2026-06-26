package com.gigu.engagement.infrastructure.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        classes = NotificationClientAdapterTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration,org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NotificationClientAdapterTest {
    private static final AtomicInteger requestCount = new AtomicInteger();
    private static final AtomicInteger responseMode = new AtomicInteger(0);
    private static final HttpServer server = startServer(exchange -> {
        requestCount.incrementAndGet();
        respond(exchange, responseMode.get() == 0 ? 200 : 500, responseMode.get() == 0 ? "{\"ok\":true}" : "{\"message\":\"down\"}");
    });

    @Autowired
    private NotificationClientAdapter adapter;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("CHAT_NOTIFICATION_SERVICE_URL", () -> "http://localhost:" + server.getAddress().getPort());
        registry.add("INTERNAL_SERVICE_TOKEN", () -> "svc-token");
    }

    @AfterAll
    static void tearDown() {
        server.stop(0);
    }

    @Test
    void successPathStillCallsNotificationClient() {
        requestCount.set(0);
        responseMode.set(0);

        assertDoesNotThrow(() -> adapter.notifyBestEffort("REQUEST_CREATED", "recipient-1", "New request", "REQUEST", UUID.randomUUID()));
        assertEquals(1, requestCount.get());
    }

    @Test
    void fallbackStopsRepeatedFailures() {
        requestCount.set(0);
        responseMode.set(1);

        assertDoesNotThrow(() -> adapter.notifyBestEffort("REQUEST_CREATED", "recipient-1", "New request", "REQUEST", UUID.randomUUID()));
        assertDoesNotThrow(() -> adapter.notifyBestEffort("REQUEST_CREATED", "recipient-1", "New request", "REQUEST", UUID.randomUUID()));
        assertDoesNotThrow(() -> adapter.notifyBestEffort("REQUEST_CREATED", "recipient-1", "New request", "REQUEST", UUID.randomUUID()));
        assertDoesNotThrow(() -> adapter.notifyBestEffort("REQUEST_CREATED", "recipient-1", "New request", "REQUEST", UUID.randomUUID()));

        assertEquals(3, requestCount.get());
    }

    private static HttpServer startServer(HttpHandler handler) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
            server.createContext("/", handler);
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException("failed to start test server", e);
        }
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(basePackageClasses = NotificationClientAdapter.class)
    static class TestApplication {}
}
