package com.gigu.marketplace.infrastructure.storage;

import com.gigu.marketplace.application.exception.SupabaseStorageException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupabaseStorageAdapterTest {
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void storeUsesSupabaseRestUrlRawBytesAndHeaders() throws Exception {
        AtomicReference<RequestCapture> capture = new AtomicReference<>();
        server = startServer(exchange -> {
            capture.set(RequestCapture.from(exchange));
            respond(exchange, 200, "{\"Key\":\"services/123/file.png\"}");
        });

        var adapter = new SupabaseStorageAdapter(baseUrl(), "service-role-key", "gig-media", HttpClient.newHttpClient());
        var stored = adapter.store("123e4567-e89b-12d3-a456-426614174000", "image/png", "ignored.png", new byte[]{1, 2, 3});

        assertEquals("gig-media", stored.bucket());
        assertTrue(stored.objectPath().startsWith("services/123e4567-e89b-12d3-a456-426614174000/"));
        assertTrue(stored.objectPath().endsWith(".png"));
        assertEquals(baseUrl() + "/storage/v1/object/public/gig-media/" + stored.objectPath(), stored.publicUrl());

        RequestCapture request = capture.get();
        assertNotNull(request);
        assertEquals("POST", request.method);
        assertEquals("/storage/v1/object/gig-media/" + stored.objectPath(), request.path);
        assertArrayEquals(new byte[]{1, 2, 3}, request.body);
        assertEquals("Bearer service-role-key", request.authorization);
        assertEquals("service-role-key", request.apikey);
        assertEquals("false", request.xUpsert);
        assertEquals("image/png", request.contentType);
    }

    @Test
    void storeIncludesSupabaseErrorBodyInException() throws Exception {
        server = startServer(exchange -> respond(exchange, 400, "{\"message\":\"new row violates row-level security policy\"}"));
        var adapter = new SupabaseStorageAdapter(baseUrl(), "service-role-key", "gig-media", HttpClient.newHttpClient());

        SupabaseStorageException ex = assertThrows(SupabaseStorageException.class,
                () -> adapter.store("123e4567-e89b-12d3-a456-426614174000", "image/png", "ignored.png", new byte[]{1}));

        assertTrue(ex.getMessage().contains("new row violates row-level security policy"));
    }

    @Test
    void validateObjectPathRejectsInvalidPath() {
        var adapter = new SupabaseStorageAdapter("http://localhost:1", "service-role-key", "gig-media", HttpClient.newHttpClient());

        assertThrows(IllegalArgumentException.class, () -> adapter.validateObjectPath("/services/x/y.png"));
        assertThrows(IllegalArgumentException.class, () -> adapter.validateObjectPath("services\\x\\y.png"));
        assertThrows(IllegalArgumentException.class, () -> adapter.validateObjectPath("services/x y.png"));
    }

    private HttpServer startServer(HttpHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", handler);
        server.start();
        return server;
    }

    private String baseUrl() {
        return "http://localhost:" + server.getAddress().getPort();
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static class RequestCapture {
        final String method;
        final String path;
        final byte[] body;
        final String authorization;
        final String apikey;
        final String xUpsert;
        final String contentType;

        private RequestCapture(String method, String path, byte[] body, String authorization, String apikey, String xUpsert, String contentType) {
            this.method = method;
            this.path = path;
            this.body = body;
            this.authorization = authorization;
            this.apikey = apikey;
            this.xUpsert = xUpsert;
            this.contentType = contentType;
        }

        static RequestCapture from(HttpExchange exchange) throws IOException {
            byte[] body = exchange.getRequestBody().readAllBytes();
            return new RequestCapture(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().getPath(),
                    body,
                    exchange.getRequestHeaders().getFirst("Authorization"),
                    exchange.getRequestHeaders().getFirst("apikey"),
                    exchange.getRequestHeaders().getFirst("x-upsert"),
                    exchange.getRequestHeaders().getFirst("Content-Type"));
        }
    }
}
