package com.example.config;

import com.example.repo.LightBulbRepository;
import com.example.repo.LocalLightBulbRepository;
import com.example.repo.S3LightBulbRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class RepoConfigTest {

    @Test
    @DisplayName("localRepo returns LocalLightBulbRepository instance")
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
    @DisplayName("Verifies S3Repo throws exception if S3Client is not created when BULBS_BUCKET is not configured")
    void s3Repo_throwsException_whenBucketNotConfigured() {
        // Arrange
        try (MockedStatic<S3Client> s3ClientMockedStatic = Mockito.mockStatic(S3Client.class)) {
            S3Client mockClient = mock(S3Client.class);
            S3ClientBuilder mockBuilder = mock(S3ClientBuilder.class);
            
            s3ClientMockedStatic.when(S3Client::builder).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockClient);
            
            RepoConfig config = new RepoConfig();

            // Act & Assert
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, config::s3Repo);
            assertEquals("BULBS_BUCKET environment variable must be set", thrown.getMessage());
        }
    }

    @Test
    @DisplayName("Verifies S3Repo throws RuntimeException if S3Client fails to initialize")
    void s3Repo_throwsRuntimeException_whenS3ClientFails() {
        // Arrange
        try (MockedStatic<S3Client> s3ClientMockedStatic = Mockito.mockStatic(S3Client.class)) {
            S3ClientBuilder mockBuilder = mock(S3ClientBuilder.class);
            
            s3ClientMockedStatic.when(S3Client::builder).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenThrow(new RuntimeException("S3 init failed"));
            
            RepoConfig config = new RepoConfig();

            // Act & Assert
            RuntimeException thrown = assertThrows(RuntimeException.class, config::s3Repo);
            assertEquals("S3 init failed", thrown.getMessage());
        }
    }
}
