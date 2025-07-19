package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.dto.HorarioDTO;
import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
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
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.example.proyectosanmiguel.dto.RegistroAsistenciaRequest;
import com.example.proyectosanmiguel.entity.RegistroAsistencia;


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

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private HorarioSemanalRepository horarioSemanalRepository;

    @Autowired
    private CredencialRepository credencialRepository;

    @Autowired
    private RegistroAsistenciaRepository registroAsistenciaRepository;

    @Autowired
    private ComplejoRepository complejoRepository;


    private static final int TOLERANCIA_MINUTOS = 10; // Changed to 10 minutes
    private static final double RADIO_PERMITIDO = 10000.0; // 100 meters

    /**
     * Endpoint para obtener los horarios del coordinador para el día actual
     */
    @GetMapping("/asistencia/horarios/hoy")
    @ResponseBody
    public List<Map<String, Object>> obtenerHorariosHoy(Principal principal) {
        if (principal == null) {
            return List.of();
        }
        Credencial credencial = credencialRepository.findByCorreo(principal.getName());
        if (credencial == null || credencial.getUsuario() == null) {
            return List.of();
        }
        Usuario coordinador = credencial.getUsuario();
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Lima"));
        List<Horario> horariosHoy = horarioRepository.findByCoordinador(coordinador.getIdUsuario())
                .stream()
                .filter(h -> h.getFecha() != null && h.getFecha().isEqual(hoy))
                .toList();
        return horariosHoy.stream().map(h -> {
            Map<String, Object> horario = new HashMap<>();
            horario.put("idHorario", h.getIdHorario());
            horario.put("idComplejo", h.getComplejoDeportivo().getIdComplejoDeportivo());
            horario.put("nombreComplejo", h.getComplejoDeportivo().getNombre());
            horario.put("horaInicio", h.getHoraIngreso().toString());
            horario.put("horaFin", h.getHoraSalida().toString());
            return horario;
        }).toList();
    }

    /**
     * Endpoint para verificar si es hora de registrar entrada o salida
     */
    @GetMapping("/asistencia/verificar-horario")
    @ResponseBody
    public Map<String, Object> verificarHorario(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        if (principal == null) {
            response.put("error", "No autenticado");
            return response;
        }
        try {
            Credencial credencial = credencialRepository.findByCorreo(principal.getName());
            if (credencial == null || credencial.getUsuario() == null) {
                response.put("error", "Usuario no encontrado");
                return response;
            }
            Usuario coordinador = credencial.getUsuario();
            LocalDate hoy = LocalDate.now(ZoneId.of("America/Lima"));
            LocalTime ahora = LocalTime.now(ZoneId.of("America/Lima"));
            List<Horario> horariosHoy = horarioRepository.findByCoordinador(coordinador.getIdUsuario())
                    .stream()
                    .filter(h -> h.getFecha() != null && h.getFecha().isEqual(hoy))
                    .toList();
            Optional<Horario> horarioEntrada = horariosHoy.stream()
                    .filter(h -> h.getHoraIngreso() != null &&
                            !ahora.isBefore(h.getHoraIngreso()) &&
                            ahora.isBefore(h.getHoraIngreso().plusMinutes(TOLERANCIA_MINUTOS)))
                    .findFirst();
            Optional<Horario> horarioSalida = horariosHoy.stream()
                    .filter(h -> h.getHoraSalida() != null &&
                            !ahora.isBefore(h.getHoraSalida()) &&
                            ahora.isBefore(h.getHoraSalida().plusMinutes(TOLERANCIA_MINUTOS)))
                    .findFirst();
            Optional<RegistroAsistencia> entradaHoy = registroAsistenciaRepository
                    .findUltimoRegistroPorTipoYFecha(coordinador.getIdUsuario(), "entrada", hoy);
            Optional<RegistroAsistencia> salidaHoy = registroAsistenciaRepository
                    .findUltimoRegistroPorTipoYFecha(coordinador.getIdUsuario(), "salida", hoy);
            response.put("puedeRegistrarEntrada", horarioEntrada.isPresent() && entradaHoy.isEmpty());
            response.put("puedeRegistrarSalida", horarioSalida.isPresent() && salidaHoy.isEmpty() && entradaHoy.isPresent());
            if (horarioEntrada.isPresent()) {
                Horario horario = horarioEntrada.get();
                response.put("horarioEntrada", Map.of(
                        "idHorario", horario.getIdHorario(),
                        "horaInicio", horario.getHoraIngreso().toString(),
                        "horaTope", horario.getHoraIngreso().plusMinutes(TOLERANCIA_MINUTOS).toString(),
                        "idComplejo", horario.getComplejoDeportivo().getIdComplejoDeportivo(),
                        "nombreComplejo", horario.getComplejoDeportivo().getNombre()
                ));
            }
            if (horarioSalida.isPresent()) {
                Horario horario = horarioSalida.get();
                response.put("horarioSalida", Map.of(
                        "idHorario", horario.getIdHorario(),
                        "horaFin", horario.getHoraSalida().toString(),
                        "horaTope", horario.getHoraSalida().plusMinutes(TOLERANCIA_MINUTOS).toString(),
                        "idComplejo", horario.getComplejoDeportivo().getIdComplejoDeportivo(),
                        "nombreComplejo", horario.getComplejoDeportivo().getNombre()
                ));
            }
        } catch (Exception e) {
            response.put("error", "Error al verificar horario: " + e.getMessage());
        }
        return response;
    }

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
    public String obtenerPaginaParcial(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(required = false) String filtro,
                                       Model model) {
        Pageable pageable = PageRequest.of(page, 4, Sort.by("fechaRecepcion").descending());

        Page<Reporte> pagina;

        if (filtro != null && !filtro.trim().isEmpty()) {
            pagina = reporteRepository.findByAsuntoContainingIgnoreCaseOrDescripcionContainingIgnoreCase(filtro, filtro, pageable);
        } else {
            pagina = reporteRepository.findAll(pageable);
        }

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
    public String vistaInicio(Model model, Principal principal) {
        // Obtener el coordinador autenticado
        Credencial credencial = credencialRepository.findByCorreo(principal.getName());
        if (credencial == null || credencial.getUsuario() == null) {
            model.addAttribute("error", "No se encontró el usuario autenticado.");
            return "error";
        }
        Usuario coordinador = credencial.getUsuario();

        // 1. Los 7 reportes más recientes
        List<Reporte> ultimosReportes = reporteRepository.findTop7ByOrderByFechaRecepcionDesc();

        // 2. Total de reportes
        long cantidadReportes = reporteRepository.count();

        // 3. Reportes cerrados
        long cantidadCerrados = reporteRepository.countByEstado("Cerrado");

        // 4. Foto de perfil
        // Usuario coordinador ya obtenido
        // 5. Complejo actual según la hora de Perú
        java.time.ZoneId zonaPeru = java.time.ZoneId.of("America/Lima");
        java.time.ZonedDateTime ahoraPeru = java.time.ZonedDateTime.now(zonaPeru);
        LocalTime ahora = ahoraPeru.toLocalTime();
        LocalDate hoy = ahoraPeru.toLocalDate();
        List<Horario> horariosHoy = horarioRepository.findByCoordinador(coordinador.getIdUsuario())
                .stream()
                .filter(h -> h.getFecha() != null && h.getFecha().isEqual(hoy))
                .toList();

        // Primero chequeamos si hay un horario activo ahora mismo
        Horario horarioActual = horariosHoy.stream()
                .filter(h -> h.getHoraIngreso() != null && h.getHoraSalida() != null &&
                        !ahora.isBefore(h.getHoraIngreso()) && ahora.isBefore(h.getHoraSalida()))
                .findFirst()
                .orElse(null);

        // Si no hay horario activo ahora, buscamos el próximo horario del día
        if (horarioActual == null) {
            horarioActual = horariosHoy.stream()
                    .filter(h -> h.getHoraIngreso() != null && ahora.isBefore(h.getHoraIngreso()))
                    .min((h1, h2) -> h1.getHoraIngreso().compareTo(h2.getHoraIngreso()))
                    .orElse(null);
        }

        // Si no hay próximo horario, usamos el último que acaba de pasar (para registrar salida)
        if (horarioActual == null) {
            horarioActual = horariosHoy.stream()
                    .filter(h -> h.getHoraSalida() != null)
                    .max((h1, h2) -> h1.getHoraSalida().compareTo(h2.getHoraSalida()))
                    .orElse(null);
        }

        String nombreComplejo = "No hay complejo asignado";
        String direccionComplejo = "-";
        String horaInicio = "";
        String horaFin = "";
        Double latitudComplejo = null;
        Double longitudComplejo = null;
        Integer idComplejoActual = null;
        Integer idHorarioActual = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        if (horarioActual != null && horarioActual.getComplejoDeportivo() != null) {
            ComplejoDeportivo complejo = horarioActual.getComplejoDeportivo();
            nombreComplejo = complejo.getNombre();
            direccionComplejo = complejo.getDireccion();
            latitudComplejo = complejo.getLatitud();
            longitudComplejo = complejo.getLongitud();
            idComplejoActual = complejo.getIdComplejoDeportivo();
            idHorarioActual = horarioActual.getIdHorario();
            horaInicio = horarioActual.getHoraIngreso() != null ? horarioActual.getHoraIngreso().format(formatter) : "";
            horaFin = horarioActual.getHoraSalida() != null ? horarioActual.getHoraSalida().format(formatter) : "";
        }
        // Agregar al modelo
        model.addAttribute("ultimosReportes", ultimosReportes);
        model.addAttribute("cantidadReportes", cantidadReportes);
        model.addAttribute("cantidadCerrados", cantidadCerrados);
        model.addAttribute("usuario", coordinador);
        model.addAttribute("complejoActualNombre", nombreComplejo);
        model.addAttribute("complejoActualDireccion", direccionComplejo);
        model.addAttribute("complejoHoraInicio", horaInicio);
        model.addAttribute("complejoHoraFin", horaFin);
        model.addAttribute("complejoLatitud", latitudComplejo);
        model.addAttribute("complejoLongitud", longitudComplejo);
        model.addAttribute("complejoId", idComplejoActual);
        model.addAttribute("horarioId", idHorarioActual);

        // Comprobar si ya registró entrada o salida hoy
        Optional<RegistroAsistencia> entradaHoy = registroAsistenciaRepository
                .findUltimoRegistroPorTipoYFecha(coordinador.getIdUsuario(), "entrada", hoy);

        Optional<RegistroAsistencia> salidaHoy = registroAsistenciaRepository
                .findUltimoRegistroPorTipoYFecha(coordinador.getIdUsuario(), "salida", hoy);

        model.addAttribute("entradaRegistrada", entradaHoy.isPresent());
        model.addAttribute("salidaRegistrada", salidaHoy.isPresent());

        // Verificar si está en horario de entrada o salida (con tolerancia)
        // Comprobar si ya registró entrada o salida para este horario hoy
        Optional<RegistroAsistencia> entradaHoyHorario = Optional.empty();
        Optional<RegistroAsistencia> salidaHoyHorario = Optional.empty();
        if (horarioActual != null && idHorarioActual != null) {
            entradaHoyHorario = registroAsistenciaRepository
                .findUltimoRegistroPorTipoYFechaYHorario(coordinador.getIdUsuario(), "entrada", hoy, idHorarioActual);
            salidaHoyHorario = registroAsistenciaRepository
                .findUltimoRegistroPorTipoYFechaYHorario(coordinador.getIdUsuario(), "salida", hoy, idHorarioActual);
        }

        boolean puedeRegistrarEntrada = false;
        boolean puedeRegistrarSalida = false;

        if (horarioActual != null) {
            if (horarioActual.getHoraIngreso() != null) {
                LocalTime horaLimiteEntrada = horarioActual.getHoraIngreso().plusMinutes(TOLERANCIA_MINUTOS);
                puedeRegistrarEntrada = !ahora.isBefore(horarioActual.getHoraIngreso()) &&
                        ahora.isBefore(horaLimiteEntrada) &&
                        entradaHoyHorario.isEmpty();
                model.addAttribute("horaLimiteEntrada", horaLimiteEntrada.format(formatter));
            } else {
                model.addAttribute("horaLimiteEntrada", "-");
            }

            if (horarioActual.getHoraSalida() != null) {
                LocalTime horaLimiteSalida = horarioActual.getHoraSalida().plusMinutes(TOLERANCIA_MINUTOS);
                puedeRegistrarSalida = !ahora.isBefore(horarioActual.getHoraSalida()) &&
                        ahora.isBefore(horaLimiteSalida) &&
                        salidaHoyHorario.isEmpty() &&
                        entradaHoyHorario.isPresent();
                model.addAttribute("horaLimiteSalida", horaLimiteSalida.format(formatter));
            } else {
                model.addAttribute("horaLimiteSalida", "-");
            }
        } else {
            model.addAttribute("horaLimiteEntrada", "-");
            model.addAttribute("horaLimiteSalida", "-");
        }
        model.addAttribute("puedeRegistrarEntrada", puedeRegistrarEntrada);
        model.addAttribute("puedeRegistrarSalida", puedeRegistrarSalida);

        // Determinar si hay horarios para hoy y si hay un horario actual
        boolean hayHorariosHoy = horariosHoy != null && !horariosHoy.isEmpty();
        boolean hayHorarioActual = horarioActual != null;
        model.addAttribute("noHayMasHorarios", !hayHorariosHoy);
        model.addAttribute("hayHorariosHoy", hayHorariosHoy);
        model.addAttribute("hayHorarioActual", hayHorarioActual);
        model.addAttribute("horarioActual", horarioActual); // para el HTML

        // --- INICIO: Lógica para Última hora de entrada y Turnos semanales ---
        // 1. Última hora de entrada registrada hoy
        Optional<RegistroAsistencia> ultimaEntradaHoy = registroAsistenciaRepository
                .findUltimoRegistroPorTipoYFecha(coordinador.getIdUsuario(), "entrada", hoy);
        String ultimaHoraEntrada = "-";
        String horaOriginalEntrada = "-";
        if (horarioActual != null && horarioActual.getHoraIngreso() != null) {
            horaOriginalEntrada = horarioActual.getHoraIngreso().format(formatter);
        }
        if (ultimaEntradaHoy.isPresent()) {
            LocalTime horaRegistro = ultimaEntradaHoy.get().getFechaHora().toLocalTime();
            ultimaHoraEntrada = horaRegistro.format(formatter);
        }
        model.addAttribute("ultimaHoraEntrada", ultimaHoraEntrada);
        model.addAttribute("horaOriginalEntrada", horaOriginalEntrada);

        // 2. Turnos semanales y porcentaje de cumplimiento
        // Calcular inicio y fin de la semana actual (lunes a domingo)
        LocalDate inicioSemana = hoy.with(java.time.DayOfWeek.MONDAY);
        LocalDate finSemana = inicioSemana.plusDays(6);
        // Todos los horarios de la semana para el coordinador
        List<Horario> horariosSemana = horarioRepository.findByCoordinador(coordinador.getIdUsuario())
                .stream()
                .filter(h -> h.getFecha() != null && !h.getFecha().isBefore(inicioSemana) && !h.getFecha().isAfter(finSemana))
                .toList();
        int totalTurnosSemana = horariosSemana.size();
        // Turnos cumplidos: registros de entrada para cada horario de la semana
        int turnosCumplidos = 0;
        for (Horario h : horariosSemana) {
            Optional<RegistroAsistencia> reg = registroAsistenciaRepository
                .findUltimoRegistroPorTipoYFecha(coordinador.getIdUsuario(), "entrada", h.getFecha());
            if (reg.isPresent()) {
                turnosCumplidos++;
            }
        }
        double porcentajeCumplido = totalTurnosSemana > 0 ? (turnosCumplidos * 100.0 / totalTurnosSemana) : 0.0;
        model.addAttribute("totalTurnosSemana", totalTurnosSemana);
        model.addAttribute("porcentajeTurnosCumplidos", porcentajeCumplido);
        // --- FIN: Lógica para Última hora de entrada y Turnos semanales ---

        return "Coordinador/coordinador_inicio";
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(Model model, Principal principal) {
        Credencial credencial = credencialRepository.findByCorreo(principal.getName());
        if (credencial == null || credencial.getUsuario() == null) {
            model.addAttribute("error", "No se encontró el usuario autenticado.");
            return "error";
        }
        Usuario coordinador = credencial.getUsuario();
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

    private static final Map<DayOfWeek, String> DIAS_ES = Map.of(
            DayOfWeek.MONDAY, "Lunes",
            DayOfWeek.TUESDAY, "Martes",
            DayOfWeek.WEDNESDAY, "Miércoles",
            DayOfWeek.THURSDAY, "Jueves",
            DayOfWeek.FRIDAY, "Viernes",
            DayOfWeek.SATURDAY, "Sábado",
            DayOfWeek.SUNDAY, "Domingo"
    );
    @GetMapping("/horarios")
    public String verHorariosCoordinador(Model model, Principal principal) {
        Credencial credencial = credencialRepository.findByCorreo(principal.getName());

        if (credencial == null || credencial.getUsuario() == null) {
            model.addAttribute("error", "No se encontró el usuario autenticado.");
            return "error";
        }

        Usuario coordinador = credencial.getUsuario();

        if (coordinador.getRol() == null || coordinador.getRol().getIdRol() != 3) {
            model.addAttribute("error", "No tienes permiso para acceder a esta vista.");
            return "error";
        }

        // Calcular inicio y fin de la semana actual y la siguiente
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemanaActual = hoy.with(java.time.DayOfWeek.MONDAY);
        LocalDate finSemanaActual = inicioSemanaActual.plusDays(6);
        LocalDate inicioSemanaSiguiente = inicioSemanaActual.plusWeeks(1);
        LocalDate finSemanaSiguiente = inicioSemanaSiguiente.plusDays(6);

        // Filtrar solo las semanas actual y siguiente
        List<HorarioSemanal> horariosSemanales = horarioSemanalRepository.findByIdCoordinador(coordinador.getIdUsuario())
                .stream()
                .filter(hs ->
                        ( !hs.getFechaInicio().isBefore(inicioSemanaActual) && !hs.getFechaFin().isAfter(finSemanaSiguiente) )
                )
                .toList();

        List<Horario> horarios = horarioRepository.findAllByIdHorarioSemanalIn(
                horariosSemanales.stream()
                        .map(HorarioSemanal::getIdHorarioSemanal)
                        .toList()
        );

        // Filtros para DataTable
        DateTimeFormatter formatoDia = DateTimeFormatter.ofPattern("dd");
        DateTimeFormatter formatoMes = DateTimeFormatter.ofPattern("MMMM", new Locale("es"));

        List<String> semanasTexto = horariosSemanales.stream()
                .map(hs -> {
                    String inicio = hs.getFechaInicio().format(formatoDia);
                    String fin = hs.getFechaFin().format(formatoDia);
                    String mes = hs.getFechaFin().format(formatoMes);
                    return "Semana del " + inicio + " al " + fin + " de " + mes;
                })
                .distinct()
                .toList();

        model.addAttribute("semanas", semanasTexto);

        List<HorarioDTO> horariosDTO = horarios.stream().map(h -> {
            HorarioDTO dto = new HorarioDTO();
            dto.setId(h.getIdHorario());
            dto.setFecha(h.getFecha());
            dto.setDiaSemana(DIAS_ES.get(h.getFecha().getDayOfWeek()));
            dto.setHoraIngreso(h.getHoraIngreso());
            dto.setHoraSalida(h.getHoraSalida());
            dto.setComplejoNombre(h.getComplejoDeportivo().getNombre());
            dto.setDireccionComplejo(h.getComplejoDeportivo().getDireccion());
            String inicio = h.getHorarioSemanal().getFechaInicio().format(formatoDia);
            String fin = h.getHorarioSemanal().getFechaFin().format(formatoDia);
            String mes = h.getHorarioSemanal().getFechaFin().format(formatoMes);
            dto.setSemanaTexto("Semana del " + inicio + " al " + fin + " de " + mes);
            return dto;
        }).toList();

        model.addAttribute("listaHorarios", horariosDTO);
        model.addAttribute("semanasTexto", semanasTexto);

        return "Coordinador/coordinador_horario";
    }

    @GetMapping("/reportes/estadisticas-estado")
    @ResponseBody
    public Map<String, Long> obtenerEstadisticasPorEstado(Principal principal) {
        // Si quieres filtrar por coordinador, puedes obtener el usuario aquí
        // Credencial credencial = credencialRepository.findByCorreo(principal.getName());
        // Usuario coordinador = credencial.getUsuario();

        long abiertos = reporteRepository.countByEstadoIgnoreCase("Abierto");
        long enProceso = reporteRepository.countByEstadoIgnoreCase("En Proceso");
        long cerrados = reporteRepository.countByEstadoIgnoreCase("Cerrado");

        Map<String, Long> data = new java.util.HashMap<>();
        data.put("Abierto", abiertos);
        data.put("En Proceso", enProceso);
        data.put("Cerrado", cerrados);
        return data;
    }


    @PostMapping("/asistencia/registrar")
    @ResponseBody
    public Map<String, Object> registrarAsistencia(
            @RequestParam("tipoRegistro") String tipoRegistro,
            @RequestParam("latitud") Double latitud,
            @RequestParam("longitud") Double longitud,
            @RequestParam("precision") Double precision,
            @RequestParam("idComplejo") Integer idComplejo,
            @RequestParam("idHorario") Integer idHorario,
            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Error desconocido");
        Usuario coordinador = null;
        try {
            System.out.println("POST /asistencia/registrar - tipoRegistro: " + tipoRegistro + " at " + LocalDateTime.now(ZoneId.of("America/Lima")));
            System.out.println("Valores recibidos: latitud=" + latitud + ", longitud=" + longitud + ", precision=" + precision + ", idComplejo=" + idComplejo + ", idHorario=" + idHorario);
            if (principal == null) {
                response.put("message", "No hay usuario autenticado");
                return response;
            }
            Credencial credencial = credencialRepository.findByCorreo(principal.getName());
            if (credencial == null || credencial.getUsuario() == null) {
                response.put("message", "Usuario no autenticado");
                return response;
            }
            coordinador = credencial.getUsuario();
            LocalDate hoy = LocalDate.now(ZoneId.of("America/Lima"));
            LocalTime ahora = LocalTime.now(ZoneId.of("America/Lima"));
            // Cambiar validación: solo se permite un registro por tipo y horario en el día
            Optional<RegistroAsistencia> registroExistente = registroAsistenciaRepository
                    .findUltimoRegistroPorTipoYFechaYHorario(coordinador.getIdUsuario(), tipoRegistro, hoy, idHorario);
            if (registroExistente.isPresent()) {
                response.put("message", "Ya has registrado tu " + tipoRegistro + " para este horario el día de hoy");
                return response;
            }
            Optional<ComplejoDeportivo> complejoOpt = complejoRepository.findById(idComplejo);
            if (complejoOpt.isEmpty()) {
                response.put("message", "El complejo seleccionado no existe");
                return response;
            }
            ComplejoDeportivo complejo = complejoOpt.get();
            double distanciaComplejo = calculateDistance(latitud, longitud, complejo.getLatitud(), complejo.getLongitud());
            System.out.println("Distance to complex: " + distanciaComplejo + " meters, Radius allowed: " + RADIO_PERMITIDO + " meters");
            if (distanciaComplejo > RADIO_PERMITIDO) {
                response.put("message", String.format("Estás a %.2f metros del complejo. Debes estar a menos de %.0f metros para registrar asistencia.", distanciaComplejo, RADIO_PERMITIDO));
                return response;
            }
            if ("salida".equals(tipoRegistro)) {
                // Validar que haya una entrada registrada para este horario
                Optional<RegistroAsistencia> entradaHoy = registroAsistenciaRepository
                        .findUltimoRegistroPorTipoYFechaYHorario(coordinador.getIdUsuario(), "entrada", hoy, idHorario);
                if (entradaHoy.isEmpty()) {
                    response.put("message", "No puedes registrar salida sin haber registrado entrada para este horario hoy");
                    return response;
                }
            }
            List<Horario> horariosHoy = horarioRepository.findByCoordinador(coordinador.getIdUsuario())
                    .stream()
                    .filter(h -> h.getFecha() != null && h.getFecha().isEqual(hoy))
                    .toList();
            boolean horarioValido = false;
            boolean esTardanza = false;
            if ("entrada".equals(tipoRegistro)) {
                Optional<Horario> horarioEntrada = horariosHoy.stream()
                        .filter(h -> h.getIdHorario().equals(idHorario) && h.getHoraIngreso() != null &&
                                !ahora.isBefore(h.getHoraIngreso()) &&
                                ahora.isBefore(h.getHoraIngreso().plusMinutes(10)))
                        .findFirst();
                if (horarioEntrada.isPresent()) {
                    horarioValido = true;
                    esTardanza = ahora.isAfter(horarioEntrada.get().getHoraIngreso());
                }
            } else if ("salida".equals(tipoRegistro)) {
                Optional<Horario> horarioSalida = horariosHoy.stream()
                        .filter(h -> h.getIdHorario().equals(idHorario) && h.getHoraSalida() != null &&
                                !ahora.isBefore(h.getHoraSalida()) &&
                                ahora.isBefore(h.getHoraSalida().plusMinutes(10)))
                        .findFirst();
                if (horarioSalida.isPresent()) {
                    horarioValido = true;
                    esTardanza = ahora.isAfter(horarioSalida.get().getHoraSalida());
                }
            }
            if (!horarioValido) {
                response.put("message", "No estás dentro del horario permitido para registrar " + tipoRegistro);
                return response;
            }
            RegistroAsistencia registro = new RegistroAsistencia();
            registro.setCoordinador(coordinador);
            registro.setComplejo(complejo);
            registro.setTipoRegistro(tipoRegistro);
            registro.setFechaHora(LocalDateTime.now(ZoneId.of("America/Lima")));
            registro.setLatitudCoordinador(latitud);
            registro.setLongitudCoordinador(longitud);
            registro.setPrecisionMetros(precision);
            registro.setDistanciaComplejo(distanciaComplejo);
            registro.setValidado(true);
            registro.setEsTardanza(esTardanza);
            registro.setCreatedAt(LocalDateTime.now());
            // Asociar el horario al registro de asistencia
            Horario horario = horarioRepository.findById(idHorario).orElse(null);
            if (horario != null) {
                registro.setHorario(horario);
            }
            System.out.println("Registro de asistencia a guardar: " + registro);
            registroAsistenciaRepository.save(registro);
            System.out.println("Registro guardado en BD: " + registro);
            String mensajeEstado = esTardanza ? " (TARDANZA)" : "";
            response.put("success", true);
            response.put("message", "Se registró la " + tipoRegistro + " correctamente" + mensajeEstado);
            response.put("fechaHora", registro.getFechaHora().toString());
            response.put("complejo", complejo.getNombre());
            response.put("esTardanza", esTardanza);
            return response;
        } catch (Exception e) {
            System.out.println("Error al guardar asistencia: " + e.getMessage());
            response.put("message", "Error interno: " + e.getMessage());
            return response;
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Endpoint para verificar y registrar faltas automáticamente
     * Se ejecuta para verificar horarios que ya pasaron sin registro
     */
    @PostMapping("/asistencia/verificar-faltas")
    @ResponseBody
    public Map<String, Object> verificarFaltas(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            Credencial credencial = credencialRepository.findByCorreo(principal.getName());
            if (credencial == null || credencial.getUsuario() == null) {
                response.put("success", false);
                response.put("message", "Usuario no autenticado");
                return response;
            }
            Usuario coordinador = credencial.getUsuario();
            LocalDate hoy = LocalDate.now(ZoneId.of("America/Lima"));
            LocalTime ahora = LocalTime.now(ZoneId.of("America/Lima"));
            List<Horario> horariosHoy = horarioRepository.findByCoordinador(coordinador.getIdUsuario())
                    .stream()
                    .filter(h -> h.getFecha() != null && h.getFecha().isEqual(hoy))
                    .toList();
            int faltasRegistradas = 0;
            for (Horario horario : horariosHoy) {
                if (horario.getHoraIngreso() != null) {
                    LocalTime horaLimiteEntrada = horario.getHoraIngreso().plusMinutes(TOLERANCIA_MINUTOS);
                    if (ahora.isAfter(horaLimiteEntrada)) {
                        long registrosEntrada = registroAsistenciaRepository
                                .countRegistrosPorTipoYFecha(coordinador.getIdUsuario(), "entrada", hoy);
                        if (registrosEntrada == 0) {
                            RegistroAsistencia falta = new RegistroAsistencia();
                            falta.setCoordinador(coordinador);
                            falta.setComplejo(horario.getComplejoDeportivo());
                            falta.setTipoRegistro("falta_entrada");
                            falta.setFechaHora(LocalDateTime.of(hoy, horario.getHoraIngreso()));
                            falta.setValidado(false);
                            falta.setEsTardanza(false);
                            falta.setCreatedAt(LocalDateTime.now());
                            registroAsistenciaRepository.save(falta);
                            faltasRegistradas++;
                        }
                    }
                }
                if (horario.getHoraSalida() != null) {
                    LocalTime horaLimiteSalida = horario.getHoraSalida().plusMinutes(TOLERANCIA_MINUTOS);
                    if (ahora.isAfter(horaLimiteSalida)) {
                        long registrosEntrada = registroAsistenciaRepository
                                .countRegistrosPorTipoYFecha(coordinador.getIdUsuario(), "entrada", hoy);
                        long registrosSalida = registroAsistenciaRepository
                                .countRegistrosPorTipoYFecha(coordinador.getIdUsuario(), "salida", hoy);
                        if (registrosEntrada > 0 && registrosSalida == 0) {
                            RegistroAsistencia falta = new RegistroAsistencia();
                            falta.setCoordinador(coordinador);
                            falta.setComplejo(horario.getComplejoDeportivo());
                            falta.setTipoRegistro("falta_salida");
                            falta.setFechaHora(LocalDateTime.of(hoy, horario.getHoraSalida()));
                            falta.setValidado(false);
                            falta.setEsTardanza(false);
                            falta.setCreatedAt(LocalDateTime.now());
                            registroAsistenciaRepository.save(falta);
                            faltasRegistradas++;
                        }
                    }
                }
            }
            response.put("success", true);
            response.put("faltasRegistradas", faltasRegistradas);
            response.put("message", faltasRegistradas > 0 ?
                    "Se registraron " + faltasRegistradas + " faltas" :
                    "No hay faltas pendientes");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al verificar faltas: " + e.getMessage());
        }
        return response;
    }

    // Helper para recalcular habilitación de botones
    private void setBotonesAsistencia(Model model, Usuario coordinador, LocalDate hoy, LocalTime ahora) {
        boolean puedeRegistrarEntrada = false;
        boolean puedeRegistrarSalida = false;
        if (coordinador != null) {
            List<Horario> horariosHoy = horarioRepository.findByCoordinador(coordinador.getIdUsuario())
                    .stream()
                    .filter(h -> h.getFecha() != null && h.getFecha().isEqual(hoy))
                    .toList();
            Optional<Horario> horarioEntrada = horariosHoy.stream()
                    .filter(h -> h.getHoraIngreso() != null &&
                            !ahora.isBefore(h.getHoraIngreso()) &&
                            ahora.isBefore(h.getHoraIngreso().plusMinutes(TOLERANCIA_MINUTOS)))
                    .findFirst();
            Optional<Horario> horarioSalida = horariosHoy.stream()
                    .filter(h -> h.getHoraSalida() != null &&
                            !ahora.isBefore(h.getHoraSalida()) &&
                            ahora.isBefore(h.getHoraSalida().plusMinutes(TOLERANCIA_MINUTOS)))
                    .findFirst();
            Optional<RegistroAsistencia> entradaHoy = registroAsistenciaRepository
                    .findUltimoRegistroPorTipoYFecha(coordinador.getIdUsuario(), "entrada", hoy);
            Optional<RegistroAsistencia> salidaHoy = registroAsistenciaRepository
                    .findUltimoRegistroPorTipoYFecha(coordinador.getIdUsuario(), "salida", hoy);
            puedeRegistrarEntrada = horarioEntrada.isPresent() && entradaHoy.isEmpty();
            puedeRegistrarSalida = horarioSalida.isPresent() && salidaHoy.isEmpty() && entradaHoy.isPresent();
        }
        model.addAttribute("puedeRegistrarEntrada", puedeRegistrarEntrada);
        model.addAttribute("puedeRegistrarSalida", puedeRegistrarSalida);
    }

    @GetMapping("/asistencia/estado")
    @ResponseBody
    public Map<String, Object> estadoAsistenciaHorario(@RequestParam("horarioId") Integer idHorario, Principal principal) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("entradaRegistrada", false);
        resp.put("salidaRegistrada", false);
        if (principal == null) return resp;
        Credencial credencial = credencialRepository.findByCorreo(principal.getName());
        if (credencial == null || credencial.getUsuario() == null) return resp;
        Usuario coordinador = credencial.getUsuario();
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Lima"));
        boolean entrada = registroAsistenciaRepository
            .findUltimoRegistroPorTipoYFechaYHorario(coordinador.getIdUsuario(), "entrada", hoy, idHorario)
            .isPresent();
        boolean salida = registroAsistenciaRepository
            .findUltimoRegistroPorTipoYFechaYHorario(coordinador.getIdUsuario(), "salida", hoy, idHorario)
            .isPresent();
        resp.put("entradaRegistrada", entrada);
        resp.put("salidaRegistrada", salida);
        return resp;
    }
}
