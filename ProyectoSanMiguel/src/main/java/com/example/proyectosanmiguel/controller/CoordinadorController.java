package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.dto.HorarioDTO;
import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.*;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Horario horarioActual = horariosHoy.stream()
                .filter(h -> h.getHoraIngreso() != null && h.getHoraSalida() != null &&
                        !ahora.isBefore(h.getHoraIngreso()) && ahora.isBefore(h.getHoraSalida()))
                .findFirst()
                .orElse(null);
        String nombreComplejo = "No hay complejo asignado";
        String direccionComplejo = "-";
        String horaInicio = "";
        String horaFin = "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        if (horarioActual != null && horarioActual.getComplejoDeportivo() != null) {
            nombreComplejo = horarioActual.getComplejoDeportivo().getNombre();
            direccionComplejo = horarioActual.getComplejoDeportivo().getDireccion();
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
}
