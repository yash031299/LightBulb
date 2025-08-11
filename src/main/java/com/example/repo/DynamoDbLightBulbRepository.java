package com.example.repo;

import com.example.model.LightBulb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.*;
import java.util.stream.Collectors;

public class DynamoDbLightBulbRepository implements LightBulbRepository {
    private static final Logger logger = LoggerFactory.getLogger(DynamoDbLightBulbRepository.class);

    private final DynamoDbEnhancedClient enhancedClient;

    private final DynamoDbTable<LightBulb> table;

    public DynamoDbLightBulbRepository(DynamoDbClient dynamoDb, 
                                     @Value("${lightbulb.table.name:LightBulb}") String tableName) {
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDb)
                .build();
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(LightBulb.class));
        
        logger.info("Initialized DynamoDB repository for table: {}", tableName);
    }

    @Override
    public List<LightBulb> findAll() {
        try {
            logger.debug("Fetching all light bulbs from DynamoDB");
            return table.scan()
                    .items()
                    .stream()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching all light bulbs", e);
            throw new RuntimeException("Failed to retrieve light bulbs: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<LightBulb> findById(Long id) {
        if (id == null) {
            logger.warn("Attempted to find light bulb with null ID");
            return Optional.empty();
        }
        
        try {
            logger.debug("Fetching light bulb with id: {}", id);
            LightBulb item = table.getItem(Key.builder().partitionValue(id).build());
            if (item == null) {
                logger.debug("No light bulb found with id: {}", id);
                return Optional.empty();
            }
            logger.debug("Successfully retrieved light bulb with id: {}", id);
            return Optional.of(item);
        } catch (Exception e) {
            String errorMsg = String.format("Error finding light bulb with id: %d. Error: %s", id, e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    @Override
    public LightBulb save(LightBulb bulb) {
        try {
            if (bulb.getId() == null) {
                bulb.setId(generateId());
                logger.debug("Generated new ID for light bulb: {}", bulb.getId());
            }
            
            logger.debug("Saving light bulb: {}", bulb);
            table.putItem(bulb);
            return bulb;
        } catch (Exception e) {
            logger.error("Error saving light bulb: " + bulb, e);
            throw new RuntimeException("Failed to save light bulb: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            logger.debug("Deleting light bulb with id: {}", id);
            table.deleteItem(Key.builder().partitionValue(id).build());
        } catch (Exception e) {
            logger.error("Error deleting light bulb with id: " + id, e);
            throw new RuntimeException("Failed to delete light bulb: " + e.getMessage(), e);
        }
    }


    protected Long generateId() {
       long timestamp = System.nanoTime();
        int random = new Random().nextInt(1000);
        return Math.abs((timestamp % 1000000000000L) * 1000 + random);
    }
}
