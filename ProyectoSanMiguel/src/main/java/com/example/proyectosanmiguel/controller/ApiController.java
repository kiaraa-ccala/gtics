package com.example.proyectosanmiguel.controller;


import com.example.proyectosanmiguel.service.DNIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;


@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private DNIService dniService;

    // Endpoint para devolver solo los nombres y apellidos en formato JSON
    @GetMapping("/dni/nombres/{dni}")
    public ResponseEntity<Map<String, String>> obtenerNombresDni(@PathVariable String dni) {
        Map<String, String> datos = dniService.obtenerNombresDni(dni);

        if (datos.containsKey("error")) {
            return ResponseEntity.badRequest().body(datos);
        }

        return ResponseEntity.ok(datos);
    }

    // Endpoint opcional para devolver el JSON crudo como String (si deseas)
    @GetMapping("/dni/raw/{dni}")
    public ResponseEntity<String> obtenerDatosDniCrudo(@PathVariable String dni) {
        String json = dniService.obtenerDatosDni(dni);
        if (json.contains("error")) {
            return ResponseEntity.badRequest().body(json);
        }
        return ResponseEntity.ok(json);
    }

}
