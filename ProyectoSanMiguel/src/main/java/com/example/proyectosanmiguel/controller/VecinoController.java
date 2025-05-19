package com.example.proyectosanmiguel.controller;
import com.example.proyectosanmiguel.repository.InformacionPagoRepository;

import com.example.proyectosanmiguel.dto.*;
import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.format.TextStyle;
import java.util.*;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("/vecino")
public class VecinoController {

    @Autowired
    private ComplejoRepository complejoRepository;
    @Autowired
    private InformacionPagoRepository informacionPagoRepository;

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

    @Autowired
    private CredencialRepository credencialRepository;

    @GetMapping("/misReservas")
    public String mostrarReservasVecino(Model model) {
        String correo = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Credencial> optionalCredencial = credencialRepository.findOptionalByCorreo(correo);

        if (optionalCredencial.isEmpty()) {
            return "redirect:/login";
        }

        Usuario usuario = optionalCredencial.get().getUsuario();
        int idUsuario = usuario.getIdUsuario();

        List<Reserva> reservas = reservaRepository.findByUsuario_IdUsuario(idUsuario);
        List<ReservaCostoDto> reservasConCosto = new ArrayList<>();
        for (Reserva r : reservas) {
            String diaSemana = r.getFecha().getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            diaSemana = diaSemana.substring(0, 1).toUpperCase() + diaSemana.substring(1);
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

    @GetMapping("/listarComplejos")
    public String listarComplejos(Model model) {
        List<Servicio> servicios = servicioRepository.findAll();
        List<ComplejoDeportivo> complejos = complejoRepository.findAll();
        List<InstanciaServicio> instanciaServicios = instanciaServicioRepository.findAll();

        model.addAttribute("complejos", complejos);
        model.addAttribute("instanciaServicios", instanciaServicios);
        model.addAttribute("servicios", servicios);
        return "Vecino/vecino_servicios";
    }

    @GetMapping("/pagos")
    public String mostrarPasarelaPago(Model model) {
        Usuario usuario = obtenerUsuarioAutenticado();

        if (usuario == null) {
            return "redirect:/login";
        }

        List<Reserva> reservasPendientes = reservaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .stream()
                .filter(r -> r.getEstado() == 0 && r.getInformacionPago() != null
                        && "Pendiente".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .toList();

        double total = reservasPendientes.stream()
                .mapToDouble(r -> r.getInformacionPago() != null ? r.getInformacionPago().getTotal().doubleValue() : 0.0)
                .sum();

        // Asegurarse de evitar el error si falta credencial
        Credencial credencial = credencialRepository.findByCorreo(usuario.getCredencial().getCorreo());

        model.addAttribute("usuario", usuario);
        model.addAttribute("credencial", credencial); // Agregarlo si lo usas en el HTML
        model.addAttribute("reservasPendientes", reservasPendientes);
        model.addAttribute("totalReservas", total);

        return "Vecino/vecino_pagos";
    }

    private Usuario obtenerUsuarioAutenticado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String correo;
        if (principal instanceof UserDetails) {
            correo = ((UserDetails) principal).getUsername();
        } else {
            correo = principal.toString();
        }

        System.out.println("Correo autenticado: " + correo);

        Credencial credencial = credencialRepository.findByCorreo(correo);
        if (credencial == null) {
            System.out.println("No se encontró credencial para el correo");
            return null;
        }
        System.out.println("Usuario encontrado: " + credencial.getUsuario().getNombre());
        return credencial.getUsuario();
    }



    @GetMapping("/crearReserva")
    public String crearReserva(@RequestParam("idInstancia") Integer idInstancia, Model model) {
        Optional<InstanciaServicio> instancia = instanciaServicioRepository.findById(idInstancia);

        if (instancia.isPresent()) {
            Reserva reserva = new Reserva();
            reserva.setInstanciaServicio(instancia.get());
            reserva.setFecha(LocalDate.now());

            String correo = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<Credencial> optionalCredencial = credencialRepository.findOptionalByCorreo(correo);
            Usuario usuario = optionalCredencial.map(Credencial::getUsuario).orElse(new Usuario());
            if (usuario.getCredencial() == null) {
                usuario.setCredencial(new Credencial());
            }

            List<InstanciaServicio> instanciaServicios =
                    instanciaServicioRepository.findInstanciaServicioByComplejoDeportivo(
                            instancia.get().getComplejoDeportivo().getIdComplejoDeportivo());

            reserva.setUsuario(usuario);
            model.addAttribute("reserva", reserva);
            model.addAttribute("instanciaServicios", instanciaServicios);
            model.addAttribute("complejo", instancia.get().getComplejoDeportivo()); // ✅ Agregado

            return "Vecino/vecino_formulario_complejo";
        } else {
            return "redirect:/vecino/listarComplejos";
        }
    }


    @PostMapping("/guardarReserva")
    public String guardarReserva(@ModelAttribute Reserva reserva,
                                 @RequestParam("bloqueHorario") String bloqueHorario,
                                 RedirectAttributes redirectAttributes) {

        String[] partes = bloqueHorario.split("-");
        LocalTime horaInicio = LocalTime.parse(partes[0].trim());
        LocalTime horaFin = LocalTime.parse(partes[1].trim());

        reserva.setHoraInicio(horaInicio);
        reserva.setHoraFin(horaFin);
        reserva.setFechaHoraRegistro(LocalDateTime.now());
        reserva.setEstado(0); // 0 = Pendiente

        // 👉 Asignar el usuario (por ahora, fijo)
        String correo = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Credencial> optionalCredencial = credencialRepository.findOptionalByCorreo(correo);
        Usuario usuario = optionalCredencial
                .map(Credencial::getUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        reserva.setUsuario(usuario);


        // ⚠️ Cargar instanciaServicio desde BD
        Optional<InstanciaServicio> instanciaOpt = instanciaServicioRepository
                .findById(reserva.getInstanciaServicio().getIdInstanciaServicio());

        if (instanciaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se encontró la instancia seleccionada.");
            return "redirect:/vecino/listarComplejos";
        }

        InstanciaServicio instancia = instanciaOpt.get();
        reserva.setInstanciaServicio(instancia);

        // Buscar la tarifa correspondiente
        Tarifa tarifa = tarifaRepository.findAll().stream()
                .filter(t -> t.getServicio().getIdServicio().equals(instancia.getServicio().getIdServicio()))
                .filter(t -> t.getDiaSemana().equalsIgnoreCase(obtenerDia(reserva.getFecha())))
                .filter(t -> !t.getHoraInicio().isAfter(horaInicio) && !t.getHoraFin().isBefore(horaFin))
                .findFirst()
                .orElse(null);

        if (tarifa == null) {
            redirectAttributes.addFlashAttribute("error", "No se encontró una tarifa válida para el horario seleccionado.");
            return "redirect:/vecino/crearReserva?idInstancia=" + instancia.getIdInstanciaServicio();
        }

        // Crear InformacionPago
        InformacionPago pago = new InformacionPago();
        pago.setFecha(LocalDate.now());
        pago.setHora(LocalTime.now());
        pago.setTipo("Pendiente");
        pago.setTotal(tarifa.getMonto());
        pago.setEstado("Pendiente");
        informacionPagoRepository.save(pago);

        reserva.setInformacionPago(pago);
        reservaRepository.save(reserva);

        return "redirect:/vecino/misReservas";
    }

    private String obtenerDia(LocalDate fecha) {
        return fecha.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
    }



}
