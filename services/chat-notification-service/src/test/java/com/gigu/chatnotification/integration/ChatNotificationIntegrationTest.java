package com.gigu.chatnotification.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class ChatNotificationIntegrationTest {
    @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    @DynamicPropertySource static void props(DynamicPropertyRegistry r){ r.add("SPRING_DATASOURCE_URL", postgres::getJdbcUrl); r.add("SPRING_DATASOURCE_USERNAME", postgres::getUsername); r.add("SPRING_DATASOURCE_PASSWORD", postgres::getPassword); r.add("JWT_SECRET",()->"jwt-secret-change-me-jwt-secret-change-me"); r.add("SERVICE_TOKEN",()->"svc-token"); }
    @Autowired MockMvc mvc;
    @Test void internalNotificationRequiresServiceToken() throws Exception {
        mvc.perform(post("/api/v1/chat/internal/notifications").contentType("application/json").content("{\"recipientId\":\"11111111-1111-1111-1111-111111111111\",\"type\":\"T\",\"title\":\"t\",\"message\":\"m\"}"))
                .andExpect(status().isForbidden());
    }
}
