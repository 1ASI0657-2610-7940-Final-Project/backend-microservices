package com.gigu.marketplace.infrastructure.storage;

import com.gigu.marketplace.application.exception.SupabaseStorageException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = SupabaseStorageClientTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration,org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SupabaseStorageClientTest {
    private static final AtomicInteger requestCount = new AtomicInteger();
    private static final AtomicInteger responseMode = new AtomicInteger(0);
    private static final AtomicReference<RequestCapture> capture = new AtomicReference<>();
    private static final HttpServer server = startServer(exchange -> {
        capture.set(RequestCapture.from(exchange));
        requestCount.incrementAndGet();
        respond(exchange, responseMode.get() == 0 ? 200 : 500, responseMode.get() == 0 ? "{\"Key\":\"ok\"}" : "{\"message\":\"row level security\"}");
    });

    @Autowired
    private SupabaseStorageClient client;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("SUPABASE_URL", () -> "http://localhost:" + server.getAddress().getPort());
        registry.add("SUPABASE_SERVICE_ROLE_KEY", () -> "service-role-key");
    }

    @AfterAll
    static void tearDown() {
        server.stop(0);
    }

    @Test
    void successPathUsesExpectedRestUrlHeadersAndRawBytes() {
        requestCount.set(0);
        responseMode.set(0);
        capture.set(null);
        byte[] bytes = new byte[]{1, 2, 3};

        assertDoesNotThrow(() -> client.upload("gig-media", "services/service-1/file.png", "image/png", bytes));
        assertEquals(1, requestCount.get());
        RequestCapture request = capture.get();
        assertNotNull(request);
        assertEquals("POST", request.method);
        assertEquals("/storage/v1/object/gig-media/services/service-1/file.png", request.path);
        assertArrayEquals(bytes, request.body);
        assertEquals("Bearer service-role-key", request.authorization);
        assertEquals("service-role-key", request.apikey);
        assertEquals("false", request.xUpsert);
        assertEquals("image/png", request.contentType);
    }

    @Test
    void failurePathReturnsControlledExceptionAndTripsCircuitBreaker() {
        requestCount.set(0);
        responseMode.set(1);

        SupabaseStorageException first = assertThrows(SupabaseStorageException.class, () -> client.upload("gig-media", "services/service-1/file.png", "image/png", new byte[]{1}));
        assertThrows(SupabaseStorageException.class, () -> client.upload("gig-media", "services/service-1/file.png", "image/png", new byte[]{1}));
        assertThrows(SupabaseStorageException.class, () -> client.upload("gig-media", "services/service-1/file.png", "image/png", new byte[]{1}));
        assertThrows(SupabaseStorageException.class, () -> client.upload("gig-media", "services/service-1/file.png", "image/png", new byte[]{1}));

        assertTrue(first.getMessage().contains("row level security"));
        assertEquals(3, requestCount.get());
    }

    @Test
    void deleteFailureReturnsControlledException() {
        responseMode.set(1);
        assertThrows(SupabaseStorageException.class, () -> client.delete("gig-media", "services/service-1/file.png"));
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

    private record RequestCapture(String method, String path, byte[] body, String authorization, String apikey, String xUpsert, String contentType) {
        static RequestCapture from(HttpExchange exchange) throws IOException {
            return new RequestCapture(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestBody().readAllBytes(),
                    exchange.getRequestHeaders().getFirst("Authorization"),
                    exchange.getRequestHeaders().getFirst("apikey"),
                    exchange.getRequestHeaders().getFirst("x-upsert"),
                    exchange.getRequestHeaders().getFirst("Content-Type"));
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(basePackageClasses = SupabaseStorageClient.class)
    static class TestApplication {}
}
