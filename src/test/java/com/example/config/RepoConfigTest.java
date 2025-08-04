package com.example.config;

import com.example.repo.LightBulbRepository;
import com.example.repo.LocalLightBulbRepository;
import com.example.repo.S3LightBulbRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class RepoConfigTest {

    @Test
    void localRepo_returnsLocalLightBulbRepositoryInstance() {
        // Arrange
        RepoConfig config = new RepoConfig();

        // Act
        LightBulbRepository repository = config.localRepo();

        // Assert
        assertNotNull(repository);
        assertTrue(repository instanceof LocalLightBulbRepository);
    }

    @Test
    void s3Repo_returnsS3LightBulbRepositoryInstance() {
        // Arrange
        try (MockedStatic<S3Client> s3ClientMockedStatic = Mockito.mockStatic(S3Client.class)) {
            S3Client mockClient = mock(S3Client.class);
            s3ClientMockedStatic.when(S3Client::create).thenReturn(mockClient);
            RepoConfig config = new RepoConfig();

            // Act
            LightBulbRepository repository = config.s3Repo();

            // Assert
            assertNotNull(repository);
            assertTrue(repository instanceof S3LightBulbRepository);
        }
    }

    @Test
    void s3Repo_throwsRuntimeException_whenS3ClientFails() {
        // Arrange
        try (MockedStatic<S3Client> s3ClientMockedStatic = Mockito.mockStatic(S3Client.class)) {
            s3ClientMockedStatic.when(S3Client::create).thenThrow(new RuntimeException("S3 init failed"));
            RepoConfig config = new RepoConfig();

            // Act & Assert
            RuntimeException thrown = assertThrows(RuntimeException.class, config::s3Repo);
            assertEquals("S3 init failed", thrown.getMessage());
        }
    }
}
