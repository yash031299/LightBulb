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
        if (testFile.exists()) testFile.delete();
    }

    @Test
    void saveAndFindAll() {
        LightBulb bulb = new LightBulb();
        bulb.setName("Bulb1");
        repository.save(bulb);

        List<LightBulb> bulbs = repository.findAll();
        assertEquals(1, bulbs.size());
        assertEquals("Bulb1", bulbs.get(0).getName());
    }

    @Test
    void deleteById() {
        LightBulb bulb = new LightBulb();
        bulb.setId(123L);
        bulb.setName("DeleteMe");
        repository.save(bulb);
        repository.deleteById(123L);
        List<LightBulb> bulbs = repository.findAll();
        assertTrue(bulbs.isEmpty());
    }
}
