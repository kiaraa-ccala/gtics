package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/")
public class LandingController {

    @Autowired
    private ComplejoRepository complejoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private InstanciaServicioRepository instanciaServicioRepository;

    /**
     * Página principal del landing (Home page)
     */
    @GetMapping("")
    public String home(Model model) {
        return index(model);
    }

    /**
     * Página principal del landing (index)
     */
    @GetMapping("index")
    public String index(Model model) {
        try {
            // Obtener información para mostrar en el landing
            List<ComplejoDeportivo> complejos = complejoRepository.findAll();
            List<Servicio> servicios = servicioRepository.findAll();
            List<Sector> sectores = sectorRepository.findAll();

            // Agregar datos al modelo para mostrar en la vista
            model.addAttribute("complejos", complejos);
            model.addAttribute("servicios", servicios);
            model.addAttribute("sectores", sectores);

            // Contar estadísticas para mostrar
            long totalComplejos = complejos.size();
            long totalServicios = servicios.size();
            long totalInstancias = instanciaServicioRepository.count();

            model.addAttribute("totalComplejos", totalComplejos);
            model.addAttribute("totalServicios", totalServicios);
            model.addAttribute("totalInstancias", totalInstancias);

            return "index";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar los datos del portal");
            return "index";
        }
    }

    /**
     * Página de información sobre los servicios
     */
    @GetMapping("servicios")
    public String servicios(Model model) {
        try {
            List<Servicio> servicios = servicioRepository.findAll();
            List<ComplejoDeportivo> complejos = complejoRepository.findAll();

            model.addAttribute("servicios", servicios);
            model.addAttribute("complejos", complejos);

            return "landing/servicios";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar los servicios");
            return "redirect:/";
        }
    }    /**
     * Página de información sobre los complejos deportivos
     */
    @GetMapping("complejos-deportivos")
    public String complejosDeportivos(Model model) {
        try {
            List<ComplejoDeportivo> complejos = complejoRepository.findAll();
            List<Sector> sectores = sectorRepository.findAll();

            model.addAttribute("complejos", complejos);
            model.addAttribute("sectores", sectores);

            return "landing/complejos";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar los complejos deportivos");
            return "redirect:/";
        }
    }

    /**
     * Página de contacto
     */
    @GetMapping("contacto")
    public String contacto(Model model) {
        return "landing/contacto";
    }

    /**
     * Página sobre nosotros
     */
    @GetMapping("nosotros")
    public String nosotros(Model model) {
        return "landing/nosotros";
    }    /**
     * Redirección a login para acceder a reservas
     */
    @GetMapping("reservas")
    public String reservas() {
        return "redirect:/inicio?message=Debes iniciar sesión para acceder a las reservas";
    }

    /**
     * Página de términos y condiciones
     */
    @GetMapping("terminos")
    public String terminos(Model model) {
        return "landing/terminos";
    }

    /**
     * Página de política de privacidad
     */
    @GetMapping("privacidad")
    public String privacidad(Model model) {
        return "landing/privacidad";
    }
}
