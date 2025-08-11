package com.example.repo;

import com.example.model.LightBulb;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("local")
class LocalLightBulbRepositoryTest {

    private LocalLightBulbRepository repository;
    private File testFile;

    @BeforeEach
    void setUp() throws IOException {
        testFile = new File("test-bulbs.json");
        if (testFile.exists()) {
            testFile.delete();
        }
        testFile.createNewFile();
        
        // Write empty JSON array to the file
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("[]");
        }

        repository = new LocalLightBulbRepository() {
            @Override
            protected List<LightBulb> readAll() {
                try {
                    if (!testFile.exists() || testFile.length() == 0) {
                        return new ArrayList<>();
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(testFile, new TypeReference<List<LightBulb>>() {});
                } catch (Exception e) {
                    // If JSON parsing fails, return empty list (for corrupt file scenarios)
                    return new ArrayList<>();
                }
            }

            @Override
            protected void writeAll(List<LightBulb> bulbs) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.writerWithDefaultPrettyPrinter()
                            .writeValue(testFile, bulbs);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write bulbs to test file", e);
                }
            }
        };
    }

    @AfterEach
    void tearDown() {
        // Arrange
        if (testFile.exists()) testFile.delete();
    }

    @Test
    @DisplayName("Verifies that save and find all work when it is called with a valid bulb")
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
    @DisplayName("Verifies that delete by id works when it is called with a valid id")
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
    @DisplayName("Verifies that save multiple bulbs works when it is called with valid bulbs")
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
    @DisplayName("Verifies that update bulb works when it is called with a valid bulb")
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
    @DisplayName("Verifies that delete by id works when it is called with a valid id")
    void deleteById_whenIdDoesNotExist_shouldNotThrow() {
        // Act & Assert
        assertDoesNotThrow(() -> repository.deleteById(999L));
    }

    @Test
    @DisplayName("Verifies that find by id works when it is called with a valid id")
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
    @DisplayName("Verifies that find by id should return empty when id does not exist")
    void findById_whenIdDoesNotExist_shouldReturnEmpty() {
        // Act
        var found = repository.findById(123L);
        // Assert
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Verifies that save works when it is called with a valid bulb and returns the bulb with a generated id")
    void save_whenBulbIdIsNull_shouldGenerateId() {
        // Arrange
        LightBulb bulb = new LightBulb(); bulb.setName("AutoId");
        // Act
        repository.save(bulb);
        // Assert
        assertNotNull(bulb.getId());
    }

    @Test
    @DisplayName("Verifies that find all works when it is called")
    void findAll_whenFileIsCorrupt_shouldReturnEmptyList() throws Exception {
        // Arrange
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("not valid json");
        }
        // Act
        List<LightBulb> bulbs = repository.findAll();
        // Assert
        assertTrue(bulbs.isEmpty());
    }

    @Test
    @DisplayName("Verifies that find all works when it is called")
    void findAll_whenFileIsEmpty_shouldReturnEmptyList() throws Exception {
        // Arrange
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("");
        }
        // Act
        List<LightBulb> bulbs = repository.findAll();
        // Assert
        assertTrue(bulbs.isEmpty());
    }
}
