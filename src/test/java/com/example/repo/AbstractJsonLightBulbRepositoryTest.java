package com.example.repo;

import com.example.model.LightBulb;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AbstractJsonLightBulbRepositoryTest {

    static class DummyRepo extends AbstractJsonLightBulbRepository {
        private List<LightBulb> store = new ArrayList<>();
        @Override protected List<LightBulb> readAll() {
            return new ArrayList<>(store);
        }
        @Override protected void writeAll(List<LightBulb> bulbs) {
            store = new ArrayList<>(bulbs);
        }
    }

    @Test
    void saveAndDelete_whenCalled_shouldSaveAndDeleteBulb() {
        // Arrange
        DummyRepo repo = new DummyRepo();
        LightBulb bulb = new LightBulb();
        bulb.setName("X");
        // Act
        repo.save(bulb);
        // Assert
        assertEquals(1, repo.findAll().size());

        // Act
        repo.deleteById(bulb.getId());
        // Assert
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void saveMultipleBulbs_whenCalled_shouldContainAllBulbs() {
        // Arrange
        DummyRepo repo = new DummyRepo();
        LightBulb bulb1 = new LightBulb(); bulb1.setName("A");
        LightBulb bulb2 = new LightBulb(); bulb2.setName("B");
        // Act
        repo.save(bulb1);
        repo.save(bulb2);
        // Assert
        assertEquals(2, repo.findAll().size());
    }

    @Test
    void updateBulb_whenIdExists_shouldUpdateBulb() {
        // Arrange
        DummyRepo repo = new DummyRepo();
        LightBulb bulb = new LightBulb(); bulb.setName("Old");
        repo.save(bulb);
        Long id = bulb.getId();
        LightBulb updated = new LightBulb(); updated.setId(id); updated.setName("New");
        // Act
        repo.save(updated);
        // Assert
        assertEquals("New", repo.findById(id).get().getName());
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void deleteById_whenIdDoesNotExist_shouldNotThrow() {
        // Arrange
        DummyRepo repo = new DummyRepo();
        // Act & Assert
        assertDoesNotThrow(() -> repo.deleteById(999L));
    }

    @Test
    void findById_whenIdExists_shouldReturnBulb() {
        // Arrange
        DummyRepo repo = new DummyRepo();
        LightBulb bulb = new LightBulb(); bulb.setName("FindMe");
        repo.save(bulb);
        // Act
        Optional<LightBulb> found = repo.findById(bulb.getId());
        // Assert
        assertTrue(found.isPresent());
        assertEquals("FindMe", found.get().getName());
    }

    @Test
    void findById_whenIdDoesNotExist_shouldReturnEmpty() {
        // Arrange
        DummyRepo repo = new DummyRepo();
        // Act
        Optional<LightBulb> found = repo.findById(123L);
        // Assert
        assertTrue(found.isEmpty());
    }

    @Test
    void save_whenBulbIdIsNull_shouldGenerateId() {
        // Arrange
        DummyRepo repo = new DummyRepo();
        LightBulb bulb = new LightBulb(); bulb.setName("AutoId");
        // Act
        repo.save(bulb);
        // Assert
        assertNotNull(bulb.getId());
    }
}
