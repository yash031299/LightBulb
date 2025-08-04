package com.example.controller;

import com.example.model.LightBulb;
import com.example.services.LightBulbService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LightBulbControllerTest {
    private MockMvc mockMvc;
    private LightBulbService service;

    @BeforeEach
    void setUp() {
        // Arrange
        service = mock(LightBulbService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LightBulbController(service)).build();
    }

    @Test
    void addBulb_whenValidInput_shouldReturnAddedBulb() throws Exception {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setName("TestBulb");
        bulb.setType("LED");
        bulb.setWattage(10);
        when(service.addBulb(any(LightBulb.class))).thenReturn(bulb);

        // Act
        var result = mockMvc.perform(post("/bulbs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"TestBulb\",\"type\":\"LED\",\"wattage\":10}"));

        // Assert
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TestBulb"))
                .andExpect(jsonPath("$.type").value("LED"))
                .andExpect(jsonPath("$.wattage").value(10));
    }

    @Test
    void getAllBulbs_whenCalled_shouldReturnBulbList() throws Exception {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setName("Bulb1");
        when(service.getAllBulbs()).thenReturn(Collections.singletonList(bulb));

        // Act
        var result = mockMvc.perform(get("/bulbs"));

        // Assert
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bulb1"));
    }

    @Test
    void getBulbById_whenBulbExists_shouldReturnBulb() throws Exception {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setName("Bulb2");
        when(service.getBulbById(1L)).thenReturn(bulb);

        // Act
        var result = mockMvc.perform(get("/bulbs/1"));

        // Assert
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bulb2"));
    }

    @Test
    void updateBulb_whenValidInput_shouldReturnUpdatedBulb() throws Exception {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setName("Updated");
        bulb.setType("CFL");
        bulb.setWattage(15);
        when(service.updateBulb(eq(1L), any(LightBulb.class))).thenReturn(bulb);

        // Act
        var result = mockMvc.perform(put("/bulbs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated\",\"type\":\"CFL\",\"wattage\":15}"));

        // Assert
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.type").value("CFL"))
                .andExpect(jsonPath("$.wattage").value(15));
    }

    @Test
    void deleteBulb_whenCalled_shouldCallServiceDelete() throws Exception {
        // Arrange
        doNothing().when(service).deleteBulb(1L);

        // Act
        var result = mockMvc.perform(delete("/bulbs/1"));

        // Assert
        result
                .andExpect(status().isOk());
        verify(service).deleteBulb(1L);
    }
}
