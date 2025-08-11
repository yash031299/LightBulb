package com.example.repo;

import com.example.model.LightBulb;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.ArrayList;
import java.util.List;

public class S3LightBulbRepository extends AbstractJsonLightBulbRepository {

    private static final String OBJECT_KEY = "bulbs.json";

    private final String BUCKET_NAME;

    private final S3Client s3;

    private final ObjectMapper mapper = new ObjectMapper();

    public S3LightBulbRepository(S3Client s3) {
        this.s3 = s3;
        this.BUCKET_NAME = System.getenv("BULBS_BUCKET");
        if (BUCKET_NAME == null || BUCKET_NAME.trim().isEmpty()) {
            throw new IllegalArgumentException("BULBS_BUCKET environment variable must be set");
        }
    }

    @Override
    protected List<LightBulb> readAll() {
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

    @Override
    protected void writeAll(List<LightBulb> bulbs) {
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
}
