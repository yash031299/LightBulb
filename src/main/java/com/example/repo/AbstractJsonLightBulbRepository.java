package com.example.repo;

import com.example.model.LightBulb;
import java.util.*;


public abstract class AbstractJsonLightBulbRepository implements LightBulbRepository {

    @Override
    public List<LightBulb> findAll() {
        return readAll();
    }

    @Override
    public Optional<LightBulb> findById(Long id) {
        return readAll().stream()
                .filter(bulb -> Objects.equals(bulb.getId(), id))
                .findFirst();
    }

    @Override
    public LightBulb save(LightBulb bulb) {
        List<LightBulb> bulbs = readAll();
        if (bulb.getId() == null) {
            bulb.setId(generateId());
        }
        bulbs.removeIf(b -> Objects.equals(b.getId(), bulb.getId()));
        bulbs.add(bulb);
        writeAll(bulbs);
        return bulb;
    }

    @Override
    public void deleteById(Long id) {
        List<LightBulb> bulbs = readAll();
        bulbs.removeIf(b -> Objects.equals(b.getId(), id));
        writeAll(bulbs);
    }

    protected abstract List<LightBulb> readAll();
    protected abstract void writeAll(List<LightBulb> bulbs);

    protected long generateId() {
        Random rand = new Random();
        return (System.currentTimeMillis() + rand.nextInt(10000)) % 9137;
    }
}
