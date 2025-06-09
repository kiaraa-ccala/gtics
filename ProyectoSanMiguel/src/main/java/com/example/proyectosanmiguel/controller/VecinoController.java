package com.example.proyectosanmiguel.controller;

// Java core imports
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

// Spring imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Project specific imports
import com.example.proyectosanmiguel.dto.ReservaCostoDto;
import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.*;

@Controller
@RequestMapping("/vecino")
public class VecinoController {
    
    @Autowired
    private ComplejoRepository complejoRepository;
    @Autowired
    private InformacionPagoRepository informacionPagoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private DescuentoRepository descuentoRepository;

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
    }    @GetMapping("/pagos")
    public String mostrarPasarelaPago(Model model, @RequestParam(value = "codigoCupon", required = false) String codigoCupon) {
        Usuario usuario = obtenerUsuarioAutenticado();

        if (usuario == null) {
            return "redirect:/login";
        }

        // Obtener reservas pendientes de pago - sin dependencias estrictas
        List<Reserva> reservasPendientes = reservaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .stream()
                .filter(r -> r.getEstado() == 0 && r.getInformacionPago() != null
                        && "Pendiente".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .toList();

        // Calcular totales - manejar caso sin reservas
        double total = reservasPendientes.stream()
                .mapToDouble(r -> r.getInformacionPago().getTotal().doubleValue())
                .sum();

        double descuentoAplicado = 0;
        double totalConDescuento = total;

        // Validar cupón solo si hay reservas pendientes y cupón proporcionado
        if (codigoCupon != null && !codigoCupon.isEmpty() && !reservasPendientes.isEmpty()) {
            try {
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
            } catch (Exception e) {
                // Manejar errores de validación de cupón graciosamente
                model.addAttribute("mensajeDescuento", "Error al validar cupón. Intenta nuevamente.");
            }
        } else if (codigoCupon != null && !codigoCupon.isEmpty() && reservasPendientes.isEmpty()) {
            model.addAttribute("mensajeDescuento", "No hay reservas pendientes para aplicar el cupón.");
        }

        // Obtener credenciales de manera segura
        Credencial credencial = null;
        try {
            credencial = credencialRepository.findByCorreo(usuario.getCredencial().getCorreo());
        } catch (Exception e) {
            // Crear credencial temporal si hay problemas
            credencial = new Credencial();
            credencial.setCorreo(usuario.getCredencial() != null ? usuario.getCredencial().getCorreo() : "");
        }

        // Agregar atributos al modelo - siempre disponibles
        model.addAttribute("usuario", usuario);
        model.addAttribute("credencial", credencial);
        model.addAttribute("reservasPendientes", reservasPendientes);
        model.addAttribute("totalReservas", totalConDescuento);
        model.addAttribute("hayReservasPendientes", !reservasPendientes.isEmpty());

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
    @Transactional
    public String guardarPago(@RequestParam("idReserva") Integer idReserva,
                              @RequestParam(value = "codigoCupon", required = false) String codigoCupon,
                              @RequestParam(value = "metodoPago", required = false) String metodoPago,
                              @RequestParam(value = "comprobanteFile", required = false) MultipartFile comprobanteFile,
                              RedirectAttributes redirectAttributes) {// Validación de parámetros básicos
        if (idReserva == null) {
            redirectAttributes.addFlashAttribute("error", "ID de reserva no válido.");
            return "redirect:/vecino/pagos";
        }
        
        if (metodoPago == null || metodoPago.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar un método de pago.");
            return "redirect:/vecino/pagos";
        }
        
        System.out.println("DEBUG: Método de pago recibido: " + metodoPago);
        System.out.println("DEBUG: ID de reserva: " + idReserva);
        System.out.println("DEBUG: Código cupón: " + codigoCupon);

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
        
        // Verificar que la reserva esté pendiente de pago
        if (reserva.getEstado() != 0) {
            redirectAttributes.addFlashAttribute("error", "La reserva no está pendiente de pago o ya ha sido procesada.");
            return "redirect:/vecino/pagos";
        }

        // Verificar que el estado del pago sea Pendiente
        if (!"Pendiente".equalsIgnoreCase(reserva.getInformacionPago().getEstado())) {
            redirectAttributes.addFlashAttribute("error", "El pago ya ha sido procesado o no está en estado pendiente.");
            return "redirect:/vecino/pagos";
        }

        InformacionPago pago = reserva.getInformacionPago();
        BigDecimal totalFinal = BigDecimal.valueOf(pago.getTotal());

        // Aplicar descuento si se proporciona cupón
        if (codigoCupon != null && !codigoCupon.trim().isEmpty()) {
            try {
                Map<String, Object> descuentoData = validarCupon(codigoCupon, idReserva);
                
                if ((boolean) descuentoData.get("valido")) {
                    totalFinal = (BigDecimal) descuentoData.get("totalConDescuento");
                    redirectAttributes.addFlashAttribute("mensajeDescuento", 
                        "Descuento de S/." + String.format("%.2f", ((BigDecimal) descuentoData.get("descuento")).doubleValue()) + " aplicado correctamente.");
                } else {
                    redirectAttributes.addFlashAttribute("error", descuentoData.get("mensaje"));
                    return "redirect:/vecino/pagos";
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error al procesar el cupón: " + e.getMessage());
                return "redirect:/vecino/pagos";
            }
        }

        // Actualizar el total con el descuento aplicado
        pago.setTotal(totalFinal.doubleValue());
        pago.setFecha(LocalDate.now());
        pago.setHora(LocalTime.now());        // Procesar según el método de pago
        try {
            System.out.println("DEBUG: Procesando pago con método: " + metodoPago + " para reserva: " + idReserva);
            
            if ("efectivo".equalsIgnoreCase(metodoPago)) {
                // Validaciones para pago en efectivo
                if (comprobanteFile == null || comprobanteFile.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Debe subir un comprobante de pago para el pago en efectivo.");
                    return "redirect:/vecino/pagos";
                }
                
                String fileName = comprobanteFile.getOriginalFilename();
                if (fileName == null || !isValidFileType(fileName)) {
                    redirectAttributes.addFlashAttribute("error", "Formato de archivo no válido. Solo se permiten JPG, PNG y PDF.");
                    return "redirect:/vecino/pagos";
                }
                
                if (comprobanteFile.getSize() > 5 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("error", "El archivo es muy grande. Tamaño máximo permitido: 5MB.");
                    return "redirect:/vecino/pagos";
                }

                // Procesar comprobante
                procesarComprobanteFile(comprobanteFile, reserva.getIdReserva());
                
                // Configurar pago en efectivo como pendiente de verificación
                pago.setEstado("Pendiente_Verificacion");
                pago.setTipo("Efectivo");
                reserva.setEstado(2); // 2 = Pendiente de Verificación
                
                System.out.println("DEBUG: Pago en efectivo configurado - Estado pago: " + pago.getEstado() + ", Estado reserva: " + reserva.getEstado());
                
                redirectAttributes.addFlashAttribute("mensajeExito", 
                    "Pago en efectivo registrado exitosamente. Tu comprobante ha sido recibido y está pendiente de verificación. " +
                    "Revisaremos tu comprobante en un plazo de 24-48 horas.");
                    
            } else {
                // Otros métodos de pago: confirmación inmediata
                pago.setEstado("Pagado");
                reserva.setEstado(1); // 1 = Activo/Pagado
                
                System.out.println("DEBUG: Pago digital configurado - Estado pago: " + pago.getEstado() + ", Estado reserva: " + reserva.getEstado());
                
                // Configurar tipo de pago
                if ("tarjeta".equalsIgnoreCase(metodoPago)) {
                    pago.setTipo("Tarjeta");
                } else if ("paypal".equalsIgnoreCase(metodoPago)) {
                    pago.setTipo("PayPal");
                } else if ("yape".equalsIgnoreCase(metodoPago)) {
                    pago.setTipo("Yape");
                } else if ("plin".equalsIgnoreCase(metodoPago)) {
                    pago.setTipo("Plin");
                } else {
                    pago.setTipo("Digital"); // Por defecto para otros métodos
                }
                
                redirectAttributes.addFlashAttribute("mensajeExito", "Pago realizado correctamente. Tu reserva está confirmada.");
            }            // Guardar cambios en la base de datos con logging
            System.out.println("DEBUG: Guardando información de pago...");
            System.out.println("DEBUG: Estado antes de guardar - Pago: " + pago.getEstado() + ", Reserva: " + reserva.getEstado());
            
            InformacionPago pagoGuardado = informacionPagoRepository.save(pago);
            System.out.println("DEBUG: Pago guardado con estado: " + pagoGuardado.getEstado());
            
            System.out.println("DEBUG: Guardando reserva...");
            Reserva reservaGuardada = reservaRepository.save(reserva);
            System.out.println("DEBUG: Reserva guardada con estado: " + reservaGuardada.getEstado());
            
            // Forzar flush para asegurar que los cambios se escriban inmediatamente
            informacionPagoRepository.flush();
            reservaRepository.flush();
            
            // Verificar los cambios releyendo desde la base de datos
            Optional<Reserva> reservaVerificacion = reservaRepository.findById(idReserva);
            if (reservaVerificacion.isPresent()) {
                Reserva rVerif = reservaVerificacion.get();
                System.out.println("DEBUG: Verificación - Estado final reserva: " + rVerif.getEstado());
                System.out.println("DEBUG: Verificación - Estado final pago: " + rVerif.getInformacionPago().getEstado());
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: Error al procesar el pago: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al procesar el pago: " + e.getMessage());
            return "redirect:/vecino/pagos";
        }

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
    }    @GetMapping("/historial-pagos")
    public String mostrarHistorialPagos(Model model, 
                                       @RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "size", defaultValue = "10") int size) {
        System.out.println("DEBUG: Iniciando mostrarHistorialPagos...");
        
        Usuario usuario = obtenerUsuarioAutenticado();
        System.out.println("DEBUG: Usuario obtenido: " + (usuario != null ? usuario.getNombre() : "null"));
        
        if (usuario == null) {
            System.out.println("DEBUG: Usuario no autenticado, redirigiendo a login");
            return "redirect:/login";
        }
        
        // Obtener todas las reservas con información de pago del usuario - manejo seguro
        List<Reserva> reservasConPago = Collections.emptyList();
        try {
            List<Reserva> todasLasReservas = reservaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
            System.out.println("DEBUG: Total reservas encontradas: " + todasLasReservas.size());
            
            reservasConPago = todasLasReservas.stream()
                    .filter(r -> {
                        boolean tieneInfoPago = r.getInformacionPago() != null;
                        if (tieneInfoPago) {
                            System.out.println("DEBUG: Reserva " + r.getIdReserva() + " tiene info de pago: " + r.getInformacionPago().getEstado());
                        }
                        return tieneInfoPago;
                    })
                    .sorted((r1, r2) -> {
                        // Manejo seguro de fechas de registro
                        if (r1.getFechaHoraRegistro() == null && r2.getFechaHoraRegistro() == null) return 0;
                        if (r1.getFechaHoraRegistro() == null) return 1;
                        if (r2.getFechaHoraRegistro() == null) return -1;
                        return r2.getFechaHoraRegistro().compareTo(r1.getFechaHoraRegistro());
                    })
                    .collect(Collectors.toList());
                    
            System.out.println("DEBUG: Reservas con info de pago: " + reservasConPago.size());
        } catch (Exception e) {
            // Log error y continuar con lista vacía
            System.err.println("Error al obtener historial de pagos: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar el historial de pagos: " + e.getMessage());
            reservasConPago = Collections.emptyList();
        }
        
        // Estadísticas de pagos - manejo seguro con listas vacías
        long totalPagos = reservasConPago.size();
        long pagosCompletados = reservasConPago.stream()
                .filter(r -> r.getInformacionPago() != null && "Pagado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .count();
        long pagosPendientes = reservasConPago.stream()
                .filter(r -> r.getInformacionPago() != null && 
                           ("Pendiente".equalsIgnoreCase(r.getInformacionPago().getEstado()) || 
                            "Pendiente_Verificacion".equalsIgnoreCase(r.getInformacionPago().getEstado())))
                .count();
        long pagosRechazados = reservasConPago.stream()
                .filter(r -> r.getInformacionPago() != null && "Rechazado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .count();
        
        double totalGastado = reservasConPago.stream()
                .filter(r -> r.getInformacionPago() != null && "Pagado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .mapToDouble(r -> r.getInformacionPago().getTotal())
                .sum();
        
        // Agrupar pagos por método de pago - manejo seguro
        Map<String, Long> pagosPorMetodo = reservasConPago.stream()
                .filter(r -> r.getInformacionPago() != null && "Pagado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .collect(Collectors.groupingBy(
                        r -> r.getInformacionPago().getTipo() != null ? r.getInformacionPago().getTipo() : "No especificado",
                        Collectors.counting()
                ));
          // Agregar atributos al modelo - siempre disponibles
        model.addAttribute("reservasConPago", reservasConPago);
        model.addAttribute("totalPagos", totalPagos);
        model.addAttribute("pagosCompletados", pagosCompletados);
        model.addAttribute("pagosPendientes", pagosPendientes);
        model.addAttribute("pagosRechazados", pagosRechazados);
        model.addAttribute("totalGastado", totalGastado);
        model.addAttribute("pagosPorMetodo", pagosPorMetodo);
        model.addAttribute("hayHistorialPagos", !reservasConPago.isEmpty());
        
        // Debug información adicional
        model.addAttribute("debug", true);
        
        System.out.println("DEBUG: Enviando al modelo:");
        System.out.println("- reservasConPago size: " + reservasConPago.size());
        System.out.println("- hayHistorialPagos: " + !reservasConPago.isEmpty());
        System.out.println("- totalPagos: " + totalPagos);
        System.out.println("- pagosCompletados: " + pagosCompletados);
        
        return "Vecino/vecino_historial_pagos";
    }

    @GetMapping("/reenviar-comprobante/{idReserva}")
    public String reenviarComprobante(@PathVariable Integer idReserva, RedirectAttributes redirectAttributes) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(idReserva);
        
        if (reservaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Reserva no encontrada.");
            return "redirect:/vecino/historial-pagos";
        }
        
        Reserva reserva = reservaOpt.get();
        
        // Verificar que la reserva pertenece al usuario autenticado
        Usuario usuario = obtenerUsuarioAutenticado();
        if (!reserva.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para realizar esta acción.");
            return "redirect:/vecino/historial-pagos";
        }
        
        // Verificar que el pago esté rechazado
        if (!"Rechazado".equalsIgnoreCase(reserva.getInformacionPago().getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Solo puedes reenviar comprobantes de pagos rechazados.");
            return "redirect:/vecino/historial-pagos";
        }
        
        // Cambiar estado a pendiente para permitir nuevo envío
        reserva.getInformacionPago().setEstado("Pendiente");
        reserva.setEstado(0); // Pendiente
        
        informacionPagoRepository.save(reserva.getInformacionPago());
        reservaRepository.save(reserva);
        
        redirectAttributes.addFlashAttribute("mensajeExito", 
            "Ahora puedes volver a subir tu comprobante. Ve a la sección de pagos para completar el proceso.");
        
        return "redirect:/vecino/pagos";
    }    // ENDPOINT TEMPORAL PARA DEBUGGING - REMOVER EN PRODUCCIÓN
    @GetMapping("/debug-pagos")
    @ResponseBody
    public Map<String, Object> debugPagos() {
        Usuario usuario = obtenerUsuarioAutenticado();
        
        Map<String, Object> debug = new HashMap<>();
        
        if (usuario == null) {
            debug.put("error", "Usuario no autenticado");
            return debug;
        }
        
        try {
            // Obtener todas las reservas del usuario
            List<Reserva> todasReservas = reservaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
            debug.put("totalReservas", todasReservas.size());
            
            // Filtrar reservas con información de pago
            List<Reserva> reservasConPago = todasReservas.stream()
                    .filter(r -> r.getInformacionPago() != null)
                    .collect(Collectors.toList());
            debug.put("reservasConPago", reservasConPago.size());
            
            // Detalles de cada reserva
            List<Map<String, Object>> detallesReservas = new ArrayList<>();
            for (Reserva r : todasReservas) {
                Map<String, Object> detalle = new HashMap<>();
                detalle.put("idReserva", r.getIdReserva());
                detalle.put("estadoReserva", r.getEstado());
                detalle.put("fecha", r.getFecha());
                detalle.put("servicio", r.getInstanciaServicio().getServicio().getNombre());
                
                if (r.getInformacionPago() != null) {
                    detalle.put("tieneInfoPago", true);
                    detalle.put("estadoPago", r.getInformacionPago().getEstado());
                    detalle.put("tipoPago", r.getInformacionPago().getTipo());
                    detalle.put("totalPago", r.getInformacionPago().getTotal());
                } else {
                    detalle.put("tieneInfoPago", false);
                }
                
                detallesReservas.add(detalle);
            }
            debug.put("detallesReservas", detallesReservas);
            
            // Estadísticas
            debug.put("usuario", usuario.getNombre() + " " + usuario.getApellido());
            debug.put("correo", usuario.getCredencial().getCorreo());
            
        } catch (Exception e) {
            debug.put("error", "Error al obtener datos: " + e.getMessage());
            debug.put("stackTrace", e.getClass().getSimpleName());
        }
        
        return debug;
    }
    
    // ENDPOINT TEMPORAL PARA VERIFICAR ESTADOS DESPUÉS DE PAGO
    @GetMapping("/debug-estado-reserva/{idReserva}")
    @ResponseBody
    public Map<String, Object> debugEstadoReserva(@PathVariable Integer idReserva) {
        Map<String, Object> debug = new HashMap<>();
        
        try {
            Optional<Reserva> reservaOpt = reservaRepository.findById(idReserva);
            if (reservaOpt.isPresent()) {
                Reserva reserva = reservaOpt.get();
                debug.put("encontrada", true);
                debug.put("idReserva", reserva.getIdReserva());
                debug.put("estadoReserva", reserva.getEstado());
                debug.put("estadoTexto", getEstadoTexto(reserva.getEstado()));
                
                if (reserva.getInformacionPago() != null) {
                    debug.put("estadoPago", reserva.getInformacionPago().getEstado());
                    debug.put("tipoPago", reserva.getInformacionPago().getTipo());
                    debug.put("totalPago", reserva.getInformacionPago().getTotal());
                    debug.put("fechaPago", reserva.getInformacionPago().getFecha());
                } else {
                    debug.put("informacionPago", "No disponible");
                }
            } else {
                debug.put("encontrada", false);
                debug.put("mensaje", "Reserva no encontrada");
            }
        } catch (Exception e) {
            debug.put("error", "Error al verificar reserva: " + e.getMessage());
        }
        
        return debug;
    }
    
    private String getEstadoTexto(Integer estado) {
        switch (estado) {
            case 0: return "Pendiente";
            case 1: return "Activo";
            case 2: return "Pendiente Verificación";
            default: return "Desconocido";
        }
    }

    // Métodos auxiliares para el procesamiento de archivos de comprobantes
    
    /**
     * Valida el tipo de archivo basándose en la extensión
     */
    private boolean isValidFileType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        
        String extension = fileName.toLowerCase();
        if (extension.lastIndexOf('.') > 0) {
            extension = extension.substring(extension.lastIndexOf('.') + 1);
        }
        
        return Arrays.asList("jpg", "jpeg", "png", "pdf").contains(extension);
    }
    
    /**
     * Procesa y guarda el archivo del comprobante de pago
     */
    private String procesarComprobanteFile(MultipartFile file, Integer idReserva) throws IOException {
        // Crear directorio si no existe
        String uploadDir = "uploads/comprobantes/";
        Path uploadPath = Paths.get(uploadDir);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generar nombre único para el archivo
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.lastIndexOf('.') > 0) {
            extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        }
        
        String uniqueFileName = "comprobante_reserva_" + idReserva + "_" + 
                               System.currentTimeMillis() + extension;
        
        // Ruta completa del archivo
        Path filePath = uploadPath.resolve(uniqueFileName);
        
        // Guardar el archivo
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Retornar la ruta relativa para almacenar en la base de datos
        return uploadDir + uniqueFileName;
    }

    @GetMapping("/debug-historial")
    public String debugHistorialPagos(Model model) {
        System.out.println("DEBUG: Iniciando debug del historial de pagos...");
        
        Usuario usuario = obtenerUsuarioAutenticado();
        System.out.println("DEBUG: Usuario obtenido: " + (usuario != null ? usuario.getNombre() : "null"));
        
        if (usuario == null) {
            model.addAttribute("error", "Usuario no autenticado");
            return "Vecino/debug_historial";
        }
        
        // Obtener datos básicos
        List<Reserva> todasLasReservas = reservaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        List<Reserva> reservasConPago = todasLasReservas.stream()
                .filter(r -> r.getInformacionPago() != null)
                .collect(Collectors.toList());
        
        // Agregar datos de debug al modelo
        model.addAttribute("reservasConPago", reservasConPago);
        model.addAttribute("totalPagos", (long) reservasConPago.size());
        model.addAttribute("pagosCompletados", reservasConPago.stream()
                .filter(r -> "Pagado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .count());
        model.addAttribute("pagosPendientes", reservasConPago.stream()
                .filter(r -> "Pendiente".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .count());
        model.addAttribute("pagosRechazados", reservasConPago.stream()
                .filter(r -> "Rechazado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .count());
        model.addAttribute("totalGastado", reservasConPago.stream()
                .filter(r -> "Pagado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .mapToDouble(r -> r.getInformacionPago().getTotal())
                .sum());
        model.addAttribute("hayHistorialPagos", !reservasConPago.isEmpty());
        model.addAttribute("debug", true);
        
        // Métodos de pago
        Map<String, Long> pagosPorMetodo = reservasConPago.stream()
                .filter(r -> "Pagado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .collect(Collectors.groupingBy(
                        r -> r.getInformacionPago().getTipo() != null ? r.getInformacionPago().getTipo() : "No especificado",
                        Collectors.counting()
                ));
        model.addAttribute("pagosPorMetodo", pagosPorMetodo);
        
        System.out.println("DEBUG: Datos enviados al template:");
        System.out.println("- Total reservas: " + todasLasReservas.size());
        System.out.println("- Reservas con pago: " + reservasConPago.size());
        System.out.println("- Hay historial: " + !reservasConPago.isEmpty());
        
        return "Vecino/debug_historial";
    }

    @GetMapping("/minimal-historial")
    public String minimalHistorialPagos(Model model) {
        System.out.println("DEBUG: Iniciando minimal historial de pagos...");
        
        Usuario usuario = obtenerUsuarioAutenticado();
        if (usuario == null) {
            model.addAttribute("error", "Usuario no autenticado");
            model.addAttribute("hayHistorialPagos", false);
            model.addAttribute("totalPagos", 0L);
            model.addAttribute("pagosCompletados", 0L);
            model.addAttribute("pagosPendientes", 0L);
            model.addAttribute("pagosRechazados", 0L);
            model.addAttribute("debug", true);
            return "Vecino/minimal_historial";
        }
        
        // Obtener datos básicos
        List<Reserva> todasLasReservas = reservaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        List<Reserva> reservasConPago = todasLasReservas.stream()
                .filter(r -> r.getInformacionPago() != null)
                .collect(Collectors.toList());
        
        // Estadísticas básicas
        long totalPagos = reservasConPago.size();
        long pagosCompletados = reservasConPago.stream()
                .filter(r -> "Pagado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .count();
        long pagosPendientes = reservasConPago.stream()
                .filter(r -> "Pendiente".equalsIgnoreCase(r.getInformacionPago().getEstado()) ||
                           "Pendiente_Verificacion".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .count();
        long pagosRechazados = reservasConPago.stream()
                .filter(r -> "Rechazado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .count();
        
        // Métodos de pago
        Map<String, Long> pagosPorMetodo = reservasConPago.stream()
                .filter(r -> "Pagado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .collect(Collectors.groupingBy(
                        r -> r.getInformacionPago().getTipo() != null ? r.getInformacionPago().getTipo() : "No especificado",
                        Collectors.counting()
                ));
        
        // Agregar al modelo
        model.addAttribute("reservasConPago", reservasConPago);
        model.addAttribute("totalPagos", totalPagos);
        model.addAttribute("pagosCompletados", pagosCompletados);
        model.addAttribute("pagosPendientes", pagosPendientes);
        model.addAttribute("pagosRechazados", pagosRechazados);
        model.addAttribute("pagosPorMetodo", pagosPorMetodo);
        model.addAttribute("hayHistorialPagos", !reservasConPago.isEmpty());
        model.addAttribute("debug", true);
        
        System.out.println("DEBUG Minimal: Total reservas=" + todasLasReservas.size() + 
                          ", Con pago=" + reservasConPago.size() + 
                          ", Hay historial=" + !reservasConPago.isEmpty());
        
        return "Vecino/minimal_historial";
    }

    @GetMapping("/debug-simple")
    public String debugSimple(Model model) {
        System.out.println("DEBUG SIMPLE: Iniciando test básico...");
        
        // Datos mínimos para testing
        model.addAttribute("hayHistorialPagos", false);
        model.addAttribute("totalPagos", 0L);
        model.addAttribute("pagosCompletados", 0L);
        model.addAttribute("pagosPendientes", 0L);
        model.addAttribute("pagosRechazados", 0L);
        model.addAttribute("totalGastado", 0.0);
        model.addAttribute("debug", true);
        model.addAttribute("reservasConPago", Collections.emptyList());
        model.addAttribute("pagosPorMetodo", Collections.emptyMap());
        
        System.out.println("DEBUG SIMPLE: Template renderizado exitosamente");
        return "Vecino/debug_simple_historial";
    }

    @GetMapping("/test-no-fragments")
    public String testSinFragmentos(Model model) {
        System.out.println("DEBUG: Iniciando test sin fragmentos...");
        
        Usuario usuario = obtenerUsuarioAutenticado();
        if (usuario == null) {
            model.addAttribute("error", "Usuario no autenticado");
            model.addAttribute("hayHistorialPagos", false);
            model.addAttribute("totalPagos", 0L);
            model.addAttribute("pagosCompletados", 0L);
            model.addAttribute("pagosPendientes", 0L);
            model.addAttribute("pagosRechazados", 0L);
            model.addAttribute("totalGastado", 0.0);
            model.addAttribute("debug", true);
            model.addAttribute("reservasConPago", Collections.emptyList());
            model.addAttribute("pagosPorMetodo", Collections.emptyMap());
            return "Vecino/no_fragments_historial";
        }
        
        // Obtener datos reales del usuario
        List<Reserva> todasLasReservas = reservaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        List<Reserva> reservasConPago = todasLasReservas.stream()
                .filter(r -> r.getInformacionPago() != null)
                .collect(Collectors.toList());
        
        // Estadísticas
        long totalPagos = reservasConPago.size();
        long pagosCompletados = reservasConPago.stream()
                .filter(r -> "Pagado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .count();
        long pagosPendientes = reservasConPago.stream()
                .filter(r -> "Pendiente".equalsIgnoreCase(r.getInformacionPago().getEstado()) ||
                           "Pendiente_Verificacion".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .count();
        long pagosRechazados = reservasConPago.stream()
                .filter(r -> "Rechazado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .count();
        
        double totalGastado = reservasConPago.stream()
                .filter(r -> "Pagado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .mapToDouble(r -> r.getInformacionPago().getTotal())
                .sum();
        
        Map<String, Long> pagosPorMetodo = reservasConPago.stream()
                .filter(r -> "Pagado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .collect(Collectors.groupingBy(
                        r -> r.getInformacionPago().getTipo() != null ? r.getInformacionPago().getTipo() : "No especificado",
                        Collectors.counting()
                ));
        
        // Agregar al modelo
        model.addAttribute("reservasConPago", reservasConPago);
        model.addAttribute("totalPagos", totalPagos);
        model.addAttribute("pagosCompletados", pagosCompletados);
        model.addAttribute("pagosPendientes", pagosPendientes);
        model.addAttribute("pagosRechazados", pagosRechazados);
        model.addAttribute("totalGastado", totalGastado);
        model.addAttribute("pagosPorMetodo", pagosPorMetodo);
        model.addAttribute("hayHistorialPagos", !reservasConPago.isEmpty());
        model.addAttribute("debug", true);
        
        System.out.println("DEBUG SIN FRAGMENTOS: Total reservas=" + todasLasReservas.size() + 
                          ", Con pago=" + reservasConPago.size() + 
                          ", Hay historial=" + !reservasConPago.isEmpty());
          return "Vecino/no_fragments_historial";
    }

    @GetMapping("/test-cdn")
    public String testCdn(Model model) {
        System.out.println("DEBUG CDN: Iniciando test con recursos CDN...");
        
        Usuario usuario = obtenerUsuarioAutenticado();
        if (usuario == null) {
            model.addAttribute("error", "Usuario no autenticado");
            model.addAttribute("hayHistorialPagos", false);
            model.addAttribute("totalPagos", 0L);
            model.addAttribute("pagosCompletados", 0L);
            model.addAttribute("pagosPendientes", 0L);
            model.addAttribute("pagosRechazados", 0L);
            model.addAttribute("totalGastado", 0.0);
            model.addAttribute("debug", true);
            model.addAttribute("reservasConPago", Collections.emptyList());
            model.addAttribute("pagosPorMetodo", Collections.emptyMap());
            return "Vecino/cdn_historial";
        }
          // Obtener SOLO los pagos completados (estado "Pagado")
        List<Reserva> todasLasReservas = reservaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        List<Reserva> pagosPagados = todasLasReservas.stream()
                .filter(r -> r.getInformacionPago() != null && 
                           "Pagado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .sorted((r1, r2) -> {
                    // Ordenar por fecha de pago descendente (más recientes primero)
                    if (r1.getInformacionPago().getFecha() != null && r2.getInformacionPago().getFecha() != null) {
                        return r2.getInformacionPago().getFecha().compareTo(r1.getInformacionPago().getFecha());
                    }
                    return r2.getFecha().compareTo(r1.getFecha());
                })
                .collect(Collectors.toList());
        
        // Estadísticas solo de pagos completados
        long totalPagosCompletados = pagosPagados.size();
        double totalGastado = pagosPagados.stream()
                .mapToDouble(r -> r.getInformacionPago().getTotal())
                .sum();
        
        Map<String, Long> pagosPorMetodo = pagosPagados.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getInformacionPago().getTipo() != null ? r.getInformacionPago().getTipo() : "No especificado",
                        Collectors.counting()
                ));
        
        // Calcular información adicional
        double pagoPromedio = totalPagosCompletados > 0 ? totalGastado / totalPagosCompletados : 0.0;
        Optional<Reserva> primerPago = pagosPagados.stream().min((r1, r2) -> {
            if (r1.getInformacionPago().getFecha() != null && r2.getInformacionPago().getFecha() != null) {
                return r1.getInformacionPago().getFecha().compareTo(r2.getInformacionPago().getFecha());
            }
            return r1.getFecha().compareTo(r2.getFecha());
        });
        
        // Agregar al modelo
        model.addAttribute("pagosPagados", pagosPagados);
        model.addAttribute("totalPagosCompletados", totalPagosCompletados);
        model.addAttribute("totalGastado", totalGastado);
        model.addAttribute("pagoPromedio", pagoPromedio);
        model.addAttribute("pagosPorMetodo", pagosPorMetodo);
        model.addAttribute("hayHistorialPagos", !pagosPagados.isEmpty());
        model.addAttribute("primerPago", primerPago.orElse(null));
        model.addAttribute("debug", true);
          System.out.println("DEBUG CDN: Total reservas=" + todasLasReservas.size() + 
                          ", Pagos completados=" + pagosPagados.size() + 
                          ", Total gastado=S/ " + totalGastado);
          return "Vecino/cdn_historial";
    }

    @GetMapping("/debug-datos")
    @ResponseBody
    public Map<String, Object> debugDatos() {
        Map<String, Object> debug = new HashMap<>();
        
        try {
            // Obtener usuario
            Usuario usuario = obtenerUsuarioAutenticado();
            debug.put("usuarioAutenticado", usuario != null);
            
            if (usuario != null) {
                debug.put("usuarioId", usuario.getIdUsuario());
                debug.put("usuarioNombre", usuario.getNombre());
                debug.put("usuarioEmail", usuario.getCredencial().getCorreo());
                
                // Obtener todas las reservas del usuario
                List<Reserva> todasReservas = reservaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
                debug.put("totalReservas", todasReservas.size());
                
                // Contar reservas con información de pago
                long reservasConPago = todasReservas.stream()
                    .filter(r -> r.getInformacionPago() != null)
                    .count();
                debug.put("reservasConPago", reservasConPago);
                
                // Contar por estado
                Map<String, Long> porEstado = todasReservas.stream()
                    .filter(r -> r.getInformacionPago() != null)
                    .collect(Collectors.groupingBy(
                        r -> r.getInformacionPago().getEstado() != null ? 
                            r.getInformacionPago().getEstado() : "NULL",
                        Collectors.counting()
                    ));
                debug.put("reservasPorEstado", porEstado);
                
                // Obtener solo pagos completados
                List<Reserva> pagosPagados = todasReservas.stream()
                    .filter(r -> r.getInformacionPago() != null && 
                               "Pagado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                    .collect(Collectors.toList());
                debug.put("pagosPagados", pagosPagados.size());
                
                // Detalles de los primeros 3 pagos
                List<Map<String, Object>> ejemplos = pagosPagados.stream()
                    .limit(3)
                    .map(r -> {
                        Map<String, Object> ejemplo = new HashMap<>();
                        ejemplo.put("reservaId", r.getIdReserva());
                        ejemplo.put("fecha", r.getFecha());
                        ejemplo.put("monto", r.getInformacionPago().getTotal());
                        ejemplo.put("estado", r.getInformacionPago().getEstado());
                        ejemplo.put("metodo", r.getInformacionPago().getTipo());
                        return ejemplo;
                    })
                    .collect(Collectors.toList());
                debug.put("ejemplosPagos", ejemplos);
            } else {
                debug.put("error", "Usuario no autenticado");
            }
        } catch (Exception e) {
            debug.put("excepcion", e.getMessage());
            debug.put("stackTrace", Arrays.toString(e.getStackTrace()));
        }
          return debug;
    }

    @GetMapping("/diagnostico-simple")
    public String diagnosticoSimple(Model model) {
        System.out.println("DEBUG DIAGNÓSTICO: Iniciando diagnóstico simple...");
        
        Usuario usuario = obtenerUsuarioAutenticado();
        if (usuario == null) {
            model.addAttribute("error", "Usuario no autenticado");
            model.addAttribute("hayHistorialPagos", false);
            model.addAttribute("totalPagosCompletados", 0L);
            model.addAttribute("totalGastado", 0.0);
            model.addAttribute("pagoPromedio", 0.0);
            model.addAttribute("pagosPorMetodo", Collections.emptyMap());
            model.addAttribute("pagosPagados", Collections.emptyList());
            model.addAttribute("primerPago", null);
            model.addAttribute("debug", true);
            return "Vecino/diagnostico_simple";
        }
        
        try {
            // Obtener SOLO los pagos completados (estado "Pagado")
            List<Reserva> todasLasReservas = reservaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
            System.out.println("DEBUG: Total reservas encontradas: " + todasLasReservas.size());
            
            List<Reserva> pagosPagados = todasLasReservas.stream()
                    .filter(r -> r.getInformacionPago() != null && 
                               "Pagado".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                    .sorted((r1, r2) -> {
                        // Ordenar por fecha de pago descendente (más recientes primero)
                        if (r1.getInformacionPago().getFecha() != null && r2.getInformacionPago().getFecha() != null) {
                            return r2.getInformacionPago().getFecha().compareTo(r1.getInformacionPago().getFecha());
                        }
                        return r2.getFecha().compareTo(r1.getFecha());
                    })
                    .collect(Collectors.toList());
            
            System.out.println("DEBUG: Pagos completados encontrados: " + pagosPagados.size());
            
            // Estadísticas solo de pagos completados
            long totalPagosCompletados = pagosPagados.size();
            double totalGastado = pagosPagados.stream()
                    .mapToDouble(r -> r.getInformacionPago().getTotal())
                    .sum();
            
            Map<String, Long> pagosPorMetodo = pagosPagados.stream()
                    .collect(Collectors.groupingBy(
                            r -> r.getInformacionPago().getTipo() != null ? r.getInformacionPago().getTipo() : "No especificado",
                            Collectors.counting()
                    ));
            
            // Calcular información adicional
            double pagoPromedio = totalPagosCompletados > 0 ? totalGastado / totalPagosCompletados : 0.0;
            Optional<Reserva> primerPago = pagosPagados.stream().min((r1, r2) -> {
                if (r1.getInformacionPago().getFecha() != null && r2.getInformacionPago().getFecha() != null) {
                    return r1.getInformacionPago().getFecha().compareTo(r2.getInformacionPago().getFecha());
                }
                return r1.getFecha().compareTo(r2.getFecha());
            });
            
            // Agregar al modelo
            model.addAttribute("pagosPagados", pagosPagados);
            model.addAttribute("totalPagosCompletados", totalPagosCompletados);
            model.addAttribute("totalGastado", totalGastado);
            model.addAttribute("pagoPromedio", pagoPromedio);
            model.addAttribute("pagosPorMetodo", pagosPorMetodo);
            model.addAttribute("hayHistorialPagos", !pagosPagados.isEmpty());
            model.addAttribute("primerPago", primerPago.orElse(null));
            model.addAttribute("debug", true);
            
            System.out.println("DEBUG DIAGNÓSTICO: Total pagos=" + totalPagosCompletados + 
                              ", Total gastado=S/ " + totalGastado + 
                              ", Métodos=" + pagosPorMetodo.size());
            
        } catch (Exception e) {
            System.err.println("ERROR EN DIAGNÓSTICO: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al obtener datos: " + e.getMessage());
            model.addAttribute("hayHistorialPagos", false);
            model.addAttribute("totalPagosCompletados", 0L);
            model.addAttribute("totalGastado", 0.0);
            model.addAttribute("pagoPromedio", 0.0);
            model.addAttribute("pagosPorMetodo", Collections.emptyMap());
            model.addAttribute("pagosPagados", Collections.emptyList());
            model.addAttribute("primerPago", null);
            model.addAttribute("debug", true);
        }
        
        return "Vecino/diagnostico_simple";
    }

    @GetMapping("/generar-comprobante-pdf/{idReserva}")
    public ResponseEntity<byte[]> generarComprobantePDF(@PathVariable Integer idReserva) {
        try {
            System.out.println("DEBUG PDF: Generando comprobante para reserva ID: " + idReserva);
            
            Usuario usuario = obtenerUsuarioAutenticado();
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Buscar la reserva
            Optional<Reserva> optReserva = reservaRepository.findById(idReserva);
            if (optReserva.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Reserva reserva = optReserva.get();
            
            // Verificar que la reserva pertenece al usuario autenticado
            if (!reserva.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Verificar que tiene información de pago y está pagado
            if (reserva.getInformacionPago() == null || 
                !"Pagado".equalsIgnoreCase(reserva.getInformacionPago().getEstado())) {
                return ResponseEntity.badRequest().build();
            }
            
            // Generar contenido HTML simple para el PDF
            String htmlContent = generarHTMLComprobante(reserva);
            
            // Por ahora, devolver como HTML (puedes agregar una librería de PDF después)
            byte[] htmlBytes = htmlContent.getBytes("UTF-8");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            headers.setContentDispositionFormData("attachment", 
                "comprobante_pago_" + idReserva + ".html");
            
            System.out.println("DEBUG PDF: Comprobante generado exitosamente para reserva " + idReserva);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(htmlBytes);
                    
        } catch (Exception e) {
            System.err.println("ERROR PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private String generarHTMLComprobante(Reserva reserva) {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formateadorHora = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter formateadorCompleto = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        return String.format("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <title>Comprobante de Pago - Reserva #%d</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    .header { text-align: center; border-bottom: 2px solid #007bff; padding-bottom: 20px; margin-bottom: 30px; }
                    .logo { color: #007bff; font-size: 24px; font-weight: bold; }
                    .subtitle { color: #6c757d; margin-top: 5px; }
                    .section { margin-bottom: 25px; }
                    .section-title { background: #f8f9fa; padding: 10px; border-left: 4px solid #007bff; font-weight: bold; margin-bottom: 15px; }
                    .info-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #eee; }
                    .label { font-weight: bold; color: #495057; }
                    .value { color: #212529; }
                    .total { background: #e7f3ff; padding: 15px; border: 2px solid #007bff; border-radius: 5px; text-align: center; }
                    .total .amount { font-size: 24px; font-weight: bold; color: #007bff; }
                    .footer { text-align: center; margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; color: #6c757d; font-size: 12px; }
                    .estado-pagado { background: #d4edda; color: #155724; padding: 5px 10px; border-radius: 3px; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="logo">🏟️ Municipalidad de San Miguel</div>
                    <div class="subtitle">Comprobante de Pago - Servicios Deportivos</div>
                </div>
                
                <div class="section">
                    <div class="section-title">📋 Información de la Reserva</div>
                    <div class="info-row">
                        <span class="label">Número de Reserva:</span>
                        <span class="value">#%d</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Complejo Deportivo:</span>
                        <span class="value">%s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Servicio:</span>
                        <span class="value">%s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Zona/Sector:</span>
                        <span class="value">%s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Fecha de Reserva:</span>
                        <span class="value">%s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Horario:</span>
                        <span class="value">%s - %s</span>
                    </div>
                </div>
                
                <div class="section">
                    <div class="section-title">👤 Información del Cliente</div>
                    <div class="info-row">
                        <span class="label">Nombre:</span>
                        <span class="value">%s %s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Email:</span>
                        <span class="value">%s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">DNI:</span>
                        <span class="value">%s</span>
                    </div>
                </div>
                
                <div class="section">
                    <div class="section-title">💳 Información del Pago</div>
                    <div class="info-row">
                        <span class="label">Estado:</span>
                        <span class="value"><span class="estado-pagado">✅ PAGADO</span></span>
                    </div>
                    <div class="info-row">
                        <span class="label">Método de Pago:</span>
                        <span class="value">%s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Fecha de Pago:</span>
                        <span class="value">%s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Hora de Pago:</span>
                        <span class="value">%s</span>
                    </div>
                </div>
                
                <div class="total">
                    <div>MONTO PAGADO</div>
                    <div class="amount">S/ %.2f</div>
                </div>
                
                <div class="footer">
                    <p><strong>Comprobante generado el:</strong> %s</p>
                    <p>Este comprobante es válido como constancia de pago para servicios deportivos municipales.</p>
                    <p>Para consultas: Mesa de Partes - Municipalidad de San Miguel</p>
                    <p><em>Documento generado automáticamente por el Sistema de Gestión Deportiva</em></p>
                </div>
            </body>
            </html>
            """,
            reserva.getIdReserva(),
            reserva.getIdReserva(),
            reserva.getInstanciaServicio().getComplejoDeportivo().getNombre(),
            reserva.getInstanciaServicio().getServicio().getNombre(),
            reserva.getInstanciaServicio().getComplejoDeportivo().getSector() != null ? 
                reserva.getInstanciaServicio().getComplejoDeportivo().getSector().getNombre() : "Zona Principal",
            reserva.getFecha().format(formateadorFecha),
            reserva.getHoraInicio().format(formateadorHora),
            reserva.getHoraFin().format(formateadorHora),
            reserva.getUsuario().getNombre(),
            reserva.getUsuario().getApellido(),
            reserva.getUsuario().getCredencial().getCorreo(),
            reserva.getUsuario().getDni() != null ? reserva.getUsuario().getDni() : "No especificado",
            reserva.getInformacionPago().getTipo() != null ? 
                reserva.getInformacionPago().getTipo() : "No especificado",
            reserva.getInformacionPago().getFecha() != null ? 
                reserva.getInformacionPago().getFecha().format(formateadorFecha) : "No registrada",
            reserva.getInformacionPago().getHora() != null ? 
                reserva.getInformacionPago().getHora().format(formateadorHora) : "No registrada",
            reserva.getInformacionPago().getTotal(),
            ahora.format(formateadorCompleto)
        );
    }

}
