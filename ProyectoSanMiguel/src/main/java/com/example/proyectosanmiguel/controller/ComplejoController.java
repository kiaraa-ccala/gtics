package com.example.proyectosanmiguel.controller;
import com.example.proyectosanmiguel.entity.ComplejoDeportivo;
import com.example.proyectosanmiguel.repository.ComplejoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ComplejoController {

    //Declaracion del repositorio como atributo final

    final ComplejoRepository complejoRepository;

    public ComplejoController(ComplejoRepository complejoRepository) {
        this.complejoRepository = complejoRepository;
    }

    //Respuesta para listar complejos
    @GetMapping("/complejos")
    public String mostrarVista() {
        return "ExampleBase/listaComplejos"; // Spring buscará "complejos.html" en /templates
    }

}
