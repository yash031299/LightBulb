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
    void saveAndDeleteWorks() {
        DummyRepo repo = new DummyRepo();
        LightBulb bulb = new LightBulb();
        bulb.setName("X");
        repo.save(bulb);
        assertEquals(1, repo.findAll().size());

        repo.deleteById(bulb.getId());
        assertTrue(repo.findAll().isEmpty());
    }
}
