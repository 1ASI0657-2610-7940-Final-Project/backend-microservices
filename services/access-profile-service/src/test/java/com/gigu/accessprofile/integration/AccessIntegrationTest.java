package com.gigu.accessprofile.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class AccessIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("SPRING_DATASOURCE_URL", postgres::getJdbcUrl);
        r.add("SPRING_DATASOURCE_USERNAME", postgres::getUsername);
        r.add("SPRING_DATASOURCE_PASSWORD", postgres::getPassword);
        r.add("JWT_SECRET", () -> "jwt-secret-change-me-jwt-secret-change-me");
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @Test
    void getMeRequiresJwt() throws Exception {
        mockMvc.perform(get("/api/v1/access/me")).andExpect(status().isForbidden());
    }

    @Test
    void signUpAndLoginFlow() throws Exception {
        var signUp = Map.of("firstName","Ana","lastName","Rojas","email","ana.rojas@upc.edu.pe","password","Password123!","role","FREELANCER");
        mockMvc.perform(post("/api/v1/access/sign-up").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(signUp))).andExpect(status().isCreated());
        var login = Map.of("email","ana.rojas@upc.edu.pe","password","Password123!");
        mockMvc.perform(post("/api/v1/access/login").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(login))).andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void signUpRejectsDuplicateEmail() throws Exception {
        var body = Map.of("firstName","Ana","lastName","Rojas","email","dup@upc.edu.pe","password","Password123!","role","CLIENT");
        mockMvc.perform(post("/api/v1/access/sign-up").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/access/sign-up").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isBadRequest());
    }
}
