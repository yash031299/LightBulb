package com.example.config;

import com.example.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;


@Configuration
public class RepoConfig {

    @Bean
    @Profile("local")
    public LightBulbRepository localRepo() {
        return new LocalLightBulbRepository();
    }

    @Bean
    @Profile("s3")
    public LightBulbRepository s3Repo() {
        S3ClientBuilder builder = S3Client.builder();
        // Add any S3 specific configurations here if needed
        return new S3LightBulbRepository(builder.build());
    }

    @Value("${lightbulb.table.name:LightBulb}")
    private String tableName;

    @Bean
    @Profile({"dynamodb", "default"})
    public LightBulbRepository dynamoDbRepo(DynamoDbClient dynamoDbClient) {
        return new DynamoDbLightBulbRepository(dynamoDbClient, tableName);
    }

    @Bean
    @Profile("dynamodb")
    public DynamoDbClient dynamoDbClient(@Value("${aws.region:ap-south-1}") String region) {
        return DynamoDbClient.builder()
                .region(Region.of(region))
                .build();
    }
}
