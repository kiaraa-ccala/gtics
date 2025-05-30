package com.example.proyectosanmiguel.controller;
import com.example.proyectosanmiguel.repository.InformacionPagoRepository;
import java.math.BigDecimal;

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
    private DescuentoRepository descuentoRepository;

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
    public String mostrarPasarelaPago(Model model, @RequestParam(value = "codigoCupon", required = false) String codigoCupon) {
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
                .mapToDouble(r -> r.getInformacionPago().getTotal().doubleValue())
                .sum();

        double descuentoAplicado = 0;
        double totalConDescuento = total;

        if (codigoCupon != null && !codigoCupon.isEmpty()) {
            // Buscar una reserva donde el cupón sea válido
            for (Reserva reserva : reservasPendientes) {
                Map<String, Object> descuentoData = aplicarDescuento(codigoCupon, reserva.getIdReserva());

                if ((boolean) descuentoData.get("valido")) {
                    descuentoAplicado = (double) descuentoData.get("descuento");
                    totalConDescuento = total - descuentoAplicado;

                    model.addAttribute("descuento", descuentoAplicado);
                    model.addAttribute("totalConDescuento", totalConDescuento);
                    model.addAttribute("mensajeDescuento", descuentoData.get("mensaje"));
                    break; // ✅ Aplicar solo una vez al primer válido
                }
            }

            // Si no se aplicó a ninguna reserva
            if (descuentoAplicado == 0) {
                model.addAttribute("mensajeDescuento", "El cupón no aplica a ninguno de los servicios.");
            } else {
                total = totalConDescuento;
            }
        }

        Credencial credencial = credencialRepository.findByCorreo(usuario.getCredencial().getCorreo());

        model.addAttribute("usuario", usuario);
        model.addAttribute("credencial", credencial);
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

    @PostMapping("/guardarPago")
    public String guardarPago(@RequestParam("idReserva") Integer idReserva,
                              @RequestParam("codigoCupon") String codigoCupon,
                              RedirectAttributes redirectAttributes) {

        Optional<Reserva> optReserva = reservaRepository.findById(idReserva);
        if (optReserva.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se encontró la reserva.");
            return "redirect:/vecino/pagos";
        }

        Reserva reserva = optReserva.get();

        if (reserva.getInformacionPago() == null) {
            redirectAttributes.addFlashAttribute("error", "La reserva no tiene información de pago asociada.");
            return "redirect:/vecino/pagos";
        }

        double total = reserva.getInformacionPago().getTotal().doubleValue();

        // Usamos aplicarDescuento que solo necesita el ID de la reserva
        Map<String, Object> descuentoData = aplicarDescuento(codigoCupon, idReserva);

        if ((boolean) descuentoData.get("valido")) {
            double descuentoAplicado = (double) descuentoData.get("descuento");
            double totalConDescuento = (double) descuentoData.get("totalConDescuento");

            InformacionPago pago = reserva.getInformacionPago();
            pago.setTotal(totalConDescuento);
            pago.setEstado("Pagado");

            informacionPagoRepository.save(pago);

            reserva.setEstado(1); // Pagado
            reservaRepository.save(reserva);

            redirectAttributes.addFlashAttribute("mensajeExito", "Pago realizado correctamente.");
            return "redirect:/vecino/misReservas";
        } else {
            redirectAttributes.addFlashAttribute("error", "El cupón no es válido.");
            return "redirect:/vecino/pagos";
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

        String correo = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Credencial> optionalCredencial = credencialRepository.findOptionalByCorreo(correo);
        Usuario usuario = optionalCredencial
                .map(Credencial::getUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        reserva.setUsuario(usuario);


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


    @PostMapping("/aplicarDescuento")
    @ResponseBody
    public Map<String, Object> aplicarDescuento(@RequestParam("codigo") String codigo,
                                                @RequestParam("idReserva") Integer idReserva) {
        Map<String, Object> resp = new HashMap<>();

        // Verificar que el parámetro idReserva no sea null
        if (idReserva == null) {
            resp.put("valido", false);
            resp.put("mensaje", "El ID de reserva no puede ser nulo.");
            return resp;
        }

        LocalDate hoy = LocalDate.now();

        // Buscar el descuento en la base de datos
        Optional<Descuento> descOpt = descuentoRepository.findByCodigoAndFechaInicioLessThanEqualAndFechaFinalGreaterThanEqual(
                codigo.trim(), hoy, hoy);

        if (descOpt.isEmpty()) {
            resp.put("valido", false);
            resp.put("mensaje", "Código de descuento inválido o expirado.");
            return resp;
        }

        Descuento descuento = descOpt.get();

        // Obtener la reserva desde idReserva
        Optional<Reserva> reservaOpt = reservaRepository.findById(idReserva);
        if (reservaOpt.isEmpty()) {
            resp.put("valido", false);
            resp.put("mensaje", "Reserva no encontrada.");
            return resp;
        }

        Reserva reserva = reservaOpt.get();

        // Obtener idServicio desde la InstanciaServicio de la reserva
        Integer idServicioReserva = reserva.getInstanciaServicio().getServicio().getIdServicio();

        // Verificar si el descuento aplica para el servicio de la reserva
        if (!descuento.getServicio().getIdServicio().equals(idServicioReserva)) {
            resp.put("valido", false);
            resp.put("mensaje", "El descuento no aplica para este servicio.");
            return resp;
        }

        // Obtener el monto de la tarifa
        Optional<Tarifa> tarifaOpt = tarifaRepository.findTarifaAplicable(
                reserva.getInstanciaServicio().getServicio().getIdServicio(),
                reserva.getFecha().getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")),
                reserva.getHoraInicio(),
                reserva.getHoraFin());

        if (tarifaOpt.isEmpty()) {
            resp.put("valido", false);
            resp.put("mensaje", "No se encontró tarifa para el servicio en este horario.");
            return resp;
        }

        Tarifa tarifa = tarifaOpt.get();  // Obtener la Tarifa

        double total = tarifa.getMonto(); // Monto de la tarifa

        // Calcular el descuento
        double descuentoAplicado = 0.0;
        if ("PORCENTAJE".equalsIgnoreCase(descuento.getTipoDescuento())) {
            descuentoAplicado = total * (descuento.getValor() / 100.0);
        } else if ("FIJO".equalsIgnoreCase(descuento.getTipoDescuento())) {
            descuentoAplicado = descuento.getValor();
        }

        // Calcular el total con descuento
        double totalConDescuento = Math.max(0, total - descuentoAplicado);

        resp.put("valido", true);
        resp.put("descuento", descuentoAplicado);
        resp.put("totalConDescuento", totalConDescuento);
        resp.put("mensaje", "Descuento aplicado correctamente.");

        return resp;
    }


}
