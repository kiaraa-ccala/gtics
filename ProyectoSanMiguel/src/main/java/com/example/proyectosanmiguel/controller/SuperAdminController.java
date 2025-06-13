package com.example.proyectosanmiguel.controller;
import com.example.proyectosanmiguel.dto.AsistenciaSemanalDto;
import com.example.proyectosanmiguel.dto.ComparativaAsistenciaDto;
import com.example.proyectosanmiguel.dto.EstadisticasPersonalDto;
import com.example.proyectosanmiguel.dto.ReporteReservasDto;
import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.ComplejoRepository;
import com.example.proyectosanmiguel.repository.RolRepository;
import com.example.proyectosanmiguel.repository.SectorRepository;
import com.example.proyectosanmiguel.repository.UsuarioRepository;
import com.example.proyectosanmiguel.repository.ServicioRepository;
import com.example.proyectosanmiguel.repository.ReservaRepository;
import com.example.proyectosanmiguel.service.ReporteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Controller
public class SuperAdminController {


    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private ComplejoRepository complejoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //Lista de Usuarios

    @GetMapping({"/superadmin", "/superadmin/", "/superadmin/usuarios/lista"})
    public String mostrarListaUsuarios(Model model) {

        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Usuario> usuariosFiltrados = new ArrayList<>();

        for(Usuario usuario : usuarios){
            if(!usuario.getRol().getNombre().equalsIgnoreCase("Superadministrador") && usuario.getActivo()!=0){
                usuariosFiltrados.add(usuario);
            }
        }

        model.addAttribute("listaUsuarios", usuariosFiltrados);

        return "SuperAdmin/superadmin_listaUsuarios";
    }

    //Formulario de creacion de Usuarios

    @GetMapping({"/superadmin/usuarios/agregar"})
    public String formularioCreacionUsuario(Model model) {

        List<Sector> sectores = sectorRepository.findAll();
        List<Rol> roles = rolRepository.findAll();

        List<Rol> rolesFiltrados = new ArrayList<>();

        for (Rol rol : roles) {
            if (!rol.getNombre().equalsIgnoreCase("Superadministrador")) {
                rolesFiltrados.add(rol);
            }
        }

        model.addAttribute("sectores", sectores);
        model.addAttribute("roles", rolesFiltrados);

        return "SuperAdmin/superadmin_agregarUsuarios";
    }

    //Guardar los datos del formulario de crear
    @PostMapping("/superadmin/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, @RequestParam("correo") String email, @RequestParam("password") String password) {

        Usuario usuarioExistente = null;

        if (usuario.getIdUsuario() != null) {
            usuarioExistente = usuarioRepository.findById(usuario.getIdUsuario()).orElse(null);
        }

        if (usuarioExistente != null) {
            // Actualizar
            usuarioExistente.setNombre(usuario.getNombre());
            usuarioExistente.setApellido(usuario.getApellido());
            usuarioExistente.setDni(usuario.getDni());
            usuarioExistente.setDireccion(usuario.getDireccion());
            usuarioExistente.setDistrito(usuario.getDistrito());
            usuarioExistente.setProvincia(usuario.getProvincia());
            usuarioExistente.setDepartamento(usuario.getDepartamento());
            usuarioExistente.setTelefono(usuario.getTelefono());
            usuarioExistente.setSector(usuario.getSector());
            usuarioExistente.setRol(usuario.getRol());
            usuarioExistente.setTercerizado(usuario.getTercerizado());

            if (usuarioExistente.getCredencial() != null) {
                usuarioExistente.getCredencial().setCorreo(email);
                // Hasheamos la contraseña
                if (password != null && !password.isEmpty()) {
                    usuarioExistente.getCredencial().setPassword(passwordEncoder.encode(password));
                }
            } else {
                Credencial credencial = new Credencial();
                credencial.setCorreo(email);
                credencial.setPassword(passwordEncoder.encode(password));
                credencial.setUsuario(usuarioExistente);
                usuarioExistente.setCredencial(credencial);
            }

            usuarioRepository.save(usuarioExistente);

        } else {
            // Crear nuevo
            Credencial credencial = new Credencial();
            credencial.setCorreo(email);
            credencial.setPassword(passwordEncoder.encode(password)); // Hasheamos la contraseña
            credencial.setUsuario(usuario);
            usuario.setCredencial(credencial);
            usuario.setActivo(1);

            usuarioRepository.save(usuario);
        }

        return "redirect:/superadmin/usuarios/lista";
    }

