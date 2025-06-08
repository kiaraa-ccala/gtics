package com.example.proyectosanmiguel.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;


@Configuration
public class S3Config {

    private static final Logger logger = LoggerFactory.getLogger(S3Config.class);

    @Value("${aws.s3.region}")
    private String awsRegion;

    @Value("${aws.accessKeyId:}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey:}")
    private String secretAccessKey;


    private AwsCredentialsProvider resolveCredentials() {
        if (!accessKeyId.isEmpty() && !secretAccessKey.isEmpty()) {
            logger.info("Usando credenciales estáticas AWS");
            System.out.println("Usando credenciales estáticas AWS");
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
        } else {
            logger.info("Usando DefaultCredentialsProvider AWS");
            System.out.println("Usando DefaultCredentialsProvider AWS");
            return DefaultCredentialsProvider.create();
        }
    }


    // Crea el cliente S3 y el presigner de S3
    // El cliente S3 se utiliza para interactuar con el servicio S3 de AWS
    // El presigner de S3 se utiliza para firmar las URLs de acceso a los objetos de S3
    // Ambos utilizan las credenciales y la región configuradas en el archivo de propiedades
    // Si las credenciales están vacías, se utiliza el DefaultCredentialsProvider de AWS
    // Si las credenciales no están vacías, se utilizan las credenciales estáticas
    // Si ocurre un error al crear el cliente o el presigner, se captura la excepción, se registra el error y se lanza una RuntimeException
    // La RuntimeException se propaga al nivel superior, donde se manejará de manera adecuada

    //El s3presigner se utiliza para generar URLs firmadas que permiten el acceso temporal a los objetos en S3
    //Esto es útil para proporcionar acceso a los objetos de S3 sin necesidad de compartir las credenciales de acceso de AWS
    //Las URLs firmadas tienen una duración limitada y se pueden configurar para permitir el acceso de solo lectura o de lectura y escritura

    //La subida de archivos a S3 se realiza mediante el cliente S3, que permite cargar objetos en un bucket específico
    //Los objetos en S3 se identifican por su nombre de clave, que incluye el prefijo de la carpeta y el nombre del archivo
    //El nombre de clave se genera de manera aleatoria para evitar colisiones y se almacena en la base de datos para su posterior uso


    @Bean
    public S3Client s3Client() {
        try {
            return S3Client.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(resolveCredentials())
                    .build();
        } catch (Exception e) {
            logger.error(" Error al crear el cliente S3: {}", e.getMessage(), e);
            System.out.println("Error al crear el cliente S3: " + e.getMessage());
            throw new RuntimeException("No se pudo inicializar el cliente S3", e);
        }
    }

    @Bean
    public S3Presigner s3Presigner() {
        try {
            return S3Presigner.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(resolveCredentials())
                    .build();
        } catch (Exception e) {
            logger.error("Error al crear el S3Presigner: {}", e.getMessage(), e);
            System.out.println("Error al crear el S3Presigner: " + e.getMessage());
            throw new RuntimeException("No se pudo inicializar el S3Presigner", e);
        }
    }
}

