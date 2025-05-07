package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.dto.EventoHorarioDto;
import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private InstanciaServicioRepository instanciaServicioRepository;

    @Autowired
    private ComplejoRepository complejoRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private EvidenciaRepository evidenciaRepository;

    @Autowired
    private FotoRepository fotoRepository;

    @ResponseBody
    @GetMapping("/horarios")
    public List<EventoHorarioDto> listarHorarios(
            @RequestParam(required = false) Integer idComplejo,
            @RequestParam(required = false) Integer idCoordinador) {

        return horarioRepository.listarHorariosPorFiltro(idComplejo, idCoordinador);
    }


    // ========== Incidencias ==========
    @GetMapping("/reportes")
    public String listarReportes(Model model) {
        model.addAttribute("listaReportes", reporteRepository.findAll());
        return "Admin/admin_reporte_incidencia";
    }

    @GetMapping("/reportes/detalle/{id}")
    public String detalleReporte(@PathVariable Integer id, Model model) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));

        List<Comentario> comentarios = comentarioRepository.findByReporteIdReporteOrderByFechaHoraAsc(id);
        Map<Integer, List<Evidencia>> evidenciasPorComentario = new HashMap<>();

        for (Comentario c : comentarios) {
            List<Evidencia> evidencias = evidenciaRepository.findByComentarioIdComentario(c.getIdComentario());
            evidenciasPorComentario.put(c.getIdComentario(), evidencias);
        }

        model.addAttribute("reporte", reporte);
        model.addAttribute("comentarios", comentarios);
        model.addAttribute("evidenciasPorComentario", evidenciasPorComentario);

        return "Admin/admin_incidente";
    }

    @PostMapping("/guardarAccion")
    public String guardarAccion(@RequestParam("idReporte") Integer idReporte,
                                @RequestParam("contenido") String contenido,
                                @RequestParam("archivos") List<MultipartFile> archivos,
                                @RequestParam("foto") MultipartFile foto,
                                RedirectAttributes attr) {

        Optional<Reporte> optReporte = reporteRepository.findById(idReporte);

        if (optReporte.isPresent()) {

            Reporte reporte = optReporte.get();

            // Guardar comentario
            Comentario comentario = new Comentario();
            comentario.setFechaHora(LocalDateTime.now());
            comentario.setContenido(contenido);
            comentario.setReporte(reporte);
            comentarioRepository.save(comentario);

            // Guardar evidencias
            for (MultipartFile archivo : archivos) {
                if (!archivo.isEmpty()) {
                    try {
                        Evidencia evidencia = new Evidencia();
                        evidencia.setNombreArchivo(archivo.getOriginalFilename());
                        evidencia.setArchivo(archivo.getBytes());
                        evidencia.setUrlArchivo("/uploads/" + archivo.getOriginalFilename()); // opcional
                        evidencia.setComentario(comentario);
                        evidenciaRepository.save(evidencia);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Guardar foto obligatoria
            if (!foto.isEmpty()) {
                try {

                    Evidencia evidencia = new Evidencia();
                    evidencia.setNombreArchivo(foto.getOriginalFilename());
                    evidencia.setArchivo(foto.getBytes());
                    evidencia.setUrlArchivo("/uploads/" + foto.getOriginalFilename()); // opcional
                    evidencia.setComentario(comentario);
                    evidenciaRepository.save(evidencia);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                attr.addFlashAttribute("error", "Debe subir una foto.");
                return "redirect:/admin/reportes";
            }

            attr.addFlashAttribute("msg", "Acción registrada correctamente.");
        } else {
            attr.addFlashAttribute("error", "Reporte no encontrado.");
        }

        return "redirect:/admin/reportes";
    }

    @GetMapping("/ver-evidencia/{id}")
    public ResponseEntity<byte[]> verEvidencia(@PathVariable Integer id) {
        try {
            Optional<Evidencia> opt = evidenciaRepository.findById(id);
            if (opt.isPresent()) {
                Evidencia evidencia = opt.get();

                String nombre = evidencia.getNombreArchivo().toLowerCase();
                String extension = nombre.substring(nombre.lastIndexOf('.') + 1);
                String contentType = switch (extension) {
                    case "png" -> "image/png";
                    case "jpg", "jpeg" -> "image/jpeg";
                    case "gif" -> "image/gif";
                    default -> "application/octet-stream";
                };


                return ResponseEntity.ok()
                        .header("Content-Type", contentType)
                        .body(evidencia.getArchivo());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.err.println("Error al servir evidencia ID: " + id);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/descargar-evidencia/{id}")
    public ResponseEntity<byte[]> descargarEvidencia(@PathVariable Integer id) {
        return evidenciaRepository.findById(id)
                .map(e -> ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + e.getNombreArchivo() + "\"")
                        .body(e.getArchivo()))
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== Monitoreo ==========
    @GetMapping("/servicios/monitoreo")
    public String monitoreoServicios(Model model) {
        List<InstanciaServicio> lista = instanciaServicioRepository.findAll();
        model.addAttribute("instancias", lista);
        return "Admin/admin_mantenimiento_modal";

    }

    // ========== TESTEO ==========
    @GetMapping("/testing")
    public String testing() {
        return "Admin/admin_agregrar_horarios";

    }
    @GetMapping("/agenda")
    public String vistaAgenda(Model model) {
        List<Usuario> coordinadores = usuarioRepository.findAllCoordinadores();
        List<ComplejoDeportivo> complejos = complejoRepository.findAll();

        model.addAttribute("coordinadores", coordinadores);
        model.addAttribute("complejos", complejos);

        return "Admin/admin_agenda";
    }
    @DeleteMapping("/agenda/eliminarHorario/{id}")
    public ResponseEntity<String> eliminarHorario(@PathVariable Integer id) {
        if (horarioRepository.existsById(id)) {  // verifica exist
            horarioRepository.deleteById(id);  // eloknina el horario
            return ResponseEntity.ok("Horario eliminado correctamente");
        } else {
            return ResponseEntity.status(404).body("Horario no encontrado");
        }
    }

    @GetMapping("/agregarHorarios")
    public String vistaAgregarHorarios(Model model) {
        List<Usuario> coordinadores = usuarioRepository.findAllCoordinadores();
        List<ComplejoDeportivo> complejos = complejoRepository.findAll();

        model.addAttribute("coordinadores", coordinadores);
        model.addAttribute("complejos", complejos);

        return "Admin/admin_agregrar_horarios";
    }


    // ========== Guardar Nuevo Complejo ==========
    @PostMapping("/servicios/guardarComplejo")
    @ResponseBody
    public String guardarNuevoComplejo(
            @RequestParam("nombreComplejo") String nombreComplejo,
            @RequestParam("direccionComplejo") String direccionComplejo,
            @RequestParam("numSoporte") String numSoporte,
            @RequestParam("serviciosSeleccionados") String serviciosSeleccionadosJson,
            @RequestParam(value = "cantidadLosa", required = false) String cantidadLosaStr,
            @RequestParam(value = "cantidadGrass", required = false) String cantidadGrassStr,
            @RequestParam("latitud") Double latitud,
            @RequestParam("longitud") Double longitud,
            @RequestParam(value = "cantidadCarriles", required = false) String cantidadCarrilesStr
    ) {
        ComplejoDeportivo nuevoComplejo = new ComplejoDeportivo();
        nuevoComplejo.setNombre(nombreComplejo);
        nuevoComplejo.setDireccion(direccionComplejo);
        nuevoComplejo.setNumeroSoporte(numSoporte);
        nuevoComplejo.setLatitud(latitud);
        nuevoComplejo.setLongitud(longitud);

        Sector sector = sectorRepository.findById(1).orElseThrow(); // Busca un sector por defecto
        nuevoComplejo.setSector(sector);


        complejoRepository.save(nuevoComplejo);

        List<String> servicios = new Gson().fromJson(serviciosSeleccionadosJson, new TypeToken<List<String>>() {}.getType());

        Integer cantidadLosa = (cantidadLosaStr != null && !cantidadLosaStr.isEmpty()) ? Integer.parseInt(cantidadLosaStr) : null;
        Integer cantidadGrass = (cantidadGrassStr != null && !cantidadGrassStr.isEmpty()) ? Integer.parseInt(cantidadGrassStr) : null;
        Integer cantidadCarriles = (cantidadCarrilesStr != null && !cantidadCarrilesStr.isEmpty()) ? Integer.parseInt(cantidadCarrilesStr) : null;

        for (String nombreServicio : servicios) {
            Servicio servicioEncontrado = servicioRepository.findByNombre(nombreServicio);
            if (servicioEncontrado != null) {
                InstanciaServicio instancia = new InstanciaServicio();
                instancia.setComplejoDeportivo(nuevoComplejo);
                instancia.setServicio(servicioEncontrado);
                instancia.setNombre(nombreServicio);

                if ("Cancha de losa".equals(nombreServicio) && cantidadLosa != null) {
                    instancia.setCapacidadMaxima(String.valueOf(cantidadLosa));
                } else if ("Cancha de grass".equals(nombreServicio) && cantidadGrass != null) {
                    instancia.setCapacidadMaxima(String.valueOf(cantidadGrass));
                } else if ("Piscina".equals(nombreServicio) && cantidadCarriles != null) {
                    instancia.setCapacidadMaxima(String.valueOf(cantidadCarriles));
                } else {
                    instancia.setCapacidadMaxima("1");
                }


                instancia.setModoAcceso("Normal");
                instanciaServicioRepository.save(instancia);
            }
        }
        return "ok";
    }
}