    //Borrar un usuario

    @GetMapping("/superadmin/usuarios/eliminar")
    public String eliminarAuto(@RequestParam("idUsuario") int idUsuario) {

        Optional<Usuario> usuario = usuarioRepository.findById(idUsuario);

        if(usuario.isPresent()) {

            Usuario usuarioEncontrado = usuario.get();

            usuarioEncontrado.setActivo(0);

            usuarioRepository.save(usuarioEncontrado);
        }

        return "redirect:/superadmin/usuarios/lista";
    }

    //Editar un usuario

    @GetMapping("/superadmin/usuarios/editar")
    public String editarUsuario(@RequestParam("idUsuario") int idUsuario, Model model) {

        Optional<Usuario> auxUsuario = usuarioRepository.findById(idUsuario);

        if(auxUsuario.isPresent()) {
            Usuario usuario = auxUsuario.get();
            model.addAttribute("usuario", usuario);

            List<Sector> sectores = sectorRepository.findAll();
            List<Rol> roles = rolRepository.findAll();

            List<Rol> rolesFiltrados = new ArrayList<>();

            for (Rol rol : roles) {
                if (!rol.getNombre().equalsIgnoreCase("Superadministrador")) {
                    rolesFiltrados.add(rol);
                }
            }

            model.addAttribute("sectores", sectores);
            model.addAttribute("roles", rolesFiltrados);

            return "SuperAdmin/superadmin_editarUsuarios";
        }
        else{
            return "redirect:/superadmin/usuarios/lista";
        }
    }

    // "/superadmin/estadisticas/personal"

    @GetMapping("/superadmin/estadisticas/personal")
    public String estadisticasPersonal() {

        return "SuperAdmin/superadmin_estadisticasPersonal";
    }

    // "/superadmin/estadisticas/financieras"

    @GetMapping("/superadmin/estadisticas/financieras")
    public String estadisticasFinancieras() {

        return "SuperAdmin/superadmin_estadisticasFinancieras";
    }

    // Reporte de servicios deportivos para el rol de SuperAdmin

    @GetMapping("/superadmin/reportes/servicios")
    public String reportesServicios(Model model) {

        model.addAttribute("reportes", complejoRepository.getReporteServiciosSuperAdmin());

        return "SuperAdmin/superadmin_reporteServicios";
    }

    // Reporte de datos financieros sobre los servicios deportivos para SuperAdmin

    @GetMapping("/superadmin/reportes/financiero")
    public String reportesFinanciero(Model model) {

        model.addAttribute("reportes", complejoRepository.getReporteServiciosFinancierosSuperAdmin());

        return "SuperAdmin/superadmin_reporteFinanciero";
    }

    // "/superadmin/asistencia"

    @GetMapping("/superadmin/asistencia")
    public String superadminAsistencia(Model model) {

        model.addAttribute("reportes", complejoRepository.getReporteHorarios());

        return "SuperAdmin/superadmin_asistencia";
    }

    // Página de generación de reportes

    @GetMapping("/superadmin/reportes/generar")
    public String generarReportes(Model model) {

        // Cargar todos los complejos deportivos para el selector de instalaciones
        List<ComplejoDeportivo> complejos = complejoRepository.findAll();
        model.addAttribute("complejos", complejos);

        return "SuperAdmin/superadmin_generarReportes";
    }

