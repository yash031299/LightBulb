package com.example.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HealthControllerTest {
    @Test
    @DisplayName("Ping endpoint returns pong")
    void testPingEndpointReturnsPong() throws Exception {
        // Arrange
        HealthController healthController = new HealthController();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(healthController).build();

        // Act
        mockMvc.perform(get("/ping").accept(MediaType.TEXT_PLAIN));

        // Assert
        mockMvc.perform(get("/ping").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }
}
