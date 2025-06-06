package com.example.proyectosanmiguel.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class AmazonS3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    public AmazonS3Service(S3Client s3Client, S3Presigner s3Presigner,
                           @Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
    }

    public String subirArchivo(MultipartFile archivo, String prefijo) throws IOException {
        // Validar tipo MIME
        String tipo = archivo.getContentType();
        List<String> permitidos = List.of("image/jpeg", "image/png", "application/pdf");
        if (tipo == null || !permitidos.contains(tipo)) {
            System.out.println("!! Tipo de archivo no permitido");
            throw new IllegalArgumentException("!! Tipo de archivo no permitido");
        }

        // Generar nombre seguro
        String nombreUnico = generarNombreUnico(archivo.getOriginalFilename());
        String key = (prefijo != null ? prefijo + "/" : "") + nombreUnico;
        //el key es el nombre del archivo en S3, incluyendo el prefijo si se proporciona
        //el prefijo es el nombre de la carpeta en S3, si no se proporciona, el archivo se guarda en la raíz del bucket
        // Generar solicitud PUT

        // Crear solicitud PUT
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(tipo)
                .acl("private")  // NO PUBLICAR el archivo
                .build();

        // Subir a S3
        s3Client.putObject(putRequest, RequestBody.fromInputStream(archivo.getInputStream(), archivo.getSize()));

        return key; // Retorna el key para almacenarlo o usarlo con URL firmada
    }

    public String generarUrlFirmada(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                builder -> builder.signatureDuration(Duration.ofMinutes(10))
                        .getObjectRequest(getObjectRequest)
        );

        return presignedRequest.url().toString();
    }


    private String generarNombreUnico(String originalFilename) {
        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            extension = originalFilename.substring(i);
        }
        return UUID.randomUUID() + extension;
    }

}
