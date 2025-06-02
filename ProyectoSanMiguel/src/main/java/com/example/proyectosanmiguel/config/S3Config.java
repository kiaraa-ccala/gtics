package com.example.proyectosanmiguel.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;


@Configuration
public class S3Config {

    private static final Logger logger = LoggerFactory.getLogger(S3Config.class);

    @Value("${aws.s3.region}")
    private String awsRegion;

    @Value("${aws.accessKeyId:}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey:}")
    private String secretAccessKey;

    @Bean
    public S3Client s3Client() {
        try {
            Region region = Region.of(awsRegion);

            var builder = S3Client.builder().region(region);

            if (!accessKeyId.isEmpty() && !secretAccessKey.isEmpty()) {
                builder.credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                        )
                );
            } else {
                builder.credentialsProvider(DefaultCredentialsProvider.create());
            }

            return builder.build();
        } catch (Exception e) {
            logger.error("Error al crear el cliente S3: {}", e.getMessage(), e);
            System.out.println("Error al crear el cliente S3: " + e.getMessage());
            throw new RuntimeException("No se pudo inicializar el cliente S3", e);
        }
    }
}

