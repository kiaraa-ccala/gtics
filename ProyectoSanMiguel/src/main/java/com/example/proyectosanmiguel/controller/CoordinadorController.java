package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.entity.Foto;
import com.example.proyectosanmiguel.entity.Horario;
import com.example.proyectosanmiguel.entity.Reporte;
import com.example.proyectosanmiguel.entity.Reserva;
import com.example.proyectosanmiguel.repository.ReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/coord")
public class CoordinadorController {

    @Autowired
    private ReporteRepository reporteRepository;

    @GetMapping("/reportes/ver")
    public String verReportes(@RequestParam(defaultValue = "0") int page, Model model) {
        int pageSize = 4;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("fechaRecepcion").descending());
        Page<Reporte> pagina = reporteRepository.findAll(pageable);

        int totalPaginas = pagina.getTotalPages();

        // Calcular el rango de páginas que se mostrarán (máximo 3)
        int startPage = Math.max(0, page - 1);
        int endPage = Math.min(totalPaginas - 1, page + 1);

        model.addAttribute("reportes", pagina.getContent());
        model.addAttribute("paginaActual", page);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "Coordinador/coordinador_ver_reportes";
    }

    @GetMapping("/reportes/mostrar")
    public String mostrarFormulario() {
        return "Coordinador/coordinador_seccion_reporte";
    }

    @PostMapping("/reportes/crear")
    public String crearReporte(@RequestParam("tipoReporte") String tipoReporte,
                               @RequestParam("fechaRecepcion") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaRecepcion,
                               @RequestParam("estado") String estado,
                               @RequestParam("asunto") String asunto,
                               @RequestParam("descripcion") String descripcion,
                               @RequestParam("respuesta") String respuesta,
                               @RequestParam(value = "idReserva", required = false) Integer idReserva,
                               @RequestParam("idHorario") Integer idHorario,
                               @RequestParam(value = "idFoto", required = false) Integer idFoto) {

        Reporte nuevoReporte = new Reporte();
        nuevoReporte.setTipoReporte(tipoReporte);
        nuevoReporte.setFechaRecepcion(fechaRecepcion);
        nuevoReporte.setEstado(estado);
        nuevoReporte.setAsunto(asunto);
        nuevoReporte.setDescripcion(descripcion);
        nuevoReporte.setRespuesta("esperando respuesta...");
        nuevoReporte.setReserva(null); // como se pidió
        Horario horario = new Horario();
        horario.setIdHorario(idHorario);
        nuevoReporte.setHorario(horario);
        nuevoReporte.setFoto(null);

        reporteRepository.save(nuevoReporte);

        return "redirect:/coord/reportes/mostrar";
    }
}
