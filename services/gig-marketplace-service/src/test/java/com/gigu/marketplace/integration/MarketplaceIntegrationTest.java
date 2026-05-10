package com.gigu.marketplace.integration;

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
class MarketplaceIntegrationTest {
    @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    @DynamicPropertySource static void props(DynamicPropertyRegistry r){ r.add("SPRING_DATASOURCE_URL", postgres::getJdbcUrl); r.add("SPRING_DATASOURCE_USERNAME", postgres::getUsername); r.add("SPRING_DATASOURCE_PASSWORD", postgres::getPassword); r.add("JWT_SECRET",()->"jwt-secret-change-me-jwt-secret-change-me"); }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test void categoriesEndpointReturnsSeeded() throws Exception {
        mvc.perform(get("/api/v1/marketplace/categories")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Design")));
    }

    @Test void searchFiltersByCategoryAndPrice() throws Exception {
        mvc.perform(get("/api/v1/marketplace/services").param("category","Design").param("priceMin","1").param("priceMax","500")).andExpect(status().isOk()).andExpect(jsonPath("$.data").exists());
    }
}
