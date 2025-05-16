package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.dto.*;
import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
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
    private HorarioSemanalRepository horarioSemanalRepository;

    @Autowired
    private FotoRepository fotoRepository;

    @Autowired
    private MantenimientoRepository mantenimientoRepository;

    @ResponseBody
    @GetMapping("/horarios")
    public List<EventoHorarioDto> listarHorarios(
            @RequestParam(required = false) Integer idComplejo,
            @RequestParam(required = false) Integer idCoordinador) {

        return horarioRepository.listarHorariosPorFiltro(idComplejo, idCoordinador);
    }
    // ========== Agreagar Horarios ==========
    @GetMapping("/test/horarios/buscar")
    @ResponseBody
    public List<HorarioTurnoDto> buscarHorarios(
            @RequestParam Integer coordinadorId,
            @RequestParam String semana // ejemplo: 2025-W21
    ) {
        try {
            // Parseo manual del año y semana desde el texto
            String[] partes = semana.split("-W");
            int anio = Integer.parseInt(partes[0]);
            int semanaNum = Integer.parseInt(partes[1]);

            // Calcular el lunes de esa semana ISO
            LocalDate fechaInicio = LocalDate.of(anio, 1, 4)
                    .with(WeekFields.ISO.weekOfWeekBasedYear(), semanaNum)
                    .with(WeekFields.ISO.getFirstDayOfWeek()); // lunes
            LocalDate fechaFin = fechaInicio.plusDays(6); // domingo
            //lo que se hizo fue sacar los dias fecha de inicio fecha fin lun -dom
            //se usa optional, ya que se espera solo una semana, no varias semanas
            //La logica del HTML es que cuando seleccione la semana, verifique con AJAx SI
            //existe un horario semanal para esa semana, si existe, se cargan los turnos
            //si no existe, muestra vacio, pero en el front puedo cargar horarios y crear una semana nueva :D
            //asi esta vista me sirve para inclusive editar turnos y borrar turnos
            // Buscar si ya existe HorarioSemanal
            Optional<HorarioSemanal> existente = horarioSemanalRepository
                    .findByCoordinadorIdUsuarioAndFechaInicioAndFechaFin(coordinadorId, fechaInicio, fechaFin);
            if (existente.isEmpty()) {
                return List.of(); // No hay turnos para esa semana
            }
            //Observamos que si envia correctamente :,vvvvv probablemente el error sea el metodo (lo era) xD
            //se resolvioXD
            return horarioRepository.obtenerTurnosPorHorarioSemanal(existente.get().getIdHorarioSemanal());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @PostMapping("/agenda/guardarHorarios")
    @ResponseBody
    @Transactional  // Asegura que las operaciones de la base de datos sean atómicas
    public ResponseEntity<String> guardarHorarios(@RequestBody HorarioSemanalGuardarDto dto) {
        try {
            // 1. Calcular fechaInicio y fechaFin según semana
            String[] partes = dto.getSemana().split("-W");
            int anio = Integer.parseInt(partes[0]);
            int semana = Integer.parseInt(partes[1]);
            LocalDate fechaInicio = LocalDate.of(anio, 1, 4)
                    .with(WeekFields.ISO.weekOfWeekBasedYear(), semana)
                    .with(WeekFields.ISO.getFirstDayOfWeek());
            LocalDate fechaFin = fechaInicio.plusDays(6);
            System.out.println("Fecha inicio: " + fechaInicio);
            // 2. Buscar o crear HorarioSemanal
            HorarioSemanal semanal = horarioSemanalRepository
                    .findByCoordinadorIdUsuarioAndFechaInicioAndFechaFin(dto.getCoordinadorId(), fechaInicio, fechaFin)
                    .orElseGet(() -> {
                        HorarioSemanal nuevo = new HorarioSemanal();
                        nuevo.setIdCoordinador(dto.getCoordinadorId());
                        nuevo.setFechaInicio(fechaInicio);
                        nuevo.setFechaFin(fechaFin);
                        nuevo.setFechaCreacion(LocalDate.now());
                        nuevo.setIdAdministrador(1);  // Este ID puede cambiar según tu lógica
                        return horarioSemanalRepository.save(nuevo);
                    });
            System.out.println("Revisar si Horarios Eliminar es null: "+ dto.getHorariosEliminar());
            System.out.println("HorarioSemanal ID: " + semanal.getIdHorarioSemanal());
            // 3. Eliminar turnos por ID
            if (dto.getHorariosEliminar() != null) {
                for (var item : dto.getHorariosEliminar()) {
                    System.out.println("test1");
                    horarioRepository.deleteById(item.getId());
                    System.out.println("test2");
                }
            }
            System.out.println("Revisar si Horarios Guardar es null: "+ dto.getHorariosGuardar());
            //ya voy 3 horas aca sin poder resolverlo :c, si me retiro de gtics? aun hay tiempo :D
            //no eres tu profe brenda, soy yo

            // 4. Insertar turnos nuevos
            if (dto.getHorariosGuardar() != null) {
                for (HorarioDiarioTurnoGuardarDto turno : dto.getHorariosGuardar()) {
                    Horario nuevo = new Horario();
                    System.out.println(semanal.getIdHorarioSemanal());
                    nuevo.setIdHorarioSemanal(semanal.getIdHorarioSemanal());
                    System.out.println(turno.getIdComplejoDeportivo());
                    nuevo.setIdComplejoDeportivo(turno.getIdComplejoDeportivo());
                    System.out.println(turno.getDiaSemana());
                    nuevo.setFecha(mapearDiaSemanaAFecha(fechaInicio, turno.getDiaSemana()));
                    System.out.println(turno.getHoraInicio());
                    nuevo.setHoraIngreso(LocalTime.parse(turno.getHoraInicio()));
                    System.out.println(turno.getHoraFin());
                    nuevo.setHoraSalida(LocalTime.parse(turno.getHoraFin()));
                    horarioRepository.save(nuevo);
                }
            }
            return ResponseEntity.ok("Guardado correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar");
        }
    }

/*
    @PostMapping("/agenda/guardarHorarios")
    @ResponseBody
    @Transactional //Se usara Transactional ya que se realizan varias operaciones, (guardar y eliminar)
    public ResponseEntity<String> guardarHorarios(@RequestBody HorarioGuardarDto dto) {
        try {
            // 1. Calcular fechaInicio y fechaFin según semana
            String[] partes = dto.getSemana().split("-W");
            int anio = Integer.parseInt(partes[0]);
            int semana = Integer.parseInt(partes[1]);
            LocalDate fechaInicio = LocalDate.of(anio, 1, 4)
                    .with(WeekFields.ISO.weekOfWeekBasedYear(), semana)
                    .with(WeekFields.ISO.getFirstDayOfWeek());
            LocalDate fechaFin = fechaInicio.plusDays(6);

            // 2. Buscar o crear HorarioSemanal
            HorarioSemanal semanal = horarioSemanalRepository
                    .findByCoordinadorIdUsuarioAndFechaInicioAndFechaFin(dto.getCoordinadorId(), fechaInicio, fechaFin)
                    .orElseGet(() -> {
                        HorarioSemanal nuevo = new HorarioSemanal();
                        nuevo.setIdCoordinador(dto.getCoordinadorId());
                        nuevo.setFechaInicio(fechaInicio);
                        nuevo.setFechaFin(fechaFin);
                        nuevo.setFechaCreacion(LocalDate.now());
                        nuevo.setIdAdministrador(1); //no es de tanta importancia si es que tenemos solo un usuario admin
                        return horarioSemanalRepository.save(nuevo);
                    });

            // 3. Eliminar turnos por ID
            if (dto.getHorariosEliminar() != null) {
                for (var item : dto.getHorariosEliminar()) {
                    horarioRepository.deleteById(item.getIdHorario());
                }
            }

            // 4. Insertar turnos nuevos
            if (dto.getHorariosGuardar() != null) {
                for (HorarioTurnoDto turno : dto.getHorariosGuardar()) {
                    Horario nuevo = new Horario();
                    nuevo.setIdHorarioSemanal(semanal.getIdHorarioSemanal());
                    nuevo.setIdComplejoDeportivo(turno.getIdComplejoDeportivo());
                    nuevo.setFecha(mapearDiaSemanaAFecha(fechaInicio, turno.getDiaSemana()));
                    nuevo.setHoraIngreso(turno.getHoraInicio());
                    nuevo.setHoraSalida(turno.getHoraFin());
                    horarioRepository.save(nuevo);
                }
            }

            return ResponseEntity.ok("Guardado correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar");
        }
    }

*/

    // metodo para obtener dia de la semana (no es controlador)
    private String obtenerDiaNombre(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miercoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sabado";
            case SUNDAY -> "Domingo";
        };
    }
    private LocalDate mapearDiaSemanaAFecha(LocalDate fechaInicio, String diaSemana) {
        // Un mapa de días de la semana a números (lunes=1, martes=2, etc.)
        Map<String, Integer> diasSemana = Map.of(
                "Lunes", 0,
                "Martes", 1,
                "Miércoles", 2,
                "Jueves", 3,
                "Viernes", 4,
                "Sábado", 5,
                "Domingo", 6
        );

        // obtener el número del día de la semana basado en la cadena
        Integer diaNumero = diasSemana.get(diaSemana);

        // sumar el número de días a la fecha de inicio para obtener la fecha correcta
        if (diaNumero != null) {
            return fechaInicio.plusDays(diaNumero);
        } else {
            // Si el día no es válido, lanzar un error o manejar el caso
            throw new IllegalArgumentException("Día de la semana inválido: " + diaSemana);
        }
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
        List<ComplejoDeportivo> complejos = complejoRepository.findAll();
        model.addAttribute("complejos", complejos);
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

    //Me servira para cargar los complejos - coordinadores luego de selccionar ver datos en la agenda
    @GetMapping("/api/complejo/{id}")
    @ResponseBody
    public ResponseEntity<ComplejoAgendaAdminDto> getComplejoById(@PathVariable Integer id) {
        ComplejoAgendaAdminDto dto = complejoRepository.obtenerComplejoDto(id);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build(); //por si no existe el complejo - manejo el error en JS {no hay complejo xd} aunque tambien podria enviar null. XD
        } //depende de como se desee manejar u.u
    }

    @GetMapping("/api/coordinador/{id}")
    @ResponseBody
    public ResponseEntity<CoordinadorAgendaAdminDto> getCoordinadorById(@PathVariable Integer id) {
        CoordinadorAgendaAdminDto dto = usuarioRepository.obtenerCoordinadorDto(id);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
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
    //Fotos
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

    @PostMapping("/reporte/cambiar-estado")
    public String cambiarEstado(@RequestParam Integer idReporte,
                                @RequestParam String nuevoEstado,
                                RedirectAttributes redirectAttributes) {
        Optional<Reporte> optionalReporte = reporteRepository.findById(idReporte);
        if (optionalReporte.isPresent()) {
            Reporte reporte = optionalReporte.get();
            reporte.setEstado(nuevoEstado);
            reporteRepository.save(reporte);
            redirectAttributes.addFlashAttribute("success", "Estado actualizado correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se encontró el reporte.");
        }
        return "redirect:/admin/reportes/detalle/" + idReporte;
    }
    @PostMapping("/mantenimientos/guardar")
    public String guardarMantenimiento(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) String fechaFinStr,
            @RequestParam("horaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
            @RequestParam("horaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin,
            @RequestParam("idComplejo") Integer idComplejo,
            @RequestParam("descripcion") String descripcion,
            RedirectAttributes redirectAttributes
    ) {
        ComplejoDeportivo complejo = complejoRepository.findById(idComplejo).orElseThrow();

        Mantenimiento mantenimiento = new Mantenimiento();
        mantenimiento.setFechaInicio(fechaInicio);
            System.out.println(fechaInicio);

        LocalDate fechaFin;
        if (fechaFinStr == null || fechaFinStr.trim().isEmpty()) {
            fechaFin = fechaInicio;
        } else {
            fechaFin = LocalDate.parse(fechaFinStr);
        }
        System.out.println(fechaFin);
        mantenimiento.setFechaFin(fechaFin);
        mantenimiento.setHoraInicio(horaInicio);
        mantenimiento.setHoraFin(horaFin);
        mantenimiento.setComplejoDeportivo(complejo);

        mantenimientoRepository.save(mantenimiento);
        redirectAttributes.addFlashAttribute("exito", "¡Mantenimiento guardado correctamente!");
        return "redirect:/admin/servicios/monitoreo";
    }

}