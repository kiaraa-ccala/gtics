package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.service.S3StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/s3")
public class NeoS3 {
    private final S3StorageService almacenamientoService;

    /**
     * Constructor de la clase NeoS3.
     *
     * @param almacenamientoService Servicio de almacenamiento S3.
     */

    public NeoS3(S3StorageService almacenamientoService) {
        this.almacenamientoService = almacenamientoService;
    }
    @GetMapping("/listar")
    public ResponseEntity<String> listar() {
        try {
            String resultado = "Hola";
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar archivos: " + e.getMessage());
        }
    }


    @PostMapping("/subir")
    public ResponseEntity<String> subir(@RequestParam MultipartFile file) {
        try {
            System.out.println("Subiendo archivo: " + file.getOriginalFilename());
            String nombre = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String resultado = almacenamientoService.upload(file, nombre);
            return ResponseEntity.ok("Archivo guardado: " + resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir archivo: " + e.getMessage());
        }
    }
}
