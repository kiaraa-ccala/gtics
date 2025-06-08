package com.example.proyectosanmiguel.controller;


import com.example.proyectosanmiguel.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/reportes")
public class ReportesTestinfController {


    @Autowired
    private ReporteService reporteService;


    @GetMapping("/sanmiguel")
    public ResponseEntity<byte[]> generarReporteSanMiguel() {
        try {
            System.out.println("Generando reporte San Miguel");
            // Llamada correcta al metodo
            List<String> datos = List.of("1", "2", "3");
            byte[] pdf = reporteService.generarReporteSanMiguel(datos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition
                    .inline()
                    .filename("reporte_sanmiguel.pdf")
                    .build());

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);

        } catch (Exception e) {
            System.err.println("Error al generar el reporte San Miguel: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


        }
    }
}