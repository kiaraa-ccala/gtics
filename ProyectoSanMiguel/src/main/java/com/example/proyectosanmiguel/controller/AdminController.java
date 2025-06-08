package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.dto.*;
import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.*;
import com.example.proyectosanmiguel.service.EmailService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.format.TextStyle;
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

    @Autowired
    private EmailService emailService;

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
    @GetMapping("/api/mantenimientos/{idComplejo}")
    @ResponseBody
    public List<Map<String, Object>> obtenerMantenimientosPorComplejo(@PathVariable Integer idComplejo) {
        List<Mantenimiento> mantenimientos = mantenimientoRepository.findByComplejoDeportivoIdComplejoDeportivo(idComplejo);
        List<Map<String, Object>> eventos = new ArrayList<>();

        for (Mantenimiento m : mantenimientos) {
            Map<String, Object> evento = new HashMap<>();
            evento.put("id", m.getIdMantenimiento());
            evento.put("title", "MANTENIMIENTO");
            evento.put("start", m.getFechaInicio().toString() + "T" + m.getHoraInicio().toString());
            evento.put("end", m.getFechaFin().toString() + "T" + m.getHoraFin().toString());
            evento.put("type", "event-danger"); // para marcar visualmente
            evento.put("venue", m.getComplejoDeportivo().getNombre());
            evento.put("nombreComplejo", m.getComplejoDeportivo().getNombre());

            eventos.add(evento);
        }

        return eventos;
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

            // Lista de IDs de turnos que se eliminarán, para excluirlos de la validación
            List<Integer> idsEliminar = dto.getHorariosEliminar() != null
                    ? dto.getHorariosEliminar().stream()
                    .map(HorarioEliminarDto::getId)
                    .toList()
                    : Collections.emptyList();
            System.out.println("IDs a eliminar (excluir de validación): " + idsEliminar);



            // Validar solapamientos primero
            if (dto.getHorariosGuardar() != null) {
                List<HorarioDiarioTurnoGuardarDto> turnos = dto.getHorariosGuardar();

                for (int i = 0; i < turnos.size(); i++) {
                    HorarioDiarioTurnoGuardarDto t1 = turnos.get(i);
                    LocalDate fecha1 = mapearDiaSemanaAFecha(fechaInicio, t1.getDiaSemana());
                    LocalTime inicio1 = LocalTime.parse(t1.getHoraInicio());
                    LocalTime fin1 = LocalTime.parse(t1.getHoraFin());

                    if (!fin1.isAfter(inicio1)) {
                        return ResponseEntity.badRequest().body(
                                "La hora fin debe ser posterior a la hora inicio en el día " + t1.getDiaSemana());
                    }

                    // Validación contra la base de datos
                    boolean solapaBD = horarioRepository.existeSolapamiento(
                            dto.getCoordinadorId(),
                            fecha1,
                            inicio1,
                            fin1,
                            idsEliminar
                    );

                    if (solapaBD) {
                        String nombreDia = fecha1.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es"));
                        return ResponseEntity.badRequest().body(
                                "El horario del día " + nombreDia + " (" + t1.getHoraInicio() +
                                        " - " + t1.getHoraFin() + ") se solapa con otro horario ya registrado.");
                    }

                    // Validación contra los otros turnos nuevos del mismo payload
                    for (int j = i + 1; j < turnos.size(); j++) {
                        HorarioDiarioTurnoGuardarDto t2 = turnos.get(j);

                        if (t1.getDiaSemana().equals(t2.getDiaSemana())) {
                            LocalTime inicio2 = LocalTime.parse(t2.getHoraInicio());
                            LocalTime fin2 = LocalTime.parse(t2.getHoraFin());

                            if (inicio1.isBefore(fin2) && fin1.isAfter(inicio2)) {
                                String nombreDia = fecha1.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es"));
                                System.out.println("Turnos solapados: " + t1.getHoraInicio() + "-" + t1.getHoraFin() + " y " +
                                        t2.getHoraInicio() + "-" + t2.getHoraFin());
                                return ResponseEntity.badRequest().body(
                                        "Los turnos nuevos del día " + nombreDia + " se solapan entre sí: " +
                                                t1.getHoraInicio() + "-" + t1.getHoraFin() + " y " +
                                                t2.getHoraInicio() + "-" + t2.getHoraFin()
                                );
                            }
                        }
                    }
                }
            }


            // Eliminar turnos
            if (!idsEliminar.isEmpty()) {
                for (Integer id : idsEliminar) {
                    horarioRepository.deleteById(id);
                }
            }
            // Insertar nuevos turnos
            System.out.println("Revisar si Horarios Guardar es null: "+ dto.getHorariosGuardar());
            if (dto.getHorariosGuardar() != null) {
                for (HorarioDiarioTurnoGuardarDto turno : dto.getHorariosGuardar()) {
                    Horario nuevo = new Horario();
                    nuevo.setIdHorarioSemanal(semanal.getIdHorarioSemanal());
                    nuevo.setIdComplejoDeportivo(turno.getIdComplejoDeportivo());
                    nuevo.setFecha(mapearDiaSemanaAFecha(fechaInicio, turno.getDiaSemana()));
                    nuevo.setHoraIngreso(LocalTime.parse(turno.getHoraInicio()));
                    nuevo.setHoraSalida(LocalTime.parse(turno.getHoraFin()));
                    horarioRepository.save(nuevo);
                }
            }

            //ya voy 3 horas aca sin poder resolverlo :c, si me retiro de gtics? aun hay tiempo :D
            //no eres tu profe brenda, soy yo

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
                "Miercoles", 2,
                "Jueves", 3,
                "Viernes", 4,
                "Sabado", 5,
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
        Optional <Horario> horarioOpt = horarioRepository.findById(id);
        if (horarioRepository.existsById(id)) {  // verifica exist
            horarioRepository.deleteById(id);  // eloknina el horario
            try {
                Map<String, Object> datos = new HashMap<>();
                Horario horario = horarioOpt.get();
                String nombreCompleto = horario.getHorarioSemanal().getCoordinador().getNombre() + " " +
                        horario.getHorarioSemanal().getCoordinador().getApellido();

                datos.put("nombreCompleto", nombreCompleto);
                datos.put("fecha", horario.getFecha());
                datos.put("horaInicio", horario.getHoraIngreso());
                datos.put("horaFin", horario.getHoraSalida());
                datos.put("complejo", horario.getComplejoDeportivo().getNombre()); // si tienes relación con complejo


                // Configura destinatario asunto y cuerpo // primera version
                String destinatario = "a20212624@pucp.edu.pe"; //seteado manualmente por el momento
                String asunto = "Horario eliminado";
                String emailTemplate = "email/emailtemplate"; // Cambia esto por la ruta correcta de tu plantilla

                emailService.enviarEmail(destinatario, asunto, emailTemplate, datos);
            } catch (IOException e) {
                e.printStackTrace();

            }
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
    public String guardarNuevoComplejo(
            @RequestParam("nombreComplejo") String nombreComplejo,
            @RequestParam("direccionComplejo") String direccion,
            @RequestParam("numSoporte") String numeroSoporte,
            @RequestParam("latitudInput") Double latitud,
            @RequestParam("longitudInput") Double longitud,
            @RequestParam("sector") Integer idSector,
            @RequestParam("modoAcceso") String modoAcceso,
            @RequestParam("capacidadMaxima") String capacidadMaxima,
            @RequestParam("tipoServicio") List<String> servicios,
            @RequestParam Map<String, String> allParams
    ) {
        System.out.println("========= DEBUG FORM INPUTS =========");
        System.out.println("nombreComplejo: " + nombreComplejo);
        System.out.println("direccionComplejo: " + direccion);
        System.out.println("numSoporte: " + numeroSoporte);
        System.out.println("latitud: " + latitud);
        System.out.println("longitud: " + longitud);
        System.out.println("sector: " + idSector);
        System.out.println("modoAcceso: " + modoAcceso);
        System.out.println("capacidadMaxima: " + capacidadMaxima);
        System.out.println("tipoServicio: " + servicios);
        System.out.println("------------- allParams -------------");
        allParams.forEach((k, v) -> System.out.println(k + ": " + v));
        System.out.println("=====================================");
        // Construir sector por ID
        Sector sector = new Sector();
        sector.setIdSector(idSector);

        // Guardar complejo deportivo
        ComplejoDeportivo complejo = new ComplejoDeportivo();
        complejo.setNombre(nombreComplejo);
        complejo.setDireccion(direccion);
        complejo.setNumeroSoporte(numeroSoporte);
        complejo.setLatitud(latitud);
        complejo.setLongitud(longitud);
        complejo.setSector(sector);
        complejoRepository.save(complejo);

        for (String servicioNombre : servicios) {
            Servicio servicio = servicioRepository.findByNombre(servicioNombre);
            if (servicio == null) continue;

            switch (servicioNombre) {
                case "Cancha de grass" -> {
                    String cantidadStr = allParams.get("cantidadGrass");
                    if (cantidadStr != null && !cantidadStr.isBlank()) {
                        int cantidad = Integer.parseInt(cantidadStr);
                        for (int i = 1; i <= cantidad; i++) {
                            String key = "nombreGrass" + i;
                            String nombre = allParams.get(key);
                            System.out.println("➡ Grass " + i + " key: " + key + ", value: " + nombre);
                            if (nombre != null && !nombre.isBlank()) {
                                InstanciaServicio instancia = new InstanciaServicio();
                                instancia.setNombre(nombre);
                                instancia.setCapacidadMaxima(capacidadMaxima);
                                instancia.setModoAcceso(modoAcceso);
                                instancia.setServicio(servicio);
                                instancia.setComplejoDeportivo(complejo);
                                instanciaServicioRepository.save(instancia);
                            }
                        }
                    } else {
                        System.out.println("⚠️ cantidadGrass no recibida o vacía.");
                    }
                }

                case "Cancha de losa" -> {
                    String cantidadStr = allParams.get("cantidadLosa");
                    if (cantidadStr != null && !cantidadStr.isBlank()) {
                        int cantidad = Integer.parseInt(cantidadStr);
                        for (int i = 1; i <= cantidad; i++) {
                            String key = "nombreLosa" + i;
                            String nombre = allParams.get(key);
                            System.out.println("➡ Losa " + i + " key: " + key + ", value: " + nombre);
                            if (nombre != null && !nombre.isBlank()) {
                                InstanciaServicio instancia = new InstanciaServicio();
                                instancia.setNombre(nombre);
                                instancia.setCapacidadMaxima(capacidadMaxima);
                                instancia.setModoAcceso(modoAcceso);
                                instancia.setServicio(servicio);
                                instancia.setComplejoDeportivo(complejo);
                                instanciaServicioRepository.save(instancia);
                            }
                        }
                    } else {
                        System.out.println("⚠️ cantidadLosa no recibida o vacía.");
                    }
                }

                case "Piscina" -> {
                    String nombre = allParams.get("nombrePiscina");
                    if (nombre != null && !nombre.isEmpty()) {
                        InstanciaServicio instancia = new InstanciaServicio();
                        instancia.setNombre(nombre);
                        instancia.setCapacidadMaxima(capacidadMaxima);
                        instancia.setModoAcceso(modoAcceso);
                        instancia.setServicio(servicio);
                        instancia.setComplejoDeportivo(complejo);
                        instanciaServicioRepository.save(instancia);
                    }
                }
                case "Gimnasio" -> {
                    String nombre = allParams.get("nombreGimnasio");
                    if (nombre != null && !nombre.isEmpty()) {
                        InstanciaServicio instancia = new InstanciaServicio();
                        instancia.setNombre(nombre);
                        instancia.setCapacidadMaxima(capacidadMaxima);
                        instancia.setModoAcceso(modoAcceso);
                        instancia.setServicio(servicio);
                        instancia.setComplejoDeportivo(complejo);
                        instanciaServicioRepository.save(instancia);
                    }
                }
                case "Pista de atletismo" -> {
                    String nombre = allParams.get("nombrePista");
                    if (nombre != null && !nombre.isEmpty()) {
                        InstanciaServicio instancia = new InstanciaServicio();
                        instancia.setNombre(nombre);
                        instancia.setCapacidadMaxima(capacidadMaxima);
                        instancia.setModoAcceso(modoAcceso);
                        instancia.setServicio(servicio);
                        instancia.setComplejoDeportivo(complejo);
                        instanciaServicioRepository.save(instancia);
                    }
                }
            }
        }

        return "redirect:/admin/servicios/monitoreo"; // Cambia esto si quieres redirigir a otra vista
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


    @PostMapping("/reporte/cerrar")
    public String cerrarReporte(@RequestParam("idReporte") Integer idReporte, RedirectAttributes attr) {
        Optional<Reporte> optionalReporte = reporteRepository.findById(idReporte);

        if (optionalReporte.isPresent()) {
            Reporte reporte = optionalReporte.get();
            reporte.setEstado("Cerrado");
            reporteRepository.save(reporte);
            attr.addFlashAttribute("msg", "El reporte fue cerrado correctamente.");
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