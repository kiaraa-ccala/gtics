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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/s3test")
public class S3TestController {

    @Autowired
    private AmazonS3Service amazonS3Service;

    @GetMapping
    public String mostrarFormularioPrueba() {
        return "Admin/admin_s3test";
    }

    //listar los archivos de un bucket como json
    @GetMapping("/listar")
    public ResponseEntity<Map<String, Object>> listarArchivos() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("archivos", amazonS3Service.listarArchivos(null));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Subir un archivo a S3 y retornar un ok
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        System.out.println("=== DEBUG UPLOAD ===");
        System.out.println("Método llamado correctamente");
        System.out.println("Archivo recibido: " + (file != null ? file.getOriginalFilename() : "NULL"));
        System.out.println("Tamaño: " + (file != null ? file.getSize() : "N/A"));
        System.out.println("Content Type: " + (file != null ? file.getContentType() : "N/A"));

        Map<String, String> response = new HashMap<>();

        // Validaciones básicas
        if (file == null) {
            System.out.println("ERROR: Archivo es null");
            response.put("status", "error");
            response.put("message", "No se recibió ningún archivo");
            return ResponseEntity.badRequest().body(response);
        }

        if (file.isEmpty()) {
            System.out.println("ERROR: Archivo está vacío");
            response.put("status", "error");
            response.put("message", "El archivo está vacío");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String url = amazonS3Service.subirArchivo(file, null);
            System.out.println("Archivo subido exitosamente: " + url);
            response.put("status", "success");
            response.put("url", url);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            System.out.println("ERROR IOException: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error al subir archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            System.out.println("ERROR Exception: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }




    /*
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String url = amazonS3Service.subirArchivo(file);
            return ResponseEntity.ok(url);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir archivo: " + e.getMessage());
        }
    }
    */


    /*
    @PostMapping("/upload-carpeta")
    public ResponseEntity<Map<String, String>> uploadFileToCarpeta(
            @RequestParam("file") MultipartFile file,
            @RequestParam("carpeta") String carpeta,
            @RequestParam(value = "id", required = false) Integer id) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            String url = amazonS3Service.subirArchivo(file, carpeta + "/", id);
            response.put("status", "success");
            response.put("url", url);
            response.put("message", "Archivo subido correctamente a la carpeta: " + carpeta);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("status", "error");
            response.put("message", "Error al subir archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    */
    /*
    @PostMapping("/test-carpetas")
    public String testMultiplesCarpetas(
            @RequestParam("fileComplejo") MultipartFile fileComplejo,
            @RequestParam("fileReporte") MultipartFile fileReporte,
            @RequestParam("fileEvidencia") MultipartFile fileEvidencia,
            RedirectAttributes redirectAttributes) {
        
        Map<String, String> resultados = new HashMap<>();
        
        try {
            if (!fileComplejo.isEmpty()) {
                String urlComplejo = amazonS3Service.subirArchivo(fileComplejo, "complejos", 1);
                resultados.put("complejo", urlComplejo);
            }
            
            if (!fileReporte.isEmpty()) {
                String urlReporte = amazonS3Service.subirArchivo(fileReporte, "reportes", 1);
                resultados.put("reporte", urlReporte);
            }
            
            if (!fileEvidencia.isEmpty()) {
                String urlEvidencia = amazonS3Service.subirArchivo(fileEvidencia, "evidencias", 1);
                resultados.put("evidencia", urlEvidencia);
            }
            
            redirectAttributes.addFlashAttribute("resultados", resultados);
            redirectAttributes.addFlashAttribute("success", "Prueba completada con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error en la prueba: " + e.getMessage());
        }
        
        return "redirect:/admin/s3test";
    }
    */
}
