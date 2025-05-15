package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.ComplejoRepository;
import com.example.proyectosanmiguel.repository.FotoRepository;
import com.example.proyectosanmiguel.repository.ReporteRepository;
import com.example.proyectosanmiguel.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/coord")
public class CoordinadorController {

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ComplejoRepository complejoDeportivoRepository;

    @Autowired
    private FotoRepository fotoRepository;

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

    @GetMapping("/reportes/pagina")
    public String obtenerPaginaParcial(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 4, Sort.by("fechaRecepcion").descending());
        Page<Reporte> pagina = reporteRepository.findAll(pageable);

        int totalPaginas = pagina.getTotalPages();
        int startPage = Math.max(0, page - 1);
        int endPage = Math.min(totalPaginas - 1, page + 1);

        model.addAttribute("reportes", pagina.getContent());
        model.addAttribute("paginaActual", page);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "Coordinador/coordinador_ver_reportes :: contenidoReportes";
    }


    @GetMapping("/reportes/mostrar")
    public String mostrarFormulario(Model model) {
        List<Reporte> ultimosReportes = reporteRepository.findTop4ByOrderByFechaRecepcionDesc();
        model.addAttribute("ultimosReportes", ultimosReportes);
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
                               @RequestParam(value = "imagenSituacion", required = false) MultipartFile imagenSituacion) throws IOException {

        Reporte nuevoReporte = new Reporte();
        nuevoReporte.setTipoReporte(tipoReporte);
        nuevoReporte.setFechaRecepcion(fechaRecepcion);
        nuevoReporte.setEstado(estado);
        nuevoReporte.setAsunto(asunto);
        nuevoReporte.setDescripcion(descripcion);
        nuevoReporte.setRespuesta("esperando respuesta...");
        nuevoReporte.setReserva(null);
        Horario horario = new Horario();
        horario.setIdHorario(idHorario);
        nuevoReporte.setHorario(horario);

        if (imagenSituacion != null && !imagenSituacion.isEmpty()) {
            Foto foto = new Foto();
            foto.setNombreFoto(imagenSituacion.getOriginalFilename());
            foto.setFoto(imagenSituacion.getBytes());
            foto.setUrlFoto("/uploads/" + imagenSituacion.getOriginalFilename()); // opcional, puede cambiar
            nuevoReporte.setFoto(foto);
        } else {
            nuevoReporte.setFoto(null);
        }

        reporteRepository.save(nuevoReporte);
        return "redirect:/coord/reportes/mostrar";
    }

    @GetMapping("/inicio")
    public String vistaInicio(Model model) {
        // 1. Los 7 reportes más recientes
        List<Reporte> ultimosReportes = reporteRepository.findTop7ByOrderByFechaRecepcionDesc();

        // 2. Total de reportes
        long cantidadReportes = reporteRepository.count();

        // 3. Reportes cerrados
        long cantidadCerrados = reporteRepository.countByEstado("Cerrado");

        //4. Foto de perfil
        Usuario coordinador = usuarioRepository.findFirstByRol_IdRol(3);

        //5. Complejo asignado
        ComplejoDeportivo complejo = complejoDeportivoRepository.findFirstBySector(coordinador.getSector());

        // Agregar al modelo
        model.addAttribute("ultimosReportes", ultimosReportes);
        model.addAttribute("cantidadReportes", cantidadReportes);
        model.addAttribute("cantidadCerrados", cantidadCerrados);
        model.addAttribute("usuario", coordinador);
        model.addAttribute("complejo", complejo);

        return "Coordinador/coordinador_inicio";
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(Model model) {
        Usuario coordinador = usuarioRepository.findFirstByRol_IdRol(3);
        model.addAttribute("usuario", coordinador);
        return "Coordinador/coordinador_perfil";
    }

    @GetMapping("/reportes/detalle/{id}")
    public String detalleReporte(@PathVariable("id") Integer id, Model model) {
        Optional<Reporte> optionalReporte = reporteRepository.findById(id);

        if (optionalReporte.isPresent()) {
            Reporte reporte = optionalReporte.get();
            model.addAttribute("reporte", reporte);
            return "Coordinador/coordinador_reporte_recibido";
        } else {
            return "redirect:/coord/reportes/ver"; // fallback si no se encuentra
        }
    }

    @GetMapping("/reporte/imagen/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> mostrarImagen(@PathVariable("id") Integer id) {
        Optional<Foto> optionalFoto = fotoRepository.findById(id);

        if (optionalFoto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Foto foto = optionalFoto.get();

        // Validar que sea .png
        if (foto.getNombreFoto() == null || !foto.getNombreFoto().toLowerCase().endsWith(".png")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(foto.getFoto(), headers, HttpStatus.OK);
    }


}
