package com.example.proyectosanmiguel.service;

import com.example.proyectosanmiguel.entity.Foto;
import com.example.proyectosanmiguel.repository.FotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class AmazonS3Service {

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Autowired
    private FotoRepository fotoRepository;

    public String subirArchivo(MultipartFile archivo, String prefijo) throws IOException {
        // Credenciales AWS
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        // Crear cliente S3
        S3Client s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
        // Generar nombre único para el archivo
        String nombreArchivo = (prefijo != null ? prefijo : "undefined/") + generarNombreUnico(archivo.getOriginalFilename());
        try {
            // Crear solicitud PUT
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(nombreArchivo)
                    .contentType(archivo.getContentType())
                    .acl("public-read")
                    .build();
            // Subir archivo
            s3Client.putObject(request, RequestBody.fromInputStream(archivo.getInputStream(), archivo.getSize()));
            // Devolver URL del archivo
            return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + nombreArchivo;
        } catch (Exception ex) {
            System.err.println(" Error al subir archivo a Amazon S3: " + ex.getMessage());
            ex.printStackTrace(); // para detalle completo del error

            // Fallback: guardar en base de datos
            Foto foto = new Foto();
            foto.setNombreFoto(archivo.getOriginalFilename());
            foto.setFoto(archivo.getBytes());
            foto.setUrlFoto("/fotos/" + archivo.getOriginalFilename());
            fotoRepository.save(foto);
            System.out.println("⚠️ Archivo guardado localmente en base de datos como fallback.");
            return "/fotos/" + archivo.getOriginalFilename();
        }
    }

    public String subirArchivo(MultipartFile archivo) throws IOException {
        return subirArchivo(archivo, null);
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
