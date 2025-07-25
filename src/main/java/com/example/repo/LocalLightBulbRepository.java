package com.example.repo;

import com.example.model.LightBulb;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.*;

public class LocalLightBulbRepository extends AbstractJsonLightBulbRepository {

    protected final File file = new File("bulbs.json");
    
    protected File getFile() {
        return file;
    }
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected List<LightBulb> readAll() {
        try {
            File currentFile = getFile();
            if (!currentFile.exists() || currentFile.length() == 0) {
                return new ArrayList<>();
            }
            try {
                return mapper.readValue(currentFile, new TypeReference<>() {});
            } catch (com.fasterxml.jackson.core.JsonParseException e) {
                // If file exists but is not valid JSON, treat as empty
                return new ArrayList<>();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + getFile().getName(), e);
        }
    }

    @Override
    protected void writeAll(List<LightBulb> bulbs) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(getFile(), bulbs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write " + getFile().getName(), e);
        }
    }

}
