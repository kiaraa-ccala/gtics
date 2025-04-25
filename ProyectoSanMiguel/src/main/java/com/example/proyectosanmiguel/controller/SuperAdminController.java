package com.example.proyectosanmiguel.controller;
import com.example.proyectosanmiguel.entity.ComplejoDeportivo;
import com.example.proyectosanmiguel.entity.Usuario;
import com.example.proyectosanmiguel.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class SuperAdminController {


    final UsuarioRepository usuarioRepository;

    public SuperAdminController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    //Lista de Usuarios

    @GetMapping({"/SuperAdmin", "/SuperAdmin/", "/SuperAdmin/ListarUsuario"})
    public String mostrarListaUsuarios(Model model) {

        List<Usuario> listaUsuarios = usuarioRepository.findAll();

        model.addAttribute("listaUsuarios", listaUsuarios);

        return "SuperAdmin/superadmin_listaUsuarios";
    }

    //Formulario de creacion de Usuarios

    @GetMapping({"/SuperAdmin/CrearUsuario"})
    public String formularioCreacionUsuario(Model model) {


        return "SuperAdmin/superadmin_agregarUsuarios";
    }

    //Guardar los datos en el formulario
    @PostMapping("/SuperAdmin/GuardarUsuario")
    public String guardarUsuario(Usuario usuario) {

        usuarioRepository.save(usuario);
        return "redirect:/SuperAdmin/ListarUsuario";
    }

    //Borrar un usuario

    @GetMapping("/autos/eliminar")
    public String eliminarAuto(@RequestParam("idUsuario") int idUsuario) {

        Optional<Usuario> usuario = usuarioRepository.findById(idUsuario);

        if(usuario.isPresent()) {
            usuarioRepository.deleteById(idUsuario);
        }

        return "redirect:/autos/listar";
    }

}
