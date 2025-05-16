package com.example.proyectosanmiguel.service;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Profile("prod") // Solo se activa en el perfil de producción
public class RealS3Service implements S3StorageService {

    private final S3Template s3Template;
    private final String bucket;

    public RealS3Service(S3Template s3Template,
                         @Value("${spring.cloud.aws.s3.bucket}") String bucket) {
        this.s3Template = s3Template;
        this.bucket = bucket;
    }


    @Override
    public String upload(MultipartFile file, String nombreDestino) throws IOException {
        s3Template.upload(bucket, nombreDestino, file.getInputStream());
        return nombreDestino;
    }
}
