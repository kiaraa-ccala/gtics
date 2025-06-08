package com.example.proyectosanmiguel.controller;
import com.example.proyectosanmiguel.repository.InformacionPagoRepository;
import java.math.BigDecimal;
import java.util.stream.Collectors;

import com.example.proyectosanmiguel.dto.*;
import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import java.util.stream.Collectors;
import java.util.stream.Collectors;

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
        double totalConDescuento = total;        if (codigoCupon != null && !codigoCupon.isEmpty()) {
            // Validar el cupón solo una vez
            Map<String, Object> descuentoData = validarCupon(codigoCupon, reservasPendientes.get(0).getIdReserva());

            if ((boolean) descuentoData.get("valido")) {
                descuentoAplicado = (double) descuentoData.get("descuento");
                totalConDescuento = total - descuentoAplicado;

                model.addAttribute("descuento", descuentoAplicado);
                model.addAttribute("totalConDescuento", totalConDescuento);
                model.addAttribute("mensajeDescuento", descuentoData.get("mensaje"));
            } else {
                model.addAttribute("mensajeDescuento", "El cupón no aplica a ninguno de los servicios.");
            }
        }

        Credencial credencial = credencialRepository.findByCorreo(usuario.getCredencial().getCorreo());

        model.addAttribute("usuario", usuario);
        model.addAttribute("credencial", credencial);
        model.addAttribute("reservasPendientes", reservasPendientes);
        model.addAttribute("totalReservas", totalConDescuento);

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
    }    @PostMapping("/guardarPago")
    public String guardarPago(@RequestParam("idReserva") Integer idReserva,
                              @RequestParam(value = "codigoCupon", required = false) String codigoCupon,
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
        
        // Verificamos que la reserva esté pendiente
        if (reserva.getEstado() != 0) {
            redirectAttributes.addFlashAttribute("error", "La reserva no está pendiente de pago.");
            return "redirect:/vecino/pagos";        }        InformacionPago pago = reserva.getInformacionPago();
        BigDecimal totalFinal = BigDecimal.valueOf(pago.getTotal());        // Si hay cupón, aplicar descuento
        if (codigoCupon != null && !codigoCupon.trim().isEmpty()) {
            Map<String, Object> descuentoData = validarCupon(codigoCupon, idReserva);
            
            if ((boolean) descuentoData.get("valido")) {
                totalFinal = (BigDecimal) descuentoData.get("totalConDescuento");
                redirectAttributes.addFlashAttribute("mensajeDescuento", 
                    "Descuento de S/." + String.format("%.2f", ((BigDecimal) descuentoData.get("descuento")).doubleValue()) + " aplicado correctamente.");
            } else {
                redirectAttributes.addFlashAttribute("error", descuentoData.get("mensaje"));
                return "redirect:/vecino/pagos";
            }
        }// Actualizar el pago
        pago.setTotal(totalFinal.doubleValue());
        pago.setEstado("Pagado");
        informacionPagoRepository.save(pago);

        // Actualizar la reserva
        reserva.setEstado(1); // Pagado
        reservaRepository.save(reserva);

        redirectAttributes.addFlashAttribute("mensajeExito", "Pago realizado correctamente.");
        return "redirect:/vecino/misReservas";
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
    }    @PostMapping("/aplicarDescuento")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> aplicarDescuento(@RequestParam("codigo") String codigo,
                                                @RequestParam("idReserva") Integer idReserva) {
        try {
            Map<String, Object> resultado = validarCupon(codigo, idReserva);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("valido", false);
            resp.put("mensaje", "Error interno del servidor: " + e.getMessage());
            System.out.println("ERROR EXCEPTION en aplicarDescuento: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(resp);
        }
    }

    // Método privado para validar cupones (usado tanto por el endpoint REST como por métodos internos)
    private Map<String, Object> validarCupon(String codigo, Integer idReserva) {
        Map<String, Object> resp = new HashMap<>();

        try {
            // Imprimir los datos recibidos
            System.out.println("=== INICIO VALIDAR CUPON ===");
            System.out.println("Código de cupón recibido: '" + codigo + "'");
            System.out.println("ID de reserva recibido: " + idReserva);

            // Verificar que el parámetro idReserva no sea null
            if (idReserva == null) {
                resp.put("valido", false);
                resp.put("mensaje", "El ID de reserva no puede ser nulo.");
                System.out.println("ERROR: ID de reserva es null");
                return resp;
            }

            LocalDate hoy = LocalDate.now();
            System.out.println("Fecha actual: " + hoy);
            
            // Primero verificar si existe el cupón en general
            System.out.println("Buscando cupón con código: '" + codigo.trim() + "'");
            Optional<Descuento> descSimple = descuentoRepository.findByCodigo(codigo.trim());
            System.out.println("Resultado de búsqueda por código: " + (descSimple.isPresent() ? "ENCONTRADO" : "NO ENCONTRADO"));
            
            if (descSimple.isEmpty()) {
                resp.put("valido", false);
                resp.put("mensaje", "Código de descuento no existe.");
                System.out.println("ERROR: Cupón '" + codigo + "' no existe en la base de datos");
                
                // Debug adicional: verificar todos los cupones existentes
                System.out.println("=== LISTANDO TODOS LOS CUPONES EXISTENTES ===");
                descuentoRepository.findAll().forEach(d -> {
                    System.out.println("Cupón DB: '" + d.getCodigo() + "' - Servicio ID: " + d.getServicio().getIdServicio());
                });
                System.out.println("=== FIN LISTA CUPONES ===");
                
                return resp;
            }

            Descuento descSimpleValue = descSimple.get();
            System.out.println("Cupón encontrado en BD: " + descSimpleValue.getCodigo());
            System.out.println("Fecha inicio: " + descSimpleValue.getFechaInicio());
            System.out.println("Fecha final: " + descSimpleValue.getFechaFinal());
            System.out.println("ID Servicio del cupón: " + descSimpleValue.getServicio().getIdServicio());
            
            // Buscar el descuento válido por código y fecha
            Optional<Descuento> descOpt = descuentoRepository.findValidDescuentoByCodigoAndFecha(codigo.trim(), hoy);

            if (descOpt.isEmpty()) {
                resp.put("valido", false);
                resp.put("mensaje", "Código de descuento inválido o expirado.");
                System.out.println("ERROR: Cupón expirado o fuera de rango de fechas");
                System.out.println("Hoy: " + hoy + ", Inicio: " + descSimpleValue.getFechaInicio() + ", Final: " + descSimpleValue.getFechaFinal());
                return resp;
            }

            Descuento descuento = descOpt.get();
            System.out.println("Descuento encontrado: " + descuento.getCodigo());

            // Obtener la reserva desde idReserva
            Optional<Reserva> reservaOpt = reservaRepository.findById(idReserva);
            if (reservaOpt.isEmpty()) {
                resp.put("valido", false);
                resp.put("mensaje", "Reserva no encontrada.");
                System.out.println("ERROR: Reserva no encontrada con ID " + idReserva);
                return resp;
            }

            Reserva reserva = reservaOpt.get();
            System.out.println("Reserva encontrada: " + reserva.getIdReserva());

            // Obtener idServicio desde la InstanciaServicio de la reserva
            Integer idServicioReserva = reserva.getInstanciaServicio().getServicio().getIdServicio();
            System.out.println("ID del servicio en la reserva: " + idServicioReserva);
            System.out.println("ID del servicio del cupón: " + descuento.getServicio().getIdServicio());

            // Verificar si el descuento aplica para el servicio de la reserva
            if (!descuento.getServicio().getIdServicio().equals(idServicioReserva)) {
                resp.put("valido", false);
                resp.put("mensaje", "El descuento no aplica para este servicio.");
                System.out.println("ERROR: El descuento no aplica para el servicio con ID " + idServicioReserva);
                return resp;
            }

            // Usar el total de la información de pago de la reserva
            if (reserva.getInformacionPago() == null) {
                resp.put("valido", false);
                resp.put("mensaje", "La reserva no tiene información de pago asociada.");
                System.out.println("ERROR: La reserva no tiene información de pago.");
                return resp;
            }

            BigDecimal total = BigDecimal.valueOf(reserva.getInformacionPago().getTotal());
            System.out.println("Total de la reserva: " + total);

            // Calcular el descuento
            BigDecimal descuentoAplicado = BigDecimal.ZERO;
            if ("PORCENTAJE".equalsIgnoreCase(descuento.getTipoDescuento())) {
                descuentoAplicado = total.multiply(BigDecimal.valueOf(descuento.getValor() / 100.0));
                System.out.println("Descuento por porcentaje: " + descuento.getValor() + "%");
            } else if ("FIJO".equalsIgnoreCase(descuento.getTipoDescuento())) {
                descuentoAplicado = BigDecimal.valueOf(descuento.getValor());
                System.out.println("Descuento fijo: S/." + descuento.getValor());
            }

            // Verificar que el descuento no sea mayor al monto de la tarifa
            if (descuentoAplicado.compareTo(total) > 0) {
                descuentoAplicado = total; // No puede ser mayor al total
            }

            // Calcular el total con descuento
            BigDecimal totalConDescuento = total.subtract(descuentoAplicado);
            if (totalConDescuento.compareTo(BigDecimal.ZERO) < 0) {
                totalConDescuento = BigDecimal.ZERO;
            }

            // Respuesta exitosa con el descuento aplicado
            resp.put("valido", true);
            resp.put("descuento", descuentoAplicado);
            resp.put("totalConDescuento", totalConDescuento);
            resp.put("mensaje", "Descuento aplicado correctamente.");
            System.out.println("SUCCESS: Descuento aplicado: " + descuentoAplicado + ", Total con descuento: " + totalConDescuento);
            System.out.println("=== FIN VALIDAR CUPON ===");

            return resp;

        } catch (Exception e) {
            System.out.println("ERROR EXCEPTION en validarCupon: " + e.getMessage());
            e.printStackTrace();
            resp.put("valido", false);
            resp.put("mensaje", "Error interno del servidor: " + e.getMessage());
            return resp;
        }
    }// Endpoint de diagnóstico para probar cupones (público para testing)
    @GetMapping("/diagnosticar-cupon/{codigo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> diagnosticarCupon(@PathVariable String codigo) {
        Map<String, Object> resp = new HashMap<>();
        
        try {
            System.out.println("=== DIAGNÓSTICO DE CUPÓN ===");
            System.out.println("Código a diagnosticar: '" + codigo + "'");
            
            // Verificar si existe el cupón
            Optional<Descuento> descOpt = descuentoRepository.findByCodigo(codigo.trim());
            
            if (descOpt.isEmpty()) {
                resp.put("existe", false);
                resp.put("mensaje", "Cupón no encontrado en la base de datos");
                
                // Listar todos los cupones
                List<String> cupones = descuentoRepository.findAll().stream()
                    .map(Descuento::getCodigo)
                    .collect(Collectors.toList());
                resp.put("cuponesExistentes", cupones);
                
                return ResponseEntity.ok(resp);
            }
            
            Descuento descuento = descOpt.get();
            resp.put("existe", true);
            resp.put("codigo", descuento.getCodigo());
            resp.put("tipoDescuento", descuento.getTipoDescuento());
            resp.put("valor", descuento.getValor());
            resp.put("fechaInicio", descuento.getFechaInicio().toString());
            resp.put("fechaFinal", descuento.getFechaFinal().toString());
            resp.put("idServicio", descuento.getServicio().getIdServicio());
            resp.put("nombreServicio", descuento.getServicio().getNombre());
            
            // Verificar validez de fecha
            LocalDate hoy = LocalDate.now();
            boolean fechaValida = !hoy.isBefore(descuento.getFechaInicio()) && !hoy.isAfter(descuento.getFechaFinal());
            resp.put("fechaValida", fechaValida);
            resp.put("fechaActual", hoy.toString());
              // Verificar reservas compatibles con diferentes estados
            List<Reserva> todasLasReservas = reservaRepository.findAll().stream()
                .filter(r -> r.getInstanciaServicio().getServicio().getIdServicio().equals(descuento.getServicio().getIdServicio()))
                .collect(Collectors.toList());
            
            List<Reserva> reservasPendientes = todasLasReservas.stream()
                .filter(r -> r.getEstado().equals(0))  // Estado 0 = Pendiente
                .collect(Collectors.toList());
                
            resp.put("totalReservasServicio", todasLasReservas.size());
            resp.put("reservasPendientes", reservasPendientes.size());
            
            // Información detallada de estados
            Map<Integer, Long> estadosReservas = todasLasReservas.stream()
                .collect(Collectors.groupingBy(Reserva::getEstado, Collectors.counting()));
            resp.put("estadosReservas", estadosReservas);
            
            System.out.println("Diagnóstico completado para: " + codigo);
            
            return ResponseEntity.ok(resp);
            
        } catch (Exception e) {
            System.out.println("ERROR en diagnóstico: " + e.getMessage());
            e.printStackTrace();
            resp.put("error", true);
            resp.put("mensaje", "Error en diagnóstico: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }    // Endpoint de prueba para verificar conectividad AJAX
    @PostMapping("/test-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testAjax() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "success");
        resp.put("message", "AJAX funciona correctamente");
        resp.put("timestamp", LocalDateTime.now().toString());
        
        System.out.println("Test AJAX ejecutado correctamente");
        return ResponseEntity.ok(resp);
    }

    // Endpoint público para diagnóstico de cupones (sin autenticación para testing)
    @GetMapping("/public/diagnosticar-cupon/{codigo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> diagnosticarCuponPublico(@PathVariable String codigo) {
        Map<String, Object> resp = new HashMap<>();
        
        try {
            System.out.println("=== DIAGNÓSTICO PÚBLICO DE CUPÓN ===");
            System.out.println("Código a diagnosticar: '" + codigo + "'");
            
            // Verificar si existe el cupón
            Optional<Descuento> descOpt = descuentoRepository.findByCodigo(codigo.trim());
            
            if (descOpt.isEmpty()) {
                resp.put("existe", false);
                resp.put("mensaje", "Cupón no encontrado en la base de datos");
                
                // Listar todos los cupones
                List<String> cupones = descuentoRepository.findAll().stream()
                    .map(Descuento::getCodigo)
                    .collect(Collectors.toList());
                resp.put("cuponesExistentes", cupones);
                
                return ResponseEntity.ok(resp);
            }
            
            Descuento descuento = descOpt.get();
            resp.put("existe", true);
            resp.put("codigo", descuento.getCodigo());
            resp.put("tipoDescuento", descuento.getTipoDescuento());
            resp.put("valor", descuento.getValor());
            resp.put("fechaInicio", descuento.getFechaInicio().toString());
            resp.put("fechaFinal", descuento.getFechaFinal().toString());
            resp.put("idServicio", descuento.getServicio().getIdServicio());
            resp.put("nombreServicio", descuento.getServicio().getNombre());
            
            // Verificar validez de fecha
            LocalDate hoy = LocalDate.now();
            boolean fechaValida = !hoy.isBefore(descuento.getFechaInicio()) && !hoy.isAfter(descuento.getFechaFinal());
            resp.put("fechaValida", fechaValida);
            resp.put("fechaActual", hoy.toString());
            
            System.out.println("Diagnóstico público completado para: " + codigo);
            
            return ResponseEntity.ok(resp);
            
        } catch (Exception e) {
            System.out.println("ERROR en diagnóstico público: " + e.getMessage());
            e.printStackTrace();
            resp.put("error", true);
            resp.put("mensaje", "Error en diagnóstico: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }
    
}
