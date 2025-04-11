package com.example.proyectosanmiguel.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExampleController {

    @GetMapping("/complejos")
    public String mostrarVista() {
        return "ExampleBase/listaComplejos"; // Spring buscará "complejos.html" en /templates
    }

}
