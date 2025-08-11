package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;

@Configuration
@Profile("test")
public class TestConfig {

    private static final LocalStackContainer localStackContainer;
    
    static {
        localStackContainer = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.0"))
                .withServices(LocalStackContainer.Service.DYNAMODB);
        localStackContainer.start();
        
        // Set system properties for AWS SDK to use our localstack container
        System.setProperty("aws.region", localStackContainer.getRegion());
        System.setProperty("aws.accessKeyId", localStackContainer.getAccessKey());
        System.setProperty("aws.secretKey", localStackContainer.getSecretKey());
        System.setProperty("aws.dynamodb.endpoint", 
            localStackContainer.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
    }
    
    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .endpointOverride(localStackContainer.getEndpointOverride(LocalStackContainer.Service.DYNAMODB))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localStackContainer.getAccessKey(), localStackContainer.getSecretKey())))
                .region(Region.of(localStackContainer.getRegion()))
                .build();
    }
    
    @Bean(destroyMethod = "stop")
    public LocalStackContainer getLocalStackContainer() {
        return localStackContainer;
    }
}
