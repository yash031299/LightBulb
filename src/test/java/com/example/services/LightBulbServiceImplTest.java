package com.example.services;

import com.example.exception.ResourceNotFoundException;
import com.example.model.LightBulb;
import com.example.repo.LightBulbRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class LightBulbServiceImplTest {

    @Mock
    private LightBulbRepository repository;
    
    @InjectMocks
    private LightBulbServiceImpl service;
    
    private LightBulb testBulb;

    @BeforeEach
    void setUp() {
        testBulb = new LightBulb();
        testBulb.setId(1L);
        testBulb.setName("Test Bulb");
        testBulb.setType("LED");
        testBulb.setWattage(10);
    }

    @Test
    @DisplayName("Verifies addBulb returns created bulb when valid input is provided")
    void addBulb_whenValidBulb_shouldReturnAddedBulb() {
        // Arrange
        when(repository.save(any(LightBulb.class))).thenReturn(testBulb);
        
        // Act
        LightBulb result = service.addBulb(testBulb);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Bulb", result.getName());
        assertEquals("LED", result.getType());
        assertEquals(10, result.getWattage());
        verify(repository).save(testBulb);
    }
    
    @Test
    @DisplayName("Verifies addBulb throws IllegalArgumentException when bulb is null")
    void addBulb_whenBulbIsNull_shouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> service.addBulb(null));
        assertEquals("LightBulb cannot be null", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Verifies getAllBulbs returns list of bulbs")
    void getAllBulbs_whenCalled_shouldReturnListOfBulbs() {
        // Arrange
        List<LightBulb> bulbs = Arrays.asList(testBulb, testBulb);
        when(repository.findAll()).thenReturn(bulbs);
        
        // Act
        List<LightBulb> result = service.getAllBulbs();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository).findAll();
    }
    
    @Test
    @DisplayName("Verifies getAllBulbs returns empty list when no bulbs exist")
    void getAllBulbs_whenNoBulbsExist_shouldReturnEmptyList() {
        // Arrange
        when(repository.findAll()).thenReturn(Collections.emptyList());
        
        // Act
        List<LightBulb> result = service.getAllBulbs();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Verifies getBulbById returns bulb when it exists")
    void getBulbById_whenExists_shouldReturnBulb() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testBulb));
        
        // Act
        LightBulb result = service.getBulbById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Bulb", result.getName());
        verify(repository).findById(1L);
    }

    @Test
    @DisplayName("Verifies getBulbById throws IllegalArgumentException when id is null")
    void getBulbById_whenIdIsNull_shouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.getBulbById(null));
        assertEquals("Bulb ID cannot be null", exception.getMessage());
        verify(repository, never()).findById(any());
    }

    @Test
    @DisplayName("Verifies getBulbById throws ResourceNotFoundException when bulb does not exist")
    void getBulbById_whenNotExists_shouldThrowResourceNotFoundException() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> service.getBulbById(999L));
        assertEquals("LightBulb not found with id : '999'", exception.getMessage());
        verify(repository).findById(999L);
    }

    @Test
    @DisplayName("Verifies updateBulb returns updated bulb when valid input is provided")
    void updateBulb_whenValidInput_shouldUpdateAndReturnBulb() {
        // Arrange
        LightBulb updatedBulb = new LightBulb();
        updatedBulb.setName("Updated Bulb");
        updatedBulb.setType("CFL");
        updatedBulb.setWattage(15);
        
        when(repository.findById(1L)).thenReturn(Optional.of(testBulb));
        when(repository.save(any(LightBulb.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        LightBulb result = service.updateBulb(1L, updatedBulb);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated Bulb", result.getName());
        assertEquals("CFL", result.getType());
        assertEquals(15, result.getWattage());
        verify(repository).findById(1L);
        verify(repository).save(any(LightBulb.class));
    }
    
    @Test
    @DisplayName("Verifies updateBulb throws IllegalArgumentException when id is null")
    void updateBulb_whenIdIsNull_shouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.updateBulb(null, testBulb));
        assertEquals("Bulb ID cannot be null", exception.getMessage());
        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }
    
    @Test
    @DisplayName("Verifies updateBulb throws IllegalArgumentException when bulb is null")
    void updateBulb_whenBulbIsNull_shouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.updateBulb(1L, null));
        assertEquals("Bulb data cannot be null", exception.getMessage());
        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }
    
    @Test
    @DisplayName("Verifies updateBulb throws ResourceNotFoundException when bulb does not exist")
    void updateBulb_whenBulbNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> service.updateBulb(999L, testBulb));
        assertEquals("LightBulb not found with id : '999'", exception.getMessage());
        verify(repository).findById(999L);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Verifies deleteBulb deletes bulb when it exists")
    void deleteBulb_whenExists_shouldDeleteBulb() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testBulb));
        doNothing().when(repository).deleteById(1L);
        
        // Act
        service.deleteBulb(1L);
        
        // Assert
        verify(repository).findById(1L);
        verify(repository).deleteById(1L);
    }
    
    @Test
    @DisplayName("Verifies deleteBulb throws IllegalArgumentException when id is null")
    void deleteBulb_whenIdIsNull_shouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.deleteBulb(null));
        assertEquals("Bulb ID cannot be null", exception.getMessage());
        verify(repository, never()).findById(any());
        verify(repository, never()).deleteById(any());
    }
    
    @Test
    @DisplayName("Verifies deleteBulb throws ResourceNotFoundException when bulb does not exist")
    void deleteBulb_whenBulbNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> service.deleteBulb(999L));
        assertEquals("LightBulb not found with id : '999'", exception.getMessage());
        verify(repository).findById(999L);
        verify(repository, never()).deleteById(any());
    }
}
