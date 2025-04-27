package com.example.proyectosanmiguel.controller;
import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.RolRepository;
import com.example.proyectosanmiguel.repository.SectorRepository;
import com.example.proyectosanmiguel.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class SuperAdminController {


    final UsuarioRepository usuarioRepository;
    final RolRepository rolRepository;
    final SectorRepository sectorRepository;

    public SuperAdminController(UsuarioRepository usuarioRepository, RolRepository rolRepository, SectorRepository sectorRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.sectorRepository = sectorRepository;
    }

    //Lista de Usuarios

    @GetMapping({"/superadmin", "/superadmin/", "/superadmin/usuarios/lista"})
    public String mostrarListaUsuarios(Model model) {

        List<Usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("listaUsuarios", usuarios);

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

    //Guardar los datos en el formulario
    @PostMapping("/superadmin/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, @RequestParam("correo") String email, @RequestParam("password") String password, @RequestParam("sector") Integer idSector) {

        Sector sector = new Sector();
        sector.setIdSector(idSector);
        usuario.setSector(sector);

        Credencial credencial = new Credencial();
        credencial.setCorreo(email);
        credencial.setPassword(password);
        credencial.setUsuario(usuario);
        usuario.setCredencial(credencial);

        usuarioRepository.save(usuario);
        return "redirect:/superadmin/usuarios/lista";
    }

    //Borrar un usuario

    @GetMapping("/superadmin/eliminar")
    public String eliminarAuto(@RequestParam("idUsuario") int idUsuario) {

        Optional<Usuario> usuario = usuarioRepository.findById(idUsuario);

        if(usuario.isPresent()) {
            usuarioRepository.deleteById(idUsuario);
        }

        return "redirect:/superadmin/usuarios/lista";
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

    // "/superadmin/reportes/servicios"

    @GetMapping("/superadmin/reportes/servicios")
    public String reportesServicios() {

        return "SuperAdmin/superadmin_reporteServicios";
    }

    // "/superadmin/reportes/financiero"

    @GetMapping("/superadmin/reportes/financiero")
    public String reportesFinanciero() {

        return "SuperAdmin/superadmin_reporteFinanciero";
    }

    // "/superadmin/asistencia"

    @GetMapping("/superadmin/asistencia")
    public String superadminAsistencia() {

        return "SuperAdmin/superadmin_asistencia";
    }

}
