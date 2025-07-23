package com.example.repo;

import com.example.model.LightBulb;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.xml.crypto.Data;
import java.util.*;

public class S3LightBulbRepository implements LightBulbRepository {

    private static final String BUCKET_NAME = System.getenv().getOrDefault("BULBS_BUCKET", "default-bucket-name");
    private static final String OBJECT_KEY = "bulbs.json";

    private final S3Client s3;
    private final ObjectMapper mapper = new ObjectMapper();

    public S3LightBulbRepository(S3Client s3) {
        this.s3 = s3;
    }

    private List<LightBulb> readAll() {
        try {
            GetObjectRequest req = GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(OBJECT_KEY)
                    .build();

            return mapper.readValue(s3.getObject(req), new TypeReference<>() {});
        } catch (NoSuchKeyException e) {
            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load bulbs from S3", e);
        }
    }

    private void writeAll(List<LightBulb> bulbs) {
        try {
            byte[] data = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(bulbs);
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(OBJECT_KEY)
                    .build();
            s3.putObject(req, RequestBody.fromBytes(data));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save bulbs to S3", e);
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
            Random rand = new Random();
            // A sample hash to generate a random ID
            long id = (System.currentTimeMillis() + rand.nextLong(10000)) % 9137;
            bulb.setId(id);
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
