package com.example.repo;

import com.example.model.LightBulb;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DynamoDbLightBulbRepositoryTest {

    @Mock
    private DynamoDbClient dynamoDbClient;
    
    @Mock
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;
    
    @Mock
    private DynamoDbTable<LightBulb> lightBulbTable;

    @Mock
    private PageIterable<LightBulb> pageIterable;

    @Mock
    private SdkIterable<LightBulb> sdkIterable;

    private DynamoDbLightBulbRepository repository;
    private static final String TABLE_NAME = "test-lightbulbs";
    private MockedStatic<DynamoDbEnhancedClient> mockEnhancedClientStatic;

    @BeforeEach
    void setUp() {
        // Mock the DynamoDbEnhancedClient creation
        mockEnhancedClientStatic = mockStatic(DynamoDbEnhancedClient.class);
        
        DynamoDbEnhancedClient.Builder mockBuilder = mock(DynamoDbEnhancedClient.Builder.class);
        when(DynamoDbEnhancedClient.builder()).thenReturn(mockBuilder);
        when(mockBuilder.dynamoDbClient(dynamoDbClient)).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(dynamoDbEnhancedClient);
        
        when(dynamoDbEnhancedClient.table(eq(TABLE_NAME), any(TableSchema.class)))
            .thenReturn(lightBulbTable);
            
        repository = new DynamoDbLightBulbRepository(dynamoDbClient, TABLE_NAME);
    }

    @AfterEach
    void tearDown() {
        if (mockEnhancedClientStatic != null) {
            mockEnhancedClientStatic.close();
        }
    }

    @Test
    @DisplayName("Verifies save method works when it is called with a valid bulb")
    void save_shouldSaveBulb() {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setId(1L);
        bulb.setName("Test Bulb");
        bulb.setType("LED");
        bulb.setWattage(10);
        
        // Act
        LightBulb savedBulb = repository.save(bulb);
        
        // Assert
        assertNotNull(savedBulb);
        assertEquals(bulb, savedBulb);
        verify(lightBulbTable).putItem(bulb);
    }

    @Test
    @DisplayName("Verifies save method works when it is called with a bulb with no id")
    void save_whenBulbHasNoId_shouldGenerateId() {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setName("Test Bulb");
        bulb.setType("LED");
        bulb.setWattage(10);
        
        // Act
        LightBulb savedBulb = repository.save(bulb);
        
        // Assert
        assertNotNull(savedBulb);
        assertNotNull(savedBulb.getId());
        verify(lightBulbTable).putItem(bulb);
    }

    @Test
    @DisplayName("Verifies findById method returns bulb when it is called with existing id")
    void findById_whenBulbExists_shouldReturnBulb() {
        // Arrange
        Long id = 1L;
        LightBulb expectedBulb = new LightBulb();
        expectedBulb.setId(id);
        expectedBulb.setName("Test Bulb");
        
        when(lightBulbTable.getItem(any(Key.class))).thenReturn(expectedBulb);
        
        // Act
        Optional<LightBulb> result = repository.findById(id);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedBulb, result.get());
        verify(lightBulbTable).getItem(any(Key.class));
    }

    @Test
    @DisplayName("Verifies findById method returns empty when it is called with non-existing id")
    void findById_whenBulbDoesNotExist_shouldReturnEmpty() {
        // Arrange
        Long id = 1L;
        when(lightBulbTable.getItem(any(Key.class))).thenReturn(null);
        
        // Act
        Optional<LightBulb> result = repository.findById(id);
        
        // Assert
        assertFalse(result.isPresent());
        verify(lightBulbTable).getItem(any(Key.class));
    }

    @Test
    @DisplayName("Verifies findById method returns empty when it is called with null id")
    void findById_whenIdIsNull_shouldReturnEmpty() {
        // Act
        Optional<LightBulb> result = repository.findById(null);
        
        // Assert
        assertFalse(result.isPresent());
        verifyNoInteractions(lightBulbTable);
    }

    @Test
    @DisplayName("Verifies findAll method returns all bulbs")
    void findAll_shouldReturnAllBulbs() {
        // Arrange - simulate what would be returned from DynamoDB scan
        LightBulb existingBulb1 = new LightBulb();
        existingBulb1.setName("Existing Bulb 1");
        existingBulb1.setType("LED");
        existingBulb1.setWattage(10);
        
        LightBulb existingBulb2 = new LightBulb();
        existingBulb2.setName("Existing Bulb 2");
        existingBulb2.setType("CFL");
        existingBulb2.setWattage(15);
        
        // Mock the DynamoDB scan operation - simulating what's already in the database
        when(lightBulbTable.scan()).thenReturn(pageIterable);
        when(pageIterable.items()).thenReturn(sdkIterable);
        when(sdkIterable.stream()).thenReturn(Stream.of(existingBulb1, existingBulb2));
        
        // Act - call findAll() which should scan and return all existing bulbs
        List<LightBulb> result = repository.findAll();
        
        // Assert - verify that all bulbs from the database are returned
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(existingBulb1));
        assertTrue(result.contains(existingBulb2));
        
        // Verify that scan was called (the core business logic)
        verify(lightBulbTable).scan();
    }

    @Test
    @DisplayName("Verifies findAll method returns empty list when database is empty")
    void findAll_whenDatabaseEmpty_shouldReturnEmptyList() {
        // Arrange - simulate empty database
        when(lightBulbTable.scan()).thenReturn(pageIterable);
        when(pageIterable.items()).thenReturn(sdkIterable);
        when(sdkIterable.stream()).thenReturn(Stream.empty());
        
        // Act
        List<LightBulb> result = repository.findAll();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        
        // Verify that scan was still called
        verify(lightBulbTable).scan();
    }

    @Test
    @DisplayName("Verifies deleteById method deletes bulb")
    void deleteById_shouldDeleteBulb() {
        // Arrange
        Long id = 1L;
        
        // Act
        repository.deleteById(id);
        
        // Assert
        verify(lightBulbTable).deleteItem(any(Key.class));
    }
}
