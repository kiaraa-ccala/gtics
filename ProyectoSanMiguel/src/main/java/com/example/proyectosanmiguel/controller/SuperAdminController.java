package com.example.proyectosanmiguel.controller;
import com.example.proyectosanmiguel.dto.AsistenciaSemanalDto;
import com.example.proyectosanmiguel.dto.ComparativaAsistenciaDto;
import com.example.proyectosanmiguel.dto.EstadisticasPersonalDto;
import com.example.proyectosanmiguel.dto.IngresosMensualesDto;
import com.example.proyectosanmiguel.dto.ReservasMensualesDto;
import com.example.proyectosanmiguel.dto.IngresosDetalleMesDto;
import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.ComplejoRepository;
import com.example.proyectosanmiguel.repository.RolRepository;
import com.example.proyectosanmiguel.repository.SectorRepository;
import com.example.proyectosanmiguel.repository.UsuarioRepository;
import com.example.proyectosanmiguel.repository.FinanzasRepository;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FinanzasRepository finanzasRepository;

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

    // --- API: Ingresos mensuales para un mes específico ---
    @GetMapping("/superadmin/api/finanzas/ingresos-mensuales")
    @ResponseBody
    public ResponseEntity<IngresosMensualesDto> getIngresosMensuales(@RequestParam("mes") int mes) {
        Double ingresos = finanzasRepository.obtenerIngresosMensuales(mes);
        if (ingresos == null) ingresos = 0.0;
        return ResponseEntity.ok(new IngresosMensualesDto(mes, ingresos));
    }

    // --- API: Reservas creadas y totales para un mes específico ---
    @GetMapping("/superadmin/api/finanzas/reservas-mensuales")
    @ResponseBody
    public ResponseEntity<ReservasMensualesDto> getReservasMensuales(@RequestParam("mes") int mes) {
        Object[] result = finanzasRepository.obtenerReservasMensuales(mes);
        long totales = 0;
        if (result != null && result.length == 1) {
            totales = result[0] != null ? ((Number) result[0]).longValue() : 0;
        }
        return ResponseEntity.ok(new ReservasMensualesDto(mes,totales));
    }

    // --- API: Ingresos totales últimos 6 meses (actual + 5 anteriores) ---
    @GetMapping("/superadmin/api/finanzas/ingresos-ultimos-meses")
    @ResponseBody
    public ResponseEntity<List<IngresosDetalleMesDto>> getIngresosUltimosMeses() {
        List<Object[]> lista = finanzasRepository.obtenerIngresosUltimosMeses();
        List<IngresosDetalleMesDto> datos = new ArrayList<>();
        for (Object[] fila : lista) {
            int anio = fila[0] != null ? ((Number) fila[0]).intValue() : 0;
            int mes = fila[1] != null ? ((Number) fila[1]).intValue() : 0;
            String nombreMes = fila[2] != null ? fila[2].toString() : "";
            double tarjeta = fila[3] != null ? ((Number) fila[3]).doubleValue() : 0.0;
            double transferencia = fila[4] != null ? ((Number) fila[4]).doubleValue() : 0.0;
            double total = fila[5] != null ? ((Number) fila[5]).doubleValue() : 0.0;
            long transacciones = fila[6] != null ? ((Number) fila[6]).longValue() : 0;
            datos.add(new IngresosDetalleMesDto(anio, mes, nombreMes, tarjeta, transferencia, total, transacciones));
        }
        return ResponseEntity.ok(datos);
    }

}
