package com.example.repo;

import com.example.model.LightBulb;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;


public class LocalLightBulbRepository implements LightBulbRepository {

    private final File file = new File("bulbs.json");
    private final ObjectMapper mapper = new ObjectMapper();

    private List<LightBulb> readAll() {
        try {
            if (!file.exists()) return new ArrayList<>();
            return mapper.readValue(file, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to read bulbs.json", e);
        }
    }

    private void writeAll(List<LightBulb> bulbs) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, bulbs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write bulbs.json", e);
        }
    }

    @Override
    public List<LightBulb> findAll() {
        return readAll();
    }

    @Override
    public Optional<LightBulb> findById(Long id) {
        return readAll().stream().filter(b -> b.getId().equals(id)).findFirst();
    }

    @Override
    public LightBulb save(LightBulb bulb) {
        List<LightBulb> bulbs = readAll();
        if(bulb.getId() == null) {
            bulb.setId((long) (bulbs.size()+1));
        }
        bulbs.removeIf(b -> b.getId().equals(bulb.getId()));
        bulbs.add(bulb);
        writeAll(bulbs);
        return bulb;
    }

    @Override
    public void deleteById(Long id) {
        List<LightBulb> bulbs = readAll();
        bulbs.removeIf(b -> b.getId().equals(id));
        writeAll(bulbs);
    }
}
