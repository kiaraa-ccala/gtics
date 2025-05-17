package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.dto.*;
import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.format.TextStyle;
import java.util.Locale;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.*;


@Controller
@RequestMapping("/vecino")
public class VecinoController {

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

    @Autowired
    private InstanciaServicioRepository instanciaServicioRepository;

    @GetMapping("/misReservas")
    public String mostrarReservasVecino(Model model) {
        int idUsuario = 8; // O el ID que uses para pruebas

        List<Reserva> reservas = reservaRepository.findByUsuario_IdUsuario(idUsuario);
        List<ReservaCostoDto> reservasConCosto = new ArrayList<>();

        for (Reserva r : reservas) {
            String diaSemana = r.getFecha().getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            diaSemana = diaSemana.substring(0, 1).toUpperCase() + diaSemana.substring(1); // Ej: "Viernes"

            Optional<Tarifa> tarifa = tarifaRepository.findTarifaAplicable(
                    r.getInstanciaServicio().getServicio().getIdServicio(),
                    diaSemana,
                    r.getHoraInicio(),
                    r.getHoraFin()
            );

            double costo = tarifa.map(Tarifa::getMonto).orElse(0.0);
            reservasConCosto.add(new ReservaCostoDto(r, costo));
        }

        model.addAttribute("listaReservas", reservasConCosto);
        return "Vecino/vecino_misReservas";
    }


    @GetMapping("/reserva/eliminar/{id}")
    public String eliminarReserva(@PathVariable("id") Integer id, RedirectAttributes attr) {
        Optional<Reserva> opt = reservaRepository.findById(id);
        if (opt.isPresent()) {
            reservaRepository.deleteById(id);
            attr.addFlashAttribute("msg", "Reserva cancelada correctamente.");
        } else {
            attr.addFlashAttribute("error", "No se encontró la reserva.");
        }
        return "redirect:/vecino/misReservas";
    }
    @GetMapping("/test")
    public String mostrarVista() {
        return "/Vecino/vecino_servicios";  // Verifica que esta vista exista
    }

    @GetMapping("/listarComplejos")
    public String listarComplejos(Model model) {
        List<Servicio> servicios = servicioRepository.findAll();  // Obtener todos los servicios
        List<ComplejoDeportivo> complejos = complejoRepository.findAll();  // Obtener todos los complejos
        List<InstanciaServicio> instanciaServicios = instanciaServicioRepository.findAll();  // Obtener todas las instancias de servicio
        model.addAttribute("complejos", complejos);  // Pasar la lista de complejos a la vista
        model.addAttribute("instanciaServicios", instanciaServicios);  // Pasar la lista de instancias de servicio a la vista
        model.addAttribute("servicios", servicios);  // Pasar la lista de servicios a la vista
        return "Vecino/vecino_servicios";  // Asegúrate de que esta sea la vista correcta
    }

    @GetMapping("/crearReserva")
    public String crearReserva(Model model, @ModelAttribute("reserva") Reserva reserva) {

        List<Servicio> servicios = servicioRepository.findAll();

        return "Vecino/vecino_formulario_complejo";
    }

    @GetMapping("/guardarReserva")
    public String guardarReserva(@ModelAttribute Reserva reserva) {

        return "Vecino/vecino_formulario_complejo";
    }
}
