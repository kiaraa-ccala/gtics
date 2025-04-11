package com.example.proyectosanmiguel.controller;
import com.example.proyectosanmiguel.entity.ComplejoDeportivo;
import com.example.proyectosanmiguel.entity.Sector;
import com.example.proyectosanmiguel.repository.ComplejoRepository;
import com.example.proyectosanmiguel.repository.SectorRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class ComplejoController {

    //Declaracion del repositorio como atributo final

    final ComplejoRepository complejoRepository;

    //Declaracion del repositorio del sector como atributo final
    final SectorRepository sectorRepository;


    //Declaracion del constructor
    public ComplejoController(ComplejoRepository complejoRepository, SectorRepository sectorRepository) {
        this.complejoRepository = complejoRepository;
        this.sectorRepository = sectorRepository;
    }

    //Respuesta para listar complejos
    @GetMapping("/complejos")
    public String mostrarListaComplejosDeportivos(Model model) {

        List<ComplejoDeportivo> complejos = complejoRepository.findAll();
        model.addAttribute("complejos", complejos);

        return "ExampleBase/listaComplejos"; // Spring buscará "complejos.html" en /templates
    }

    @GetMapping("/complejos/new")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("complejo", new ComplejoDeportivo());
        model.addAttribute("sectorList", sectorRepository.findAll());
        return "ExampleBase/crearComplejo";
    }

    @PostMapping("/complejos/save")
    public String guardarComplejo(@ModelAttribute("complejo") ComplejoDeportivo complejo) {
        complejoRepository.save(complejo);
        return "redirect:/complejos";
    }

    @GetMapping("/complejos/delete")
    public String eliminarComplejo(@RequestParam("id") int id) {
        complejoRepository.deleteById(id);
        return "redirect:/complejos";
    }

}
