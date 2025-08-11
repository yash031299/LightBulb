package com.example.repo;

import com.example.model.LightBulb;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalLightBulbRepository extends AbstractJsonLightBulbRepository {

    private static final String FILE_NAME = "bulbs.json";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected List<LightBulb> readAll() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(file, new TypeReference<List<LightBulb>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to read bulbs from file: " + FILE_NAME, e);
        }
    }


    @Override
    protected void writeAll(List<LightBulb> bulbs) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(FILE_NAME), bulbs);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write bulbs to file: " + FILE_NAME, e);
        }
    }
}
