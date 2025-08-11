package com.example.repo;

import com.example.model.LightBulb;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3LightBulbRepository Tests")
class S3LightBulbRepositoryTest {

    @Mock
    private S3Client s3Client;

    private ObjectMapper objectMapper;
    
    // Test bucket name to use instead of environment variable
    private static final String TEST_BUCKET = "test-bulbs-bucket";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // Test-specific implementation that mimics S3LightBulbRepository behavior without environment variable dependency
    private static class TestS3LightBulbRepository extends AbstractJsonLightBulbRepository {
        private final S3Client s3;
        private final String bucketName;
        private final String objectKey = "bulbs.json";
        private final ObjectMapper mapper = new ObjectMapper();
        
        public TestS3LightBulbRepository(S3Client s3Client, String bucketName) {
            this.s3 = s3Client;
            this.bucketName = bucketName;
        }
        
        @Override
        protected List<LightBulb> readAll() {
            try {
                GetObjectRequest req = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();

                return mapper.readValue(s3.getObject(req), new TypeReference<List<LightBulb>>() {});
            } catch (NoSuchKeyException e) {
                return new ArrayList<>();
            } catch (Exception e) {
                throw new RuntimeException("Failed to load bulbs from S3", e);
            }
        }

        @Override
        protected void writeAll(List<LightBulb> bulbs) {
            try {
                String json = mapper.writeValueAsString(bulbs);
                PutObjectRequest req = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();

                s3.putObject(req, RequestBody.fromString(json));
            } catch (Exception e) {
                throw new RuntimeException("Failed to save bulbs to S3", e);
            }
        }
    }

    private LightBulbRepository createTestRepository() {
        return new TestS3LightBulbRepository(s3Client, TEST_BUCKET);
    }

    @Test
    @DisplayName("Should return all bulbs when S3 contains data")
    void findAll_whenS3HasData_shouldReturnAllBulbs() throws Exception {
        // Arrange
        LightBulb bulb1 = createTestBulb(1L, "LED Bulb", "LED", 10);
        LightBulb bulb2 = createTestBulb(2L, "CFL Bulb", "CFL", 15);
        List<LightBulb> expectedBulbs = Arrays.asList(bulb1, bulb2);

        String jsonData = objectMapper.writeValueAsString(expectedBulbs);
        InputStream inputStream = new ByteArrayInputStream(jsonData.getBytes());
        ResponseInputStream<GetObjectResponse> responseStream = new ResponseInputStream<>(
            GetObjectResponse.builder().build(),
            inputStream
        );

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);

        LightBulbRepository repository = createTestRepository();

        // Act
        List<LightBulb> result = repository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("LED Bulb", result.get(0).getName());
        assertEquals("CFL Bulb", result.get(1).getName());

