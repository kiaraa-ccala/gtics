package com.example.proyectosanmiguel.controller;


import com.example.proyectosanmiguel.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/reportes")
public class ReportesTestinfController {


    @Autowired
    private ReporteService reporteService;


    @GetMapping("/sanmiguel")
    public ResponseEntity<byte[]> generarReporteSanMiguel() {
        try {
            // Llamada correcta al metodo
            byte[] pdf = reporteService.generarReporteSanMiguel();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition
                    .inline()
                    .filename("reporte_sanmiguel.pdf")
                    .build());

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


        }
    }
}