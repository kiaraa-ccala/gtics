package com.example.proyectosanmiguel.controller;
import com.example.proyectosanmiguel.entity.ComplejoDeportivo;
import com.example.proyectosanmiguel.entity.Usuario;
import com.example.proyectosanmiguel.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class SuperAdminController {


    final UsuarioRepository usuarioRepository;

    public SuperAdminController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    //Lista de Usuarios

    @GetMapping({"/SuperAdmin", "/"})
    public String mostrarListaUsuarios(Model model) {

        List<Usuario> listaUsuarios = usuarioRepository.findAll();

        model.addAttribute("listaUsuarios", listaUsuarios);

        return "SuperAdmin/superadmin_reporteFinanciero";
    }

}
