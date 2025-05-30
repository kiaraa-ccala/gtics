package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.service.AmazonS3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.io.IOException;

@Controller
@RequestMapping("/s3test")
public class S3TestController {

    @Autowired
    private AmazonS3Service amazonS3Service;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String url = amazonS3Service.subirArchivo(file);
            return ResponseEntity.ok(url);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir archivo: " + e.getMessage());
        }
    }

    }
