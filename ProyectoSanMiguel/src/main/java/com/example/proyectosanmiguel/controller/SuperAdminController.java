package com.example.proyectosanmiguel.controller;
import com.example.proyectosanmiguel.dto.AsistenciaSemanalDto;
import com.example.proyectosanmiguel.dto.ComparativaAsistenciaDto;
import com.example.proyectosanmiguel.dto.EstadisticasPersonalDto;
import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.ComplejoRepository;
import com.example.proyectosanmiguel.repository.RolRepository;
import com.example.proyectosanmiguel.repository.SectorRepository;
import com.example.proyectosanmiguel.repository.UsuarioRepository;
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

    // ENDPOINTS AJAX PARA ESTADÍSTICAS PERSONALES

    @GetMapping("/superadmin/api/estadisticas/personal")
    @ResponseBody
    public ResponseEntity<EstadisticasPersonalDto> getEstadisticasPersonal() {
        try {
            LocalDate hoy = LocalDate.now();
            
            Long totalPersonal = usuarioRepository.countPersonalActivo();
            Long nuevoPersonal = usuarioRepository.countNuevoPersonal();
            Long personalConHorario = usuarioRepository.countPersonalConHorarioHoy(hoy);
            Long personalPresente = usuarioRepository.countPersonalPresenteHoy(hoy);
            
            Long personalAusente = personalConHorario - personalPresente;
            
            Double porcentajeATiempo = personalConHorario > 0 ? 
                (personalPresente.doubleValue() / personalConHorario.doubleValue()) * 100 : 0.0;
            Double porcentajeNuevo = totalPersonal > 0 ? 
                (nuevoPersonal.doubleValue() / totalPersonal.doubleValue()) * 100 : 0.0;
            Double porcentajeAusente = personalConHorario > 0 ? 
                (personalAusente.doubleValue() / personalConHorario.doubleValue()) * 100 : 0.0;
              EstadisticasPersonalDto estadisticas = new EstadisticasPersonalDto();
            estadisticas.setTotalPersonal(totalPersonal.intValue());
            estadisticas.setPersonalATiempo(personalPresente.intValue());
            estadisticas.setNuevoPersonal(nuevoPersonal.intValue());
            estadisticas.setPersonalAusente(personalAusente.intValue());
            estadisticas.setPorcentajeATiempo(porcentajeATiempo);
            estadisticas.setPorcentajeNuevo(porcentajeNuevo);
            estadisticas.setPorcentajeAusente(porcentajeAusente);
            
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
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
                int diaIndex = ((Number) resultado[0]).intValue() - 2; // MySQL DAYOFWEEK empieza en 1 (Domingo)
                if (diaIndex < 0) diaIndex = 6; // Domingo al final
                
                asistencias.set(diaIndex, ((Number) resultado[1]).intValue());
                tardanzas.set(diaIndex, ((Number) resultado[2]).intValue());
                ausencias.set(diaIndex, ((Number) resultado[3]).intValue());
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
            LocalDate inicioMeses = hoy.minusMonths(5).withDayOfMonth(1);
            
            List<Object[]> resultados = usuarioRepository.getPorcentajeAsistenciaMensual(inicioMeses);
            
            List<String> meses = new ArrayList<>();
            List<Double> porcentajes = new ArrayList<>();
            
            for (Object[] resultado : resultados) {
                int mes = ((Number) resultado[0]).intValue();
                int año = ((Number) resultado[1]).intValue();
                Double porcentaje = ((Number) resultado[2]).doubleValue();
                
                String nombreMes = LocalDate.of(año, mes, 1)
                    .getMonth()
                    .getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
                    
                meses.add(nombreMes);
                porcentajes.add(porcentaje);
            }
            
            // Calcular cambio porcentual
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

}
