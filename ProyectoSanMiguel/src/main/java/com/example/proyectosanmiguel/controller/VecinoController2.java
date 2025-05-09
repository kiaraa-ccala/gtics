package com.example.proyectosanmiguel.controller;
import com.example.proyectosanmiguel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vecino")
public class VecinoController2 {

    @Autowired
    private ComplejoRepository complejoRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FotoRepository fotoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @GetMapping("/crearReserva")
    public String crearReserva(Model model) {

        return "Vecino/vecino_formulario_complejo";
    }

}

