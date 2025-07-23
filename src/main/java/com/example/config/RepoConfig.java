package com.example.config;

import com.example.repo.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class RepoConfig {

    @Bean
    public LightBulbRepository lightBulbRepository() {
        String env = System.getenv("AWS_LAMBDA_FUNCTION_NAME");
        if (env != null) {
            return new S3LightBulbRepository(S3Client.create());
        } else {
            return new LocalLightBulbRepository();
        }
    }
}
