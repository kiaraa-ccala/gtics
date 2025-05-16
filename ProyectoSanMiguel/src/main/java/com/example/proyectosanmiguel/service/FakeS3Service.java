package com.example.proyectosanmiguel.service;


import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@Profile("dev")
public class FakeS3Service implements S3StorageService {


    @Override
    public String upload(MultipartFile file, String pathPrefix) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("El nombre del archivo es inválido.");
        }

        File dir = new File("uploads/" + pathPrefix);
        if (!dir.exists()) dir.mkdirs();

        String filename = System.currentTimeMillis() + "_" + originalFilename;
        File targetFile = new File(dir, filename);
        file.transferTo(targetFile);

        return targetFile.getAbsolutePath(); // Ruta local donde se guardó
    }

}