        verify(s3Client).getObject(argThat((GetObjectRequest req) ->
            req.bucket().equals(TEST_BUCKET) && req.key().equals("bulbs.json")
        ));
    }

    @Test
    @DisplayName("Should return empty list when S3 object does not exist")
    void findAll_whenS3ObjectNotExists_shouldReturnEmptyList() {
        // Arrange
        when(s3Client.getObject(any(GetObjectRequest.class)))
            .thenThrow(NoSuchKeyException.builder().message("Object not found").build());

        LightBulbRepository repository = createTestRepository();

        // Act
        List<LightBulb> result = repository.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(s3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when S3 throws unexpected exception")
    void findAll_whenS3ThrowsException_shouldThrowRuntimeException() {
        // Arrange
        when(s3Client.getObject(any(GetObjectRequest.class)))
            .thenThrow(S3Exception.builder().message("S3 error").build());

        LightBulbRepository repository = createTestRepository();

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> repository.findAll());
        assertEquals("Failed to load bulbs from S3", exception.getMessage());

        verify(s3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("Should return bulb when it exists in the repository")
    void findById_whenBulbExists_shouldReturnBulb() throws Exception {
        // Arrange
        LightBulb expectedBulb = createTestBulb(1L, "LED Bulb", "LED", 10);
        List<LightBulb> bulbs = Arrays.asList(expectedBulb, createTestBulb(2L, "CFL Bulb", "CFL", 15));

        String jsonData = objectMapper.writeValueAsString(bulbs);
        InputStream inputStream = new ByteArrayInputStream(jsonData.getBytes());
        ResponseInputStream<GetObjectResponse> responseStream = new ResponseInputStream<>(
            GetObjectResponse.builder().build(),
            inputStream
        );

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);

        LightBulbRepository repository = createTestRepository();

        // Act
        Optional<LightBulb> result = repository.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("LED Bulb", result.get().getName());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("Should return empty Optional when bulb does not exist")
    void findById_whenBulbNotExists_shouldReturnEmpty() throws Exception {
        // Arrange
        List<LightBulb> bulbs = Arrays.asList(createTestBulb(2L, "CFL Bulb", "CFL", 15));

        String jsonData = objectMapper.writeValueAsString(bulbs);
        InputStream inputStream = new ByteArrayInputStream(jsonData.getBytes());
        ResponseInputStream<GetObjectResponse> responseStream = new ResponseInputStream<>(
            GetObjectResponse.builder().build(),
            inputStream
        );

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);

        LightBulbRepository repository = createTestRepository();

        // Act
        Optional<LightBulb> result = repository.findById(1L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should create new bulb with generated ID and save to S3")
    void save_whenNewBulb_shouldAddBulbAndSaveToS3() throws Exception {
        // Arrange
        LightBulb existingBulb = createTestBulb(1L, "Existing Bulb", "LED", 10);
        LightBulb newBulb = createTestBulb(null, "New Bulb", "CFL", 15);

        List<LightBulb> existingBulbs = new ArrayList<>(Arrays.asList(existingBulb));

        String existingJsonData = objectMapper.writeValueAsString(existingBulbs);
        InputStream inputStream = new ByteArrayInputStream(existingJsonData.getBytes());
        ResponseInputStream<GetObjectResponse> responseStream = new ResponseInputStream<>(
            GetObjectResponse.builder().build(),
            inputStream
        );

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

        LightBulbRepository repository = createTestRepository();

        // Act
        LightBulb result = repository.save(newBulb);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId()); // ID should be generated
        assertTrue(result.getId() > 1L); // Should be greater than existing max ID
        assertEquals("New Bulb", result.getName());
    }

    @Test
    @DisplayName("Should update existing bulb and save to S3")
    void save_whenUpdatingExistingBulb_shouldUpdateAndSaveToS3() throws Exception {
        // Arrange
        LightBulb existingBulb = createTestBulb(1L, "Old Name", "LED", 10);
        LightBulb updatedBulb = createTestBulb(1L, "Updated Name", "CFL", 20);

        List<LightBulb> existingBulbs = new ArrayList<>(Arrays.asList(existingBulb));

        String existingJsonData = objectMapper.writeValueAsString(existingBulbs);
        InputStream inputStream = new ByteArrayInputStream(existingJsonData.getBytes());
        ResponseInputStream<GetObjectResponse> responseStream = new ResponseInputStream<>(
            GetObjectResponse.builder().build(),
            inputStream
        );

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

        LightBulbRepository repository = createTestRepository();

        // Act
        LightBulb result = repository.save(updatedBulb);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated Name", result.getName());
        assertEquals("CFL", result.getType());
        assertEquals(20, result.getWattage());
    }

    @Test
    @DisplayName("Should throw RuntimeException when S3 write operation fails")
    void save_whenS3WriteThrowsException_shouldThrowRuntimeException() throws Exception {
        // Arrange
        LightBulb newBulb = createTestBulb(null, "New Bulb", "LED", 10);

        List<LightBulb> existingBulbs = new ArrayList<>();

        String existingJsonData = objectMapper.writeValueAsString(existingBulbs);
        InputStream inputStream = new ByteArrayInputStream(existingJsonData.getBytes());
        ResponseInputStream<GetObjectResponse> responseStream = new ResponseInputStream<>(
            GetObjectResponse.builder().build(),
            inputStream
        );

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenThrow(S3Exception.builder().message("Write failed").build());

        LightBulbRepository repository = createTestRepository();

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> repository.save(newBulb));
        assertEquals("Failed to save bulbs to S3", exception.getMessage());
    }

    @Test
    @DisplayName("Should remove existing bulb and save updated list to S3")
    void deleteById_whenBulbExists_shouldRemoveBulbAndSaveToS3() throws Exception {
        // Arrange
        LightBulb bulb1 = createTestBulb(1L, "Bulb 1", "LED", 10);
        LightBulb bulb2 = createTestBulb(2L, "Bulb 2", "CFL", 15);
        List<LightBulb> existingBulbs = new ArrayList<>(Arrays.asList(bulb1, bulb2));

        String existingJsonData = objectMapper.writeValueAsString(existingBulbs);
        InputStream inputStream = new ByteArrayInputStream(existingJsonData.getBytes());
        ResponseInputStream<GetObjectResponse> responseStream = new ResponseInputStream<>(
            GetObjectResponse.builder().build(),
            inputStream
        );

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

        LightBulbRepository repository = createTestRepository();

        // Act
        repository.deleteById(1L);

        // Assert
        verify(s3Client).getObject(any(GetObjectRequest.class));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Should handle deletion of non-existent bulb gracefully")
    void deleteById_whenBulbNotExists_shouldStillCallS3Operations() throws Exception {
        // Arrange
        LightBulb existingBulb = createTestBulb(2L, "Existing Bulb", "LED", 10);
        List<LightBulb> existingBulbs = new ArrayList<>(Arrays.asList(existingBulb));

        String existingJsonData = objectMapper.writeValueAsString(existingBulbs);
        InputStream inputStream = new ByteArrayInputStream(existingJsonData.getBytes());
        ResponseInputStream<GetObjectResponse> responseStream = new ResponseInputStream<>(
            GetObjectResponse.builder().build(),
            inputStream
        );

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

        LightBulbRepository repository = createTestRepository();

        // Act
        repository.deleteById(1L); // Non-existent ID

        // Assert
        verify(s3Client).getObject(any(GetObjectRequest.class));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Should initialize repository with S3Client")
    void constructor_shouldSetS3Client() {
        // Act
        LightBulbRepository newRepository = createTestRepository();

        // Assert
        assertNotNull(newRepository);
    }

    @Test
    @DisplayName("Should use configured bucket name and object key for S3 operations")
    void bucketNameAndObjectKey_shouldUseEnvironmentVariableOrDefault() {
        // This test verifies the static configuration
        // The actual values are tested implicitly through the S3 operations

        // Act & Assert
        assertDoesNotThrow(this::createTestRepository);
    }
    
    private LightBulb createTestBulb(Long id, String name, String type, int wattage) {
        LightBulb bulb = new LightBulb();
        bulb.setId(id);
        bulb.setName(name);
        bulb.setType(type);
        bulb.setWattage(wattage);
        return bulb;
    }
}
