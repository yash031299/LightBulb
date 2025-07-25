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
    void addBulb_shouldReturnAddedBulb() {
        LightBulb bulb = new LightBulb();
        bulb.setName("TestBulb");
        when(repository.save(any(LightBulb.class))).thenReturn(bulb);

        LightBulb result = service.addBulb(bulb);

        assertEquals("TestBulb", result.getName());
        verify(repository).save(bulb);
    }

    @Test
    void getAllBulbs_shouldReturnList() {
        List<LightBulb> bulbs = List.of(new LightBulb());
        when(repository.findAll()).thenReturn(bulbs);

        List<LightBulb> result = service.getAllBulbs();
        assertEquals(1, result.size());
        verify(repository).findAll();
    }

    @Test
    void getBulbById_shouldReturnBulb() {
        LightBulb bulb = new LightBulb();
        bulb.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(bulb));

        LightBulb result = service.getBulbById(1L);
        assertEquals(1L, result.getId());
        verify(repository).findById(1L);
    }

    @Test
    void updateBulb_shouldUpdateFields() {
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

        LightBulb result = service.updateBulb(1L, update);
        assertEquals("New", result.getName());
        assertEquals("B", result.getType());
        assertEquals(10, result.getWattage());

        verify(repository).save(existing);
    }

    @Test
    void deleteBulb_shouldCallDeleteById() {
        service.deleteBulb(1L);
        verify(repository).deleteById(1L);
    }
}
