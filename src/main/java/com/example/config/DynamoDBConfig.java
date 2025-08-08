package com.example.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;


@Configuration
@Profile("dynamodb")
public class DynamoDBConfig {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDBConfig.class);

    @Value("${lightbulb.table.name:LightBulb}")
    private String tableName;

    private final DynamoDbClient dynamoDbClient;

    public DynamoDBConfig(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @PostConstruct
    public void createTableIfNotExists() {
        try {
            // Check if table exists
            dynamoDbClient.describeTable(builder -> builder.tableName(tableName));
            logger.info("DynamoDB table {} already exists", tableName);
        } catch (ResourceNotFoundException e) {
            // Table doesn't exist, create it
            logger.info("Creating DynamoDB table: {}", tableName);
            
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName(tableName)
                    .keySchema(KeySchemaElement.builder()
                            .attributeName("id")
                            .keyType(KeyType.HASH)
                            .build())
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName("id")
                            .attributeType(ScalarAttributeType.N)
                            .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build();

            try {
                dynamoDbClient.createTable(createTableRequest);
                logger.info("Successfully created DynamoDB table: {}", tableName);
                
                // Wait for table to be active
                dynamoDbClient.waiter().waitUntilTableExists(builder -> builder.tableName(tableName).build());
                logger.info("DynamoDB table {} is now active", tableName);
                
            } catch (Exception ex) {
                logger.error("Error creating DynamoDB table: {}", ex.getMessage(), ex);
                throw new RuntimeException("Failed to create DynamoDB table", ex);
            }
        } catch (Exception e) {
            logger.error("Error checking/creating DynamoDB table: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize DynamoDB table", e);
        }
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient() {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}