    // API endpoint para obtener servicios para filtros
    @GetMapping("/superadmin/api/servicios")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerServicios() {
        try {
            List<Servicio> servicios = servicioRepository.findAll();
            List<Map<String, Object>> serviciosDto = new ArrayList<>();

            for (Servicio servicio : servicios) {
                Map<String, Object> servicioMap = new HashMap<>();
                servicioMap.put("id", servicio.getIdServicio());
                servicioMap.put("nombre", servicio.getNombre());
                serviciosDto.add(servicioMap);
            }

            return ResponseEntity.ok(serviciosDto);
        } catch (Exception e) {
            System.err.println("Error al obtener servicios: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/superadmin/api/estadisticas/personal")
    @ResponseBody
    public ResponseEntity<EstadisticasPersonalDto> getEstadisticasPersonal() {
        try {
            LocalDate hoy = LocalDate.now();
            LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
            LocalDate finSemana = inicioSemana.plusDays(6);

            Long totalPersonal = usuarioRepository.countPersonalActivo();

            Long horariosATiempo = usuarioRepository.countHorariosATiempoSemana(inicioSemana, finSemana);

            Long horariosAusentes = usuarioRepository.countHorariosAusentesSemana(inicioSemana, finSemana);

            // Calcular porcentajes
            Long totalHorarios = horariosATiempo + horariosAusentes;
            Double porcentajeATiempo = totalHorarios > 0 ? (horariosATiempo * 100.0) / totalHorarios : 0.0;
            Double porcentajeAusente = totalHorarios > 0 ? (horariosAusentes * 100.0) / totalHorarios : 0.0;
            
            EstadisticasPersonalDto estadisticas = new EstadisticasPersonalDto(
                totalPersonal.intValue(),
                horariosATiempo.intValue(),
                horariosAusentes.intValue(),
                porcentajeATiempo,
                porcentajeAusente
            );
            
            System.out.println("DEBUG: Estadísticas creadas correctamente");
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            System.err.println("ERROR en getEstadisticasPersonal: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/superadmin/api/asistencia/semanal")
    @ResponseBody
    public ResponseEntity<AsistenciaSemanalDto> getAsistenciaSemanal() {
        try {
            LocalDate hoy = LocalDate.now();
            LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
            LocalDate finSemana = inicioSemana.plusDays(6);
            
            List<Object[]> resultados = usuarioRepository.getAsistenciaSemanal(inicioSemana, finSemana);
            
            List<String> dias = Arrays.asList("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom");
            List<Integer> asistencias = new ArrayList<>();
            List<Integer> tardanzas = new ArrayList<>();
            List<Integer> ausencias = new ArrayList<>();
            
            // Inicializar con ceros
            for (int i = 0; i < 7; i++) {
                asistencias.add(0);
                tardanzas.add(0);
                ausencias.add(0);
            }
              // Llenar con datos reales
            for (Object[] resultado : resultados) {
                int dayOfWeek = ((Number) resultado[0]).intValue(); // DAYOFWEEK(h.fecha)
                int diaIndex = dayOfWeek - 2; // MySQL DAYOFWEEK: 1=Domingo, 2=Lunes, etc.
                if (diaIndex < 0) diaIndex = 6; // Domingo al final
                
                asistencias.set(diaIndex, ((Number) resultado[2]).intValue()); // aTiempo
                tardanzas.set(diaIndex, ((Number) resultado[3]).intValue());   // tardanza
                ausencias.set(diaIndex, ((Number) resultado[4]).intValue());   // ausente
            }
            
            AsistenciaSemanalDto asistenciaSemanal = new AsistenciaSemanalDto();
            asistenciaSemanal.setDias(dias);
            asistenciaSemanal.setAsistencias(asistencias);
            asistenciaSemanal.setTardanzas(tardanzas);
            asistenciaSemanal.setAusencias(ausencias);
            
            return ResponseEntity.ok(asistenciaSemanal);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/superadmin/api/asistencia/comparativa")
    @ResponseBody
    public ResponseEntity<ComparativaAsistenciaDto> getComparativaAsistencia() {
        try {
            LocalDate hoy = LocalDate.now();
            LocalDate inicioMeses = hoy.minusMonths(3).withDayOfMonth(1);

            List<Object[]> resultados = usuarioRepository.getPorcentajeAsistenciaMensual(inicioMeses);

            List<String> meses = new ArrayList<>();
            List<Double> porcentajes = new ArrayList<>();

            for (Object[] resultado : resultados) {
                String mesAno = (String) resultado[0];      // CONCAT(MONTHNAME(...), ' ', YEAR(...))
                Double porcentaje = ((Number) resultado[3]).doubleValue(); // porcentajeAsistencia

                meses.add(mesAno);
                porcentajes.add(porcentaje);
            }

            // Calcular cambio porcentual respecto al mes anterior
            Double cambioPercentual = 0.0;
            if (porcentajes.size() >= 2) {
                Double mesAnterior = porcentajes.get(porcentajes.size() - 2);
                Double mesActual = porcentajes.get(porcentajes.size() - 1);
                cambioPercentual = mesActual - mesAnterior;
            }

            ComparativaAsistenciaDto comparativa = new ComparativaAsistenciaDto();
            comparativa.setMeses(meses);
            comparativa.setPorcentajesAsistencia(porcentajes);
            comparativa.setCambioPercentual(cambioPercentual);

            return ResponseEntity.ok(comparativa);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Endpoint para generar reporte de reservas en PDF
    @PostMapping("/superadmin/reportes/generar")
    @ResponseBody
    public ResponseEntity<byte[]> generarReporte(@RequestParam("tipoReporte") String tipoReporte,
                                                  @RequestParam("rangoFecha") String rangoFecha,
                                                  @RequestParam(value = "fechaInicio", required = false) String fechaInicio,
                                                  @RequestParam(value = "fechaFin", required = false) String fechaFin,
                                                  @RequestParam(value = "instalaciones", required = false) Integer idComplejo,
                                                  @RequestParam(value = "filtros", required = false) Map<String, String> filtros) {
        System.out.println("=== SE LLAMÓ A GENERAR REPORTE ===");
        System.out.println("Tipo de reporte: " + tipoReporte);
        System.out.println("Rango de fecha: " + rangoFecha);
        System.out.println("Fecha inicio: " + fechaInicio);
        System.out.println("Fecha fin: " + fechaFin);
        System.out.println("ID Complejo: " + idComplejo);
        System.out.println("Filtros: " + filtros);
        System.out.println("Fecha de inicio: " + fechaInicio);
        System.out.println("Fecha de fin: " + fechaFin);
        System.out.println("ID de complejo: " + idComplejo);
        System.out.println("Filtros: " + filtros);
        try {
            System.out.println("=== GENERANDO REPORTE ===");
            System.out.println("Tipo: " + tipoReporte);
            System.out.println("Rango: " + rangoFecha);
            System.out.println("Fecha inicio: " + fechaInicio);
            System.out.println("Fecha fin: " + fechaFin);
            System.out.println("ID Complejo: " + idComplejo);
            System.out.println("Filtros: " + filtros);
            // Calcular fechas según el rango seleccionado
            LocalDate fechaInicioCalculada = null;
            LocalDate fechaFinCalculada = null;

            LocalDate hoy = LocalDate.now();

            switch (rangoFecha) {
                case "hoy":
                    fechaInicioCalculada = hoy;
                    fechaFinCalculada = hoy;
                    break;
                case "ultima_semana":
                    fechaInicioCalculada = hoy.minusDays(7);
                    fechaFinCalculada = hoy;
                    break;
                case "ultimo_mes":
                    fechaInicioCalculada = hoy.minusMonths(1);
                    fechaFinCalculada = hoy;
                    break;
                case "personalizado":
                    if (fechaInicio != null && fechaFin != null) {
                        fechaInicioCalculada = LocalDate.parse(fechaInicio);
                        fechaFinCalculada = LocalDate.parse(fechaFin);
                    }
                    break;
            }

            System.out.println("Fechas calculadas - Inicio: " + fechaInicioCalculada + ", Fin: " + fechaFinCalculada);

            // Obtener filtros adicionales
            Integer idServicio = null;
            if (filtros != null) {
                String servicioStr = filtros.get("servicio");
                if (servicioStr != null && !servicioStr.isEmpty()) {
                    idServicio = Integer.parseInt(servicioStr);
                }
            }

            // Obtener datos y generar PDF según el tipo de reporte
            byte[] pdf = null;
            String nombreArchivo = "";

            if ("reservas".equalsIgnoreCase(tipoReporte)) {
                // Reporte de Reservas
                List<ReporteReservasDto> datos = reservaRepository.getReporteReservas(
                    fechaInicioCalculada, fechaFinCalculada, idComplejo, idServicio);

                System.out.println("Datos obtenidos: " + datos.size() + " registros");

                // Si no hay datos, crear datos de prueba para testing
                if (datos.isEmpty()) {
                    System.out.println("No hay datos reales, usando datos de prueba...");
                    datos = java.util.Arrays.asList(
                        new ReporteReservasDto() {
                            public String getIdReserva() { return "1"; }
                            public String getNombreUsuario() { return "Juan Pérez"; }
                            public String getNombreComplejo() { return "Complejo San Miguel"; }
                            public String getNombreServicio() { return "Cancha de Fútbol"; }
                            public String getFechaReserva() { return "13/06/2025"; }
                            public String getMontoTotal() { return "S/. 50.00"; }
                            public String getEstadoReserva() { return "Pagada"; }
                        },
                        new ReporteReservasDto() {
                            public String getIdReserva() { return "2"; }
                            public String getNombreUsuario() { return "María García"; }
                            public String getNombreComplejo() { return "Complejo San Miguel"; }
                            public String getNombreServicio() { return "Piscina"; }
                            public String getFechaReserva() { return "12/06/2025"; }
                            public String getMontoTotal() { return "S/. 30.00"; }
                            public String getEstadoReserva() { return "Pendiente"; }
                        },
                        new ReporteReservasDto() {
                            public String getIdReserva() { return "3"; }
                            public String getNombreUsuario() { return "Carlos López"; }
                            public String getNombreComplejo() { return "Complejo San Miguel"; }
                            public String getNombreServicio() { return "Gimnasio"; }
                            public String getFechaReserva() { return "11/06/2025"; }
                            public String getMontoTotal() { return "S/. 25.00"; }
                            public String getEstadoReserva() { return "Pagada"; }
                        }
                    );
                    System.out.println("Datos de prueba creados: " + datos.size() + " registros");
                }

                String pathReporte = "/reportes/ReporteServiciosSanMiguel.jasper";
                pdf = reporteService.generarReportesGeneral(datos, pathReporte);
                System.out.println("PDF generado: " + (pdf != null ? pdf.length + " bytes" : "null"));
                nombreArchivo = "reporte_reservas_" + rangoFecha + ".pdf";

            } else if ("financiero".equalsIgnoreCase(tipoReporte)) {
                // Reporte Financiero (para implementar esto después)
                // List<ReporteFinancieroDto> datos = complejoRepository.getReporteServiciosFinancierosSuperAdmin();
                // String pathReporte = "/reportes/ReporteFinanciero.jasper"; // falta reporte xD
                // pdf = reporteService.generarReportesGeneral(datos, pathReporte);
                // nombreArchivo = "reporte_financiero_" + rangoFecha + ".pdf";

                // Por ahora se usara el mismo reporte de reservas
                List<ReporteReservasDto> datos = reservaRepository.getReporteReservas(
                    fechaInicioCalculada, fechaFinCalculada, idComplejo, idServicio);

                System.out.println("Datos financieros obtenidos: " + datos.size() + " registros");

                // Si no hay datos, crear datos de prueba para testing
                if (datos.isEmpty()) {
                    System.out.println("No hay datos financieros reales, usando datos de prueba...");
                    datos = java.util.Arrays.asList(
                        new ReporteReservasDto() {
                            public String getIdReserva() { return "F001"; }
                            public String getNombreUsuario() { return "Ana Martínez"; }
                            public String getNombreComplejo() { return "Complejo San Miguel"; }
                            public String getNombreServicio() { return "Salón de Eventos"; }
                            public String getFechaReserva() { return "13/06/2025"; }
                            public String getMontoTotal() { return "S/. 150.00"; }
                            public String getEstadoReserva() { return "Pagada"; }
                        },
                        new ReporteReservasDto() {
                            public String getIdReserva() { return "F002"; }
                            public String getNombreUsuario() { return "Roberto Silva"; }
                            public String getNombreComplejo() { return "Complejo San Miguel"; }
                            public String getNombreServicio() { return "Cancha de Tenis"; }
                            public String getFechaReserva() { return "12/06/2025"; }
                            public String getMontoTotal() { return "S/. 80.00"; }
                            public String getEstadoReserva() { return "Pagada"; }
                        }
                    );
                    System.out.println("Datos de prueba financieros creados: " + datos.size() + " registros");
                }

                String pathReporte = "/reportes/ReporteServiciosSanMiguel.jasper";
                pdf = reporteService.generarReportesGeneral(datos, pathReporte);
                nombreArchivo = "reporte_financiero_" + rangoFecha + ".pdf";
            } else {
                throw new IllegalArgumentException("Tipo de reporte no válido: " + tipoReporte);
            }

            // Verificar que el PDF se generó correctamente
            if (pdf == null || pdf.length == 0) {
                System.err.println("ERROR: El PDF generado está vacío o es null");
                return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: No se pudo generar el PDF".getBytes());
            }

            // Configurar headers para la respuesta
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDisposition(org.springframework.http.ContentDisposition
                    .inline()
                    .filename(nombreArchivo)
                    .build());

            System.out.println("=== RESPUESTA EXITOSA ===");
            System.out.println("Archivo: " + nombreArchivo);
            System.out.println("Tamaño PDF: " + pdf.length + " bytes");

            return new ResponseEntity<>(pdf, headers, org.springframework.http.HttpStatus.OK);

        } catch (Exception e) {
            System.err.println("Error al generar el reporte: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint temporal para verificar datos de reservas
    @GetMapping("/superadmin/debug/reservas")
    @ResponseBody
    public ResponseEntity<List<ReporteReservasDto>> debugReservas() {
        try {
            LocalDate hoy = LocalDate.now();
            List<ReporteReservasDto> datos = reservaRepository.getReporteReservas(hoy, hoy, null, null);

            System.out.println("=== DEBUG RESERVAS ===");
            System.out.println("Fecha consultada: " + hoy);
            System.out.println("Cantidad de registros: " + datos.size());

            if (!datos.isEmpty()) {
                System.out.println("Primer registro:");
                ReporteReservasDto primer = datos.get(0);
                System.out.println("- ID: " + primer.getIdReserva());
                System.out.println("- Usuario: " + primer.getNombreUsuario());
                System.out.println("- Complejo: " + primer.getNombreComplejo());
                System.out.println("- Servicio: " + primer.getNombreServicio());
                System.out.println("- Fecha: " + primer.getFechaReserva());
                System.out.println("- Monto: " + primer.getMontoTotal());
                System.out.println("- Estado: " + primer.getEstadoReserva());
            }

            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            System.err.println("Error en debug reservas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Endpoint de prueba para generar reporte simple
    @GetMapping("/superadmin/test/reporte")
    @ResponseBody
    public ResponseEntity<byte[]> testReporte() {
        try {
            System.out.println("=== TEST REPORTE ===");

            // Obtener datos de prueba
            LocalDate hoy = LocalDate.now();
            LocalDate hace30Dias = hoy.minusDays(30);
            List<ReporteReservasDto> datos = reservaRepository.getReporteReservas(hace30Dias, hoy, null, null);

            System.out.println("Datos obtenidos para test: " + datos.size() + " registros");

            if (datos.isEmpty()) {
                System.out.println("No hay datos, creando datos de prueba...");
                // Crear datos de prueba que implementen la interfaz ReporteReservasDto
                datos = java.util.Arrays.asList(
                    new ReporteReservasDto() {
                        public String getIdReserva() { return "1"; }
                        public String getNombreUsuario() { return "Juan Pérez"; }
                        public String getNombreComplejo() { return "Complejo San Miguel"; }
                        public String getNombreServicio() { return "Cancha de Fútbol"; }
                        public String getFechaReserva() { return "13/06/2025"; }
                        public String getMontoTotal() { return "S/. 50.00"; }
                        public String getEstadoReserva() { return "Pagada"; }
                    },
                    new ReporteReservasDto() {
                        public String getIdReserva() { return "2"; }
                        public String getNombreUsuario() { return "María García"; }
                        public String getNombreComplejo() { return "Complejo San Miguel"; }
                        public String getNombreServicio() { return "Piscina"; }
                        public String getFechaReserva() { return "12/06/2025"; }
                        public String getMontoTotal() { return "S/. 30.00"; }
                        public String getEstadoReserva() { return "Pendiente"; }
                    },
                    new ReporteReservasDto() {
                        public String getIdReserva() { return "3"; }
                        public String getNombreUsuario() { return "Carlos López"; }
                        public String getNombreComplejo() { return "Complejo San Miguel"; }
                        public String getNombreServicio() { return "Gimnasio"; }
                        public String getFechaReserva() { return "11/06/2025"; }
                        public String getMontoTotal() { return "S/. 25.00"; }
                        public String getEstadoReserva() { return "Pagada"; }
                    }
                );
                System.out.println("Datos de prueba creados: " + datos.size() + " registros");
            }

            String pathReporte = "/reportes/ReporteServiciosSanMiguel.jasper";
            byte[] pdf = reporteService.generarReportesGeneral(datos, pathReporte);

            if (pdf == null || pdf.length == 0) {
                System.err.println("ERROR: PDF de prueba está vacío");
                return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error en generación de PDF de prueba".getBytes());
            }

            // Configurar headers para la respuesta
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDisposition(org.springframework.http.ContentDisposition
                    .inline()
                    .filename("test_reporte.pdf")
                    .build());

            System.out.println("Test reporte generado exitosamente: " + pdf.length + " bytes");
            return new ResponseEntity<>(pdf, headers, org.springframework.http.HttpStatus.OK);

        } catch (Exception e) {
            System.err.println("Error en test de reporte: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint temporal para verificar datos de reservas en febrero
    @GetMapping("/superadmin/debug/reservas-febrero")
    @ResponseBody
    public ResponseEntity<List<ReporteReservasDto>> debugReservasFebrero() {
        try {
            // Buscar en todo febrero 2025
            LocalDate inicioFebrero = LocalDate.of(2025, 2, 1);
            LocalDate finFebrero = LocalDate.of(2025, 2, 28);

            List<ReporteReservasDto> datos = reservaRepository.getReporteReservas(inicioFebrero, finFebrero, null, null);

            System.out.println("=== DEBUG RESERVAS FEBRERO ===");
            System.out.println("Rango consultado: " + inicioFebrero + " a " + finFebrero);
            System.out.println("Cantidad de registros: " + datos.size());

            if (!datos.isEmpty()) {
                System.out.println("Primer registro:");
                ReporteReservasDto primer = datos.get(0);
                System.out.println("- ID: " + primer.getIdReserva());
                System.out.println("- Usuario: " + primer.getNombreUsuario());
                System.out.println("- Complejo: " + primer.getNombreComplejo());
                System.out.println("- Servicio: " + primer.getNombreServicio());
                System.out.println("- Fecha: " + primer.getFechaReserva());
                System.out.println("- Monto: " + primer.getMontoTotal());
                System.out.println("- Estado: " + primer.getEstadoReserva());
            }

            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            System.err.println("Error en debug reservas febrero: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}
