package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.entity.Comentario;
import com.example.proyectosanmiguel.entity.Reporte;
import com.example.proyectosanmiguel.repository.ComentarioRepository;
import com.example.proyectosanmiguel.repository.ReporteRepository;
import com.example.proyectosanmiguel.repository.InstanciaServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private InstanciaServicioRepository instanciaServicioRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    // ========== Incidencias ==========
    @GetMapping("/reportes")
    public String listarReportes(Model model) {
        model.addAttribute("listaReportes", reporteRepository.findAll());
        return "admin/reportes/lista";
    }

    @GetMapping("/reportes/detalle/{id}")
    public String detalleReporte(@PathVariable Integer id, Model model) {
        model.addAttribute("reporte", reporteRepository.findById(id).orElseThrow());
        return "admin/reportes/detalle";
    }


}
