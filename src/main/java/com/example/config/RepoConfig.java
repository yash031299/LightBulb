package com.example.config;

import com.example.repo.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;

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
        return new S3LightBulbRepository(S3Client.create());
    }

}
