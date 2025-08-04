package com.example.repo;

import com.example.model.LightBulb;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("local")
class LocalLightBulbRepositoryTest {

    private LocalLightBulbRepository repository;
    private File testFile;

    @BeforeEach
    void setUp() throws Exception {
        // Arrange
        testFile = File.createTempFile("test-bulbs", ".json");

        try (java.io.FileWriter writer = new java.io.FileWriter(testFile)) {
            writer.write("[]");
        }

        repository = new LocalLightBulbRepository() {
            @Override
            protected File getFile() {
                return testFile;
            }
        };
    }

    @AfterEach
    void tearDown() {
        // Arrange
        if (testFile.exists()) testFile.delete();
    }

    @Test
    void saveAndFindAll_whenBulbSaved_shouldReturnBulb() {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setName("Bulb1");
        // Act
        repository.save(bulb);
        List<LightBulb> bulbs = repository.findAll();
        // Assert
        assertEquals(1, bulbs.size());
        assertEquals("Bulb1", bulbs.get(0).getName());
    }

    @Test
    void deleteById_whenCalled_shouldRemoveBulb() {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setId(123L);
        bulb.setName("DeleteMe");
        repository.save(bulb);
        // Act
        repository.deleteById(123L);
        List<LightBulb> bulbs = repository.findAll();
        // Assert
        assertTrue(bulbs.isEmpty());
    }

    @Test
    void saveMultipleBulbs_whenCalled_shouldReturnAllBulbs() {
        // Arrange
        LightBulb bulb1 = new LightBulb(); bulb1.setName("A");
        LightBulb bulb2 = new LightBulb(); bulb2.setName("B");
        // Act
        repository.save(bulb1);
        repository.save(bulb2);
        List<LightBulb> bulbs = repository.findAll();
        // Assert
        assertEquals(2, bulbs.size());
    }

    @Test
    void updateBulb_whenIdExists_shouldUpdateBulb() {
        // Arrange
        LightBulb bulb = new LightBulb(); bulb.setName("Old");
        repository.save(bulb);
        Long id = bulb.getId();
        LightBulb updated = new LightBulb(); updated.setId(id); updated.setName("New");
        // Act
        repository.save(updated);
        // Assert
        assertEquals("New", repository.findById(id).get().getName());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void deleteById_whenIdDoesNotExist_shouldNotThrow() {
        // Act & Assert
        assertDoesNotThrow(() -> repository.deleteById(999L));
    }

    @Test
    void findById_whenIdExists_shouldReturnBulb() {
        // Arrange
        LightBulb bulb = new LightBulb(); bulb.setName("FindMe");
        repository.save(bulb);
        // Act
        var found = repository.findById(bulb.getId());
        // Assert
        assertTrue(found.isPresent());
        assertEquals("FindMe", found.get().getName());
    }

    @Test
    void findById_whenIdDoesNotExist_shouldReturnEmpty() {
        // Act
        var found = repository.findById(123L);
        // Assert
        assertTrue(found.isEmpty());
    }

    @Test
    void save_whenBulbIdIsNull_shouldGenerateId() {
        // Arrange
        LightBulb bulb = new LightBulb(); bulb.setName("AutoId");
        // Act
        repository.save(bulb);
        // Assert
        assertNotNull(bulb.getId());
    }

    @Test
    void findAll_whenFileIsCorrupt_shouldReturnEmptyList() throws Exception {
        // Arrange
        try (java.io.FileWriter writer = new java.io.FileWriter(testFile)) {
            writer.write("not valid json");
        }
        // Act
        List<LightBulb> bulbs = repository.findAll();
        // Assert
        assertTrue(bulbs.isEmpty());
    }

    @Test
    void findAll_whenFileIsEmpty_shouldReturnEmptyList() throws Exception {
        // Arrange
        try (java.io.FileWriter writer = new java.io.FileWriter(testFile)) {
            writer.write("");
        }
        // Act
        List<LightBulb> bulbs = repository.findAll();
        // Assert
        assertTrue(bulbs.isEmpty());
    }
}
