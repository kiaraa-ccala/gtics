package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.service.RealS3Service;
import com.example.proyectosanmiguel.service.S3StorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3StorageService s3Service;

    public S3Controller(S3StorageService s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam MultipartFile file,
                                         @RequestParam Integer userId,
                                         @RequestParam String rol) {
        try {
            // Genera la ruta lógica usando rol y userId
            String pathPrefix = "reportes/" + rol.toLowerCase() + "/" + userId + "/";

            // Llama al servicio con la nueva firma
            String key = s3Service.upload(file, pathPrefix);

            return ResponseEntity.ok("Archivo subido correctamente: " + key);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir el archivo: " + e.getMessage());
        }
    }

}
