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

    @GetMapping({"/SuperAdmin", "/SuperAdmin/", "/SuperAdmin/ListarUsuario"})
    public String mostrarListaUsuarios(Model model) {

        List<Usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("listaUsuarios", usuarios);

        return "SuperAdmin/superadmin_listaUsuarios";
    }

    //Formulario de creacion de Usuarios

    @GetMapping({"/SuperAdmin/CrearUsuario"})
    public String formularioCreacionUsuario(Model model) {

        List<Sector> sectores = sectorRepository.findAll();
        List<Rol> roles = rolRepository.findAll();

        model.addAttribute("sectores", sectores);
        model.addAttribute("roles", roles);

        return "SuperAdmin/superadmin_agregarUsuarios";
    }

    //Guardar los datos en el formulario
    @PostMapping("/SuperAdmin/GuardarUsuario")
    public String guardarUsuario(@ModelAttribute Usuario usuario, @RequestParam("correo") String email, @RequestParam("password") String password, @RequestParam("sector") Integer idSector) {

        System.out.println("Received usuario: " + usuario);
        System.out.println("Nombre: " + usuario.getNombre());
        System.out.println("Apellido: " + usuario.getApellido());
        System.out.println("DNI: " + usuario.getDni());

        Sector sector = new Sector();
        sector.setIdSector(idSector);
        usuario.setSector(sector);

        Credencial credencial = new Credencial();
        credencial.setCorreo(email);
        credencial.setPassword(password);
        credencial.setUsuario(usuario);
        usuario.setCredencial(credencial);

        usuarioRepository.save(usuario);
        return "redirect:/SuperAdmin/ListarUsuario";
    }

    //Borrar un usuario

    @GetMapping("/SuperAdmin/eliminar")
    public String eliminarAuto(@RequestParam("idUsuario") int idUsuario) {

        Optional<Usuario> usuario = usuarioRepository.findById(idUsuario);

        if(usuario.isPresent()) {
            usuarioRepository.deleteById(idUsuario);
        }

        return "redirect:/autos/listar";
    }

}
