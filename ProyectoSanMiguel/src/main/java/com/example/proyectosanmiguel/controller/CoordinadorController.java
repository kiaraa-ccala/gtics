package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.entity.Reporte;
import com.example.proyectosanmiguel.repository.ReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/coord")
public class CoordinadorController {

    @Autowired
    private ReporteRepository reporteRepository;

    @GetMapping("/reportes")
    public String verReportes(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Reporte> paginaReportes = reporteRepository.findAll(PageRequest.of(page, 5));

        model.addAttribute("reportes", paginaReportes.getContent());
        model.addAttribute("totalPaginas", paginaReportes.getTotalPages());
        model.addAttribute("paginaActual", page);

        return "Coordinador/coordinador_ver_reportes";
    }
}
