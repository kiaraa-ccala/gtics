package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.dto.EventoHorarioDto;
import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
        model.addAttribute("reporte", reporteRepository.findById(id).orElseThrow());
        List<Comentario> comentarios = comentarioRepository.findByReporteIdReporteOrderByFechaHoraAsc(id);
        model.addAttribute("comentarios", comentarios);
        return "Admin/admin_incidente";
    }

    @PostMapping("/admin/guardarAccion")
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
                    Foto nuevaFoto = new Foto();
                    nuevaFoto.setNombreFoto(foto.getOriginalFilename());
                    nuevaFoto.setFoto(foto.getBytes());
                    nuevaFoto.setUrlFoto("/uploads/" + foto.getOriginalFilename()); // opcional
                    fotoRepository.save(nuevaFoto);

                    // Asociar la foto al reporte
                    reporte.setFoto(nuevaFoto);
                    reporteRepository.save(reporte);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                attr.addFlashAttribute("error", "Debe subir una foto.");
                return "Admin/admin_incidente";
            }

            attr.addFlashAttribute("msg", "Acción registrada correctamente.");
        } else {
            attr.addFlashAttribute("error", "Reporte no encontrado.");
        }

        return "Admin/admin_incidente";
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