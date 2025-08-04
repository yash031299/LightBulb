package com.example.services;

import com.example.model.LightBulb;
import com.example.repo.LightBulbRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class LightBulbServiceImplTest {

    private LightBulbRepository repository;
    private LightBulbServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(LightBulbRepository.class);
        service = new LightBulbServiceImpl(repository);
    }

    @Test
    void addBulb_whenValidBulb_shouldReturnAddedBulb() {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setName("TestBulb");
        when(repository.save(any(LightBulb.class))).thenReturn(bulb);
        // Act
        LightBulb result = service.addBulb(bulb);
        // Assert
        assertEquals("TestBulb", result.getName());
        verify(repository).save(bulb);
    }

    @Test
    void getAllBulbs_whenCalled_shouldReturnList() {
        // Arrange
        List<LightBulb> bulbs = List.of(new LightBulb());
        when(repository.findAll()).thenReturn(bulbs);
        // Act
        List<LightBulb> result = service.getAllBulbs();
        // Assert
        assertEquals(1, result.size());
        verify(repository).findAll();
    }

    @Test
    void getBulbById_whenExists_shouldReturnBulb() {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(bulb));
        // Act
        LightBulb result = service.getBulbById(1L);
        // Assert
        assertEquals(1L, result.getId());
        verify(repository).findById(1L);
    }

    @Test
    void getBulbById_whenNotExists_shouldThrowException() {
        // Arrange
        when(repository.findById(2L)).thenReturn(Optional.empty());
        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getBulbById(2L));
        assertTrue(ex.getMessage().contains("Bulb not found"));
    }

    @Test
    void updateBulb_whenExists_shouldUpdateFields() {
        // Arrange
        LightBulb existing = new LightBulb();
        existing.setId(1L);
        existing.setName("Old");
        existing.setType("A");
        existing.setWattage(5);
        LightBulb update = new LightBulb();
        update.setName("New");
        update.setType("B");
        update.setWattage(10);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(LightBulb.class))).thenReturn(update);
        // Act
        LightBulb result = service.updateBulb(1L, update);
        // Assert
        assertEquals("New", result.getName());
        assertEquals("B", result.getType());
        assertEquals(10, result.getWattage());
        verify(repository).save(existing);
    }

    @Test
    void updateBulb_whenNotExists_shouldThrowException() {
        // Arrange
        LightBulb update = new LightBulb();
        when(repository.findById(99L)).thenReturn(Optional.empty());
        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.updateBulb(99L, update));
        assertTrue(ex.getMessage().contains("Bulb not found"));
    }

    @Test
    void deleteBulb_whenCalled_shouldCallDeleteById() {
        // Act
        service.deleteBulb(1L);
        // Assert
        verify(repository).deleteById(1L);
    }
}
