package com.example.controller;

import com.example.exception.GlobalExceptionHandler;
import com.example.exception.ResourceNotFoundException;
import com.example.model.LightBulb;
import com.example.services.LightBulbService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)

class LightBulbControllerTest {

    @Mock
    private LightBulbService service;

    @InjectMocks
    private LightBulbController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Verifies addBulb returns created bulb when valid input is provided")
    void addBulb_whenValidInput_shouldReturnCreatedBulb() throws Exception {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setId(1L);
        bulb.setName("TestBulb");
        bulb.setType("LED");
        bulb.setWattage(10);
        
        when(service.addBulb(any(LightBulb.class))).thenReturn(bulb);

        // Act & Assert
        mockMvc.perform(post("/bulbs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulb)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/bulbs/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("TestBulb"))
                .andExpect(jsonPath("$.type").value("LED"))
                .andExpect(jsonPath("$.wattage").value(10));
                
        verify(service).addBulb(any(LightBulb.class));
    }
    
    @Test
    @DisplayName("Verifies addBulb returns bad request when invalid input is provided")
    void addBulb_whenInvalidInput_shouldReturnBadRequest() throws Exception {
        // Arrange
        LightBulb bulb = new LightBulb(); // Missing required fields
        
        // Act & Assert
        mockMvc.perform(post("/bulbs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulb)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isMap())
                .andExpect(jsonPath("$.validationErrors").isNotEmpty())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.type").exists())
                .andExpect(jsonPath("$.validationErrors.wattage").exists());
                
        verify(service, never()).addBulb(any());
    }

    @Test
    @DisplayName("Verifies getAllBulbs returns a list of bulbs")
    void getAllBulbs_whenBulbsExist_shouldReturnBulbList() throws Exception {
        // Arrange
        LightBulb bulb1 = new LightBulb();
        bulb1.setId(1L);
        bulb1.setName("Bulb1");
        bulb1.setType("LED");
        bulb1.setWattage(10);
        
        LightBulb bulb2 = new LightBulb();
        bulb2.setId(2L);
        bulb2.setName("Bulb2");
        bulb2.setType("CFL");
        bulb2.setWattage(15);
        
        when(service.getAllBulbs()).thenReturn(Arrays.asList(bulb1, bulb2));

        // Act & Assert
        mockMvc.perform(get("/bulbs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Bulb1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Bulb2"));
                
        verify(service).getAllBulbs();
    }
    
    @Test
    @DisplayName("Verifies getAllBulbs returns an empty list when no bulbs exist")
    void getAllBulbs_whenNoBulbs_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(service.getAllBulbs()).thenReturn(Collections.emptyList());
        
        // Act & Assert
        mockMvc.perform(get("/bulbs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
                
        verify(service).getAllBulbs();
    }

    @Test
    @DisplayName("Verifies getBulbById returns a bulb when it exists")
    void getBulbById_whenBulbExists_shouldReturnBulb() throws Exception {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setId(1L);
        bulb.setName("Test Bulb");
        bulb.setType("LED");
        bulb.setWattage(10);
        
        when(service.getBulbById(1L)).thenReturn(bulb);

        // Act & Assert
        mockMvc.perform(get("/bulbs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Bulb"))
                .andExpect(jsonPath("$.type").value("LED"))
                .andExpect(jsonPath("$.wattage").value(10));
                
        verify(service).getBulbById(1L);
    }
    
    @Test
    @DisplayName("Verifies getBulbById returns not found when the bulb does not exist")
    void getBulbById_whenBulbNotExists_shouldReturnNotFound() throws Exception {
        // Arrange
        when(service.getBulbById(999L)).thenThrow(new ResourceNotFoundException("LightBulb", "id", 999L));
        
        // Act & Assert
        mockMvc.perform(get("/bulbs/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("not found")));
                
        verify(service).getBulbById(999L);
    }
    
    @Test
    @DisplayName("Verifies getBulbById returns bad request when the id is invalid")
    void getBulbById_whenInvalidIdFormat_shouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/bulbs/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("Invalid value 'abc' for parameter 'id'")));
                
        verify(service, never()).getBulbById(any());
    }

    @Test
    @DisplayName("Verifies updateBulb returns updated bulb when valid input is provided")
    void updateBulb_whenValidInput_shouldReturnUpdatedBulb() throws Exception {
        // Arrange
        LightBulb updatedBulb = new LightBulb();
        updatedBulb.setId(1L);
        updatedBulb.setName("Updated Bulb");
        updatedBulb.setType("CFL");
        updatedBulb.setWattage(15);
        
        when(service.updateBulb(eq(1L), any(LightBulb.class))).thenReturn(updatedBulb);

        // Act & Assert
        mockMvc.perform(put("/bulbs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedBulb)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Bulb"))
                .andExpect(jsonPath("$.type").value("CFL"))
                .andExpect(jsonPath("$.wattage").value(15));
                
        verify(service).updateBulb(eq(1L), any(LightBulb.class));
    }
    
    @Test
    @DisplayName("Verifies updateBulb returns bad request when id in URL does not match id in request body")
    void updateBulb_whenIdMismatch_shouldReturnBadRequest() throws Exception {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setId(2L);
        bulb.setName("Test Bulb");
        bulb.setType("LED");
        bulb.setWattage(10);
        
        // Act & Assert
        mockMvc.perform(put("/bulbs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulb)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("ID in URL does not match ID in request body")));
                
        verify(service, never()).updateBulb(any(), any());
    }
    
    @Test
    @DisplayName("Verifies updateBulb returns bad request when invalid input is provided")
    void updateBulb_whenInvalidInput_shouldReturnBadRequest() throws Exception {
        // Arrange
        LightBulb bulb = new LightBulb(); // Missing required fields
        
        // Act & Assert
        mockMvc.perform(put("/bulbs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulb)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isMap())
                .andExpect(jsonPath("$.validationErrors").isNotEmpty())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.type").exists())
                .andExpect(jsonPath("$.validationErrors.wattage").exists());
                
        verify(service, never()).updateBulb(any(), any());
    }

    @Test
    @DisplayName("Verifies deleteBulb returns no content when the bulb exists")
    void deleteBulb_whenBulbExists_shouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/bulbs/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
                
        verify(service).deleteBulb(1L);
    }
    
    @Test
    @DisplayName("Verifies deleteBulb returns not found when the bulb does not exist")
    void deleteBulb_whenBulbNotExists_shouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("LightBulb", "id", 999L))
            .when(service).deleteBulb(999L);
        
        // Act & Assert
        mockMvc.perform(delete("/bulbs/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
                
        verify(service).deleteBulb(999L);
    }
    
    @Test
    @DisplayName("Verifies deleteBulb returns bad request when the id is invalid")
    void deleteBulb_whenInvalidIdFormat_shouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/bulbs/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("Invalid value 'abc' for parameter 'id'")));
                
        verify(service, never()).deleteBulb(any());
    }
}
