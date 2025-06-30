// ...existing code...

    @GetMapping("/pagos")
    public String mostrarPasarelaPago(Model model, @RequestParam(value = "codigoCupon", required = false) String codigoCupon,
                                      @RequestParam(value = "idReservaCupon", required = false) Integer idReservaCupon) {
        Usuario usuario = obtenerUsuarioAutenticado();

        if (usuario == null) {
            return "redirect:/login";
        }

        List<Reserva> reservasPendientes = reservaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .stream()
                .filter(r -> r.getEstado() == 0 && r.getInformacionPago() != null
                        && "Pendiente".equalsIgnoreCase(r.getInformacionPago().getEstado()))
                .toList();

        // Verificar si hay reservas pendientes
        boolean hayReservasPendientes = !reservasPendientes.isEmpty();

        double total = reservasPendientes.stream()
                .mapToDouble(r -> r.getInformacionPago().getTotal().doubleValue())
                .sum();

        double descuentoAplicado = 0;
        double totalConDescuento = total;

        // Si hay cupón y reserva seleccionada, aplicar solo a esa reserva
        if (codigoCupon != null && !codigoCupon.isEmpty() && idReservaCupon != null) {
            Map<String, Object> descuentoData = aplicarDescuento(codigoCupon, idReservaCupon);
            if ((boolean) descuentoData.get("valido")) {
                descuentoAplicado = (double) descuentoData.get("descuento");
                totalConDescuento = total - descuentoAplicado;
                model.addAttribute("descuento", descuentoAplicado);
                model.addAttribute("totalConDescuento", totalConDescuento);
                model.addAttribute("mensajeDescuento", descuentoData.get("mensaje"));
            } else {
                model.addAttribute("mensajeDescuento", descuentoData.get("mensaje"));
            }
        }

        Credencial credencial = credencialRepository.findByCorreo(usuario.getCredencial().getCorreo());

        model.addAttribute("usuario", usuario);
        model.addAttribute("credencial", credencial);
        model.addAttribute("reservasPendientes", reservasPendientes);
        model.addAttribute("hayReservasPendientes", hayReservasPendientes);
        model.addAttribute("totalReservas", total);
        model.addAttribute("descuento", descuentoAplicado);
        model.addAttribute("totalConDescuento", totalConDescuento);

        return "Vecino/vecino_pagos";
    }

    @PostMapping("/guardarPago")
    public String guardarPago(@RequestParam("idReserva") Integer idReserva,
                              @RequestParam("codigoCupon") String codigoCupon,
                              RedirectAttributes redirectAttributes) {

        System.out.println("=== PROCESANDO PAGO ===");
        System.out.println("ID Reserva: " + idReserva);
        System.out.println("Código Cupón: " + codigoCupon);

        Optional<Reserva> optReserva = reservaRepository.findById(idReserva);
        if (optReserva.isEmpty()) {
            System.out.println("ERROR: No se encontró la reserva con ID: " + idReserva);
            redirectAttributes.addFlashAttribute("error", "No se encontró la reserva.");
            return "redirect:/vecino/pagos";
        }

        Reserva reserva = optReserva.get();
        System.out.println("Reserva encontrada. Estado actual: " + reserva.getEstado());

        if (reserva.getInformacionPago() == null) {
            System.out.println("ERROR: La reserva no tiene información de pago asociada");
            redirectAttributes.addFlashAttribute("error", "La reserva no tiene información de pago asociada.");
            return "redirect:/vecino/pagos";
        }

        double total = reserva.getInformacionPago().getTotal().doubleValue();
        System.out.println("Total original: " + total);

        // Usamos aplicarDescuento que solo necesita el ID de la reserva
        Map<String, Object> descuentoData = aplicarDescuento(codigoCupon, idReserva);

        if ((boolean) descuentoData.get("valido")) {
            double descuentoAplicado = (double) descuentoData.get("descuento");
            double totalConDescuento = (double) descuentoData.get("totalConDescuento");
            
            System.out.println("Descuento aplicado: " + descuentoAplicado);
            System.out.println("Total con descuento: " + totalConDescuento);

            try {
                // Actualizar información de pago
                InformacionPago pago = reserva.getInformacionPago();
                pago.setTotal(totalConDescuento);
                pago.setEstado("Pagado");
                pago.setFechaPago(new Date()); // Agregar fecha de pago
                informacionPagoRepository.save(pago);
                System.out.println("Información de pago actualizada correctamente");

                // Actualizar estado de la reserva
                int estadoAnterior = reserva.getEstado();
                reserva.setEstado(1); // 1 = Pagado/Activo
                reservaRepository.save(reserva);
                
                System.out.println("Estado de reserva actualizado de " + estadoAnterior + " a " + reserva.getEstado());
                System.out.println("=== PAGO PROCESADO EXITOSAMENTE ===");

                redirectAttributes.addFlashAttribute("mensajeExito", 
                    "Pago realizado correctamente. Su reserva ahora está activa.");
                return "redirect:/vecino/misReservas";
                
            } catch (Exception e) {
                System.out.println("ERROR al guardar el pago: " + e.getMessage());
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", 
                    "Error al procesar el pago. Por favor, intente nuevamente.");
                return "redirect:/vecino/pagos";
            }
        } else {
            System.out.println("ERROR: Cupón inválido - " + descuentoData.get("mensaje"));
            redirectAttributes.addFlashAttribute("error", descuentoData.get("mensaje"));
            return "redirect:/vecino/pagos";
        }
    }    // Método duplicado eliminado - usar el método validarCupon() que está más abajo

    /**
     * Simula el procesamiento de pago con diferentes métodos de pago
     */
    @PostMapping("/procesarPagoConMetodo")
    public String procesarPagoConMetodo(@RequestParam("idReserva") Integer idReserva,
                                      @RequestParam("metodoPago") String metodoPago,
                                      @RequestParam(value = "codigoCupon", required = false) String codigoCupon,
                                      RedirectAttributes redirectAttributes) {

        System.out.println("=== PROCESANDO PAGO CON MÉTODO ===");
        System.out.println("ID Reserva: " + idReserva);
        System.out.println("Método de Pago: " + metodoPago);
        System.out.println("Código Cupón: " + codigoCupon);

        Optional<Reserva> optReserva = reservaRepository.findById(idReserva);
        if (optReserva.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se encontró la reserva.");
            return "redirect:/vecino/pagos";
        }

        Reserva reserva = optReserva.get();

        try {
            // Simular procesamiento según el método de pago
            switch (metodoPago.toLowerCase()) {
                case "tarjeta":
                    // Pago con tarjeta: Procesamiento automático (sin necesidad de aprobación)
                    return procesarPagoTarjeta(reserva, codigoCupon, redirectAttributes);
                case "yape":
                case "plin":
                    // Pagos móviles: Procesamiento automático
                    return procesarPagoMovil(reserva, codigoCupon, metodoPago, redirectAttributes);
                case "transferencia":
                    // Transferencia bancaria: Requiere verificación del administrador
                    return procesarPagoTransferencia(reserva, codigoCupon, redirectAttributes);
                case "comprobante":
                case "voucher":
                    // Comprobante de pago (foto/PDF): Requiere aprobación del administrador
                    return procesarPagoComprobante(reserva, codigoCupon, redirectAttributes);
                default:
                    redirectAttributes.addFlashAttribute("error", "Método de pago no válido.");
                    return "redirect:/vecino/pagos";
            }
            }
        } catch (Exception e) {
            System.out.println("ERROR en procesamiento de pago: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al procesar el pago. Intente nuevamente.");
            return "redirect:/vecino/pagos";
        }
    }

    /**
     * Procesa pago con tarjeta de crédito/débito
     */
    private String procesarPagoTarjeta(Reserva reserva, String codigoCupon, RedirectAttributes redirectAttributes) {
        System.out.println("=== PROCESANDO PAGO CON TARJETA ===");
        System.out.println("ID Reserva: " + reserva.getIdReserva());
        System.out.println("Estado actual reserva: " + reserva.getEstado());
        System.out.println("Estado actual pago: " + reserva.getInformacionPago().getEstado());
        
        double total = calcularTotalConDescuento(reserva, codigoCupon);
        System.out.println("Total calculado: " + total);
        
        // Simular procesamiento de tarjeta (siempre exitoso en demo)
        System.out.println("Simulación de pago con tarjeta exitosa, llamando a confirmarPago...");
        return confirmarPago(reserva, total, "Tarjeta", redirectAttributes);
    }

    /**
     * Procesa pago móvil (Yape/Plin) - Automático
     */
    private String procesarPagoMovil(Reserva reserva, String codigoCupon, String metodo, RedirectAttributes redirectAttributes) {
        System.out.println("=== PROCESANDO PAGO MÓVIL (" + metodo.toUpperCase() + ") ===");
        System.out.println("Procesando pago móvil con " + metodo + " (automático)...");
        
        double total = calcularTotalConDescuento(reserva, codigoCupon);
        
        // Simular procesamiento móvil (siempre exitoso en demo y automático)
        return confirmarPago(reserva, total, metodo, redirectAttributes);
    }

    /**
     * Procesa pago por transferencia bancaria o comprobante
     */
    private String procesarPagoTransferencia(Reserva reserva, String codigoCupon, RedirectAttributes redirectAttributes) {
        System.out.println("Procesando pago por transferencia/comprobante...");
        
        double total = calcularTotalConDescuento(reserva, codigoCupon);
        
        // Para transferencias/comprobantes, el estado queda pendiente de verificación del administrador
        InformacionPago pago = reserva.getInformacionPago();
        pago.setTotal(BigDecimal.valueOf(total));
        pago.setEstado("Pendiente_Verificacion");
        pago.setFechaPago(new Date());
        informacionPagoRepository.saveAndFlush(pago);

        // Estado 2 = Pendiente de Verificación del Administrador
        reserva.setEstado(2); 
        reservaRepository.saveAndFlush(reserva);

        System.out.println("Pago por comprobante registrado como pendiente de verificación del administrador");
        
        redirectAttributes.addFlashAttribute("mensajeInfo", 
            "Comprobante de pago enviado. Un administrador verificará su pago en las próximas horas y activará su reserva.");
        return "redirect:/vecino/misReservas";
    }

    /**
     * Procesa pago con comprobante (foto/PDF)
     */
    private String procesarPagoComprobante(Reserva reserva, String codigoCupon, RedirectAttributes redirectAttributes) {
        System.out.println("Procesando pago con comprobante (requiere aprobación del administrador)...");
        
        double total = calcularTotalConDescuento(reserva, codigoCupon);
        
        // El comprobante requiere aprobación del administrador
        InformacionPago pago = reserva.getInformacionPago();
        pago.setTotal(BigDecimal.valueOf(total));
        pago.setEstado("Pendiente_Verificacion");
        pago.setFechaPago(new Date());
        informacionPagoRepository.saveAndFlush(pago);

        // Estado 2 = Pendiente de Verificación del Administrador
        reserva.setEstado(2);
        reservaRepository.saveAndFlush(reserva);

        System.out.println("Comprobante registrado como pendiente de verificación del administrador");
        
        redirectAttributes.addFlashAttribute("mensajeInfo", 
            "Comprobante de pago enviado correctamente. Un administrador verificará su pago y activará su reserva.");
        return "redirect:/vecino/misReservas";
    }

    /**
     * Calcula el total con descuento aplicado
     */
    private double calcularTotalConDescuento(Reserva reserva, String codigoCupon) {
        double total = reserva.getInformacionPago().getTotal().doubleValue();
        
        if (codigoCupon != null && !codigoCupon.trim().isEmpty()) {
            Map<String, Object> descuentoData = aplicarDescuento(codigoCupon, reserva.getIdReserva());
            if ((boolean) descuentoData.get("valido")) {
                total = (double) descuentoData.get("totalConDescuento");
                System.out.println("Descuento aplicado. Total: " + total);
            }
        }
        
        return total;
    }

    /**
     * Confirma el pago automático (tarjeta de crédito/débito)
     */
    @Transactional
    private String confirmarPago(Reserva reserva, double total, String metodoPago, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== INICIANDO CONFIRMACIÓN DE PAGO AUTOMÁTICO (TARJETA) ===");
            System.out.println("Reserva ID: " + reserva.getIdReserva());
            System.out.println("Estado actual reserva: " + reserva.getEstado());
            System.out.println("Estado actual pago: " + reserva.getInformacionPago().getEstado());
            
            // Actualizar información de pago
            InformacionPago pago = reserva.getInformacionPago();
            pago.setTotal(BigDecimal.valueOf(total));
            pago.setEstado("Pagado");
            pago.setFechaPago(new Date());
            
            System.out.println("Guardando información de pago...");
            InformacionPago pagoGuardado = informacionPagoRepository.saveAndFlush(pago);
            System.out.println("Pago guardado con estado: " + pagoGuardado.getEstado());

            // Actualizar estado de la reserva a ACTIVO (los pagos con tarjeta son automáticos)
            System.out.println("Actualizando estado de reserva a ACTIVO (1) - Pago con tarjeta procesado automáticamente...");
            reserva.setEstado(1); // 1 = Activo/Pagado
            Reserva reservaGuardada = reservaRepository.saveAndFlush(reserva);
            System.out.println("Reserva guardada con estado: " + reservaGuardada.getEstado());
            
            // Verificación adicional
            Optional<Reserva> verificacion = reservaRepository.findById(reserva.getIdReserva());
            if (verificacion.isPresent()) {
                System.out.println("VERIFICACIÓN FINAL - Estado en BD: " + verificacion.get().getEstado());
                System.out.println("VERIFICACIÓN FINAL - Estado pago en BD: " + verificacion.get().getInformacionPago().getEstado());
            }

            System.out.println("Pago con tarjeta confirmado exitosamente y reserva activada automáticamente. Método: " + metodoPago + ", Total: " + total);

            redirectAttributes.addFlashAttribute("mensajeExito", 
                "¡Pago procesado exitosamente! Su reserva ha sido activada automáticamente.");
            return "redirect:/vecino/misReservas";

        } catch (Exception e) {
            System.err.println("Error al confirmar pago: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensajeError", 
                "Error al procesar el pago: " + e.getMessage());
            return "redirect:/vecino/pagos";
        }
    } 
                "¡Pago realizado correctamente con " + metodoPago + "! Su reserva está ahora activa.");
            return "redirect:/vecino/misReservas";

        } catch (Exception e) {
            System.out.println("ERROR al confirmar pago: " + e.getMessage());
            throw new RuntimeException("Error al confirmar el pago", e);
        }
    }

    /**
     * Webhook para confirmación de pagos externos (simulado)
     */
    @PostMapping("/webhook/confirmarPago")
    @ResponseBody
    public Map<String, Object> webhookConfirmarPago(@RequestParam("idReserva") Integer idReserva,
                                                   @RequestParam("estado") String estado,
                                                   @RequestParam("transactionId") String transactionId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== WEBHOOK CONFIRMACIÓN PAGO ===");
            System.out.println("ID Reserva: " + idReserva);
            System.out.println("Estado: " + estado);
            System.out.println("Transaction ID: " + transactionId);

            Optional<Reserva> optReserva = reservaRepository.findById(idReserva);
            if (optReserva.isEmpty()) {
                response.put("success", false);
                response.put("message", "Reserva no encontrada");
                return response;
            }

            Reserva reserva = optReserva.get();
            InformacionPago pago = reserva.getInformacionPago();

            if ("success".equalsIgnoreCase(estado) || "approved".equalsIgnoreCase(estado)) {
                // Pago exitoso
                pago.setEstado("Pagado");
                reserva.setEstado(1); // Activo
                
                System.out.println("Pago confirmado por webhook");
            } else if ("failed".equalsIgnoreCase(estado) || "rejected".equalsIgnoreCase(estado)) {
                // Pago fallido
                pago.setEstado("Rechazado");
                reserva.setEstado(3); // Rechazado
                
                System.out.println("Pago rechazado por webhook");
            }

            pago.setFechaPago(new Date());
            informacionPagoRepository.save(pago);
            reservaRepository.save(reserva);

            response.put("success", true);
            response.put("message", "Estado actualizado correctamente");
            
        } catch (Exception e) {
            System.out.println("ERROR en webhook: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error interno del servidor");
        }

        return response;
    }

    /**
     * Endpoint para obtener el estado actualizado de una reserva
     */
    @GetMapping("/api/reserva/{id}/estado")
    @ResponseBody
    public Map<String, Object> obtenerEstadoReserva(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Reserva> optReserva = reservaRepository.findById(id);
            if (optReserva.isEmpty()) {
                response.put("success", false);
                response.put("message", "Reserva no encontrada");
                return response;
            }
            
            Reserva reserva = optReserva.get();
            String estadoTexto;
            String estadoClase;
            
            switch (reserva.getEstado()) {
                case 0:
                    estadoTexto = "Pendiente de Pago";
                    estadoClase = "badge bg-warning";
                    break;
                case 1:
                    estadoTexto = "Activa";
                    estadoClase = "badge bg-success";
                    break;
                case 2:
                    estadoTexto = "Pendiente de Verificación";
                    estadoClase = "badge bg-info";
                    break;
                case 3:
                    estadoTexto = "Rechazada";
                    estadoClase = "badge bg-danger";
                    break;
                default:
                    estadoTexto = "Estado Desconocido";
                    estadoClase = "badge bg-secondary";
            }
            
            response.put("success", true);
            response.put("estado", reserva.getEstado());
            response.put("estadoTexto", estadoTexto);
            response.put("estadoClase", estadoClase);
            
            if (reserva.getInformacionPago() != null) {
                response.put("estadoPago", reserva.getInformacionPago().getEstado());
                response.put("totalPago", reserva.getInformacionPago().getTotal());
                response.put("fechaPago", reserva.getInformacionPago().getFechaPago());
            }
            
        } catch (Exception e) {
            System.out.println("ERROR al obtener estado de reserva: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error interno del servidor");
        }
        
        return response;
    }

    /**
     * Página de confirmación de pago
     */
    @GetMapping("/confirmacion-pago")
    public String mostrarConfirmacionPago(@RequestParam("idReserva") Integer idReserva, 
                                        Model model) {
        
        try {
            Optional<Reserva> optReserva = reservaRepository.findById(idReserva);
            if (optReserva.isPresent()) {
                Reserva reserva = optReserva.get();
                model.addAttribute("reserva", reserva);
                model.addAttribute("mensajeExito", "¡Pago procesado exitosamente!");
                
                // Agregar información adicional para la vista
                if (reserva.getInformacionPago() != null) {
                    model.addAttribute("informacionPago", reserva.getInformacionPago());
                }
                
                return "Vecino/confirmacion_pago";
            } else {
                model.addAttribute("error", "No se encontró la reserva especificada");
                return "redirect:/vecino/misReservas";
            }
        } catch (Exception e) {
            System.out.println("ERROR en página de confirmación: " + e.getMessage());
            model.addAttribute("error", "Error al cargar la confirmación de pago");
            return "redirect:/vecino/misReservas";
        }
    }

    /**
     * Método de debug para verificar y forzar actualización del estado de reserva
     */
    @PostMapping("/debug/actualizarEstadoReserva")
    @ResponseBody
    public String debugActualizarEstado(@RequestParam("idReserva") Integer idReserva) {
        try {
            Optional<Reserva> optReserva = reservaRepository.findById(idReserva);
            if (optReserva.isEmpty()) {
                return "ERROR: Reserva no encontrada con ID: " + idReserva;
            }

            Reserva reserva = optReserva.get();
            System.out.println("=== DEBUG ACTUALIZACIÓN ESTADO ===");
            System.out.println("Estado ANTES: " + reserva.getEstado());
            System.out.println("Estado pago ANTES: " + reserva.getInformacionPago().getEstado());

            // Forzar actualización
            reserva.setEstado(1); // Pagado
            reserva.getInformacionPago().setEstado("Pagado");
            reserva.getInformacionPago().setFechaPago(new Date());
            
            // Guardar con flush forzado
            reservaRepository.saveAndFlush(reserva);
            informacionPagoRepository.saveAndFlush(reserva.getInformacionPago());

            // Verificar actualización
            Reserva reservaActualizada = reservaRepository.findById(idReserva).get();
            System.out.println("Estado DESPUÉS: " + reservaActualizada.getEstado());
            System.out.println("Estado pago DESPUÉS: " + reservaActualizada.getInformacionPago().getEstado());

            return "SUCCESS: Reserva actualizada. Estado: " + reservaActualizada.getEstado() + 
                   ", Estado pago: " + reservaActualizada.getInformacionPago().getEstado();

        } catch (Exception e) {
            System.out.println("ERROR en debug: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Método de debug para verificar el estado actual de una reserva
     */
    @GetMapping("/debug/estado/{idReserva}")
    @ResponseBody
    public Map<String, Object> debugEstadoReserva(@PathVariable Integer idReserva) {
        Map<String, Object> debug = new HashMap<>();
        
        try {
            Optional<Reserva> optReserva = reservaRepository.findById(idReserva);
            if (optReserva.isPresent()) {
                Reserva reserva = optReserva.get();
                debug.put("success", true);
                debug.put("idReserva", reserva.getIdReserva());
                debug.put("estadoReserva", reserva.getEstado());
                debug.put("estadoPago", reserva.getInformacionPago().getEstado());
                debug.put("totalPago", reserva.getInformacionPago().getTotal());
                debug.put("fechaPago", reserva.getInformacionPago().getFechaPago());
                
                // Descripción del estado
                String estadoDesc;
                switch (reserva.getEstado()) {
                    case 0: estadoDesc = "Pendiente"; break;
                    case 1: estadoDesc = "Activo/Pagado"; break;
                    case 2: estadoDesc = "Pendiente Verificación"; break;
                    case 3: estadoDesc = "Rechazado"; break;
                    default: estadoDesc = "Desconocido"; break;
                }
                debug.put("estadoDescripcion", estadoDesc);
                
            } else {
                debug.put("success", false);
                debug.put("message", "Reserva no encontrada");
            }
        } catch (Exception e) {
            debug.put("success", false);
            debug.put("error", e.getMessage());
        }
        
        return debug;
    }

    /**
     * Método para forzar actualización de estado (solo para debug)
     */
    @PostMapping("/debug/forzarEstado")
    @ResponseBody
    @Transactional
    public Map<String, Object> forzarEstadoReserva(@RequestParam Integer idReserva, 
                                                   @RequestParam Integer nuevoEstado) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== FORZANDO ACTUALIZACIÓN DE ESTADO ===");
            System.out.println("ID Reserva: " + idReserva);
            System.out.println("Nuevo Estado: " + nuevoEstado);
            
            Optional<Reserva> optReserva = reservaRepository.findById(idReserva);
            if (optReserva.isPresent()) {
                Reserva reserva = optReserva.get();
                System.out.println("Estado anterior: " + reserva.getEstado());
                
                reserva.setEstado(nuevoEstado);
                Reserva guardada = reservaRepository.saveAndFlush(reserva);
                
                // También actualizar el pago si es estado activo
                if (nuevoEstado == 1) {
                    InformacionPago pago = reserva.getInformacionPago();
                    pago.setEstado("Pagado");
                    pago.setFechaPago(new Date());
                    informacionPagoRepository.saveAndFlush(pago);
                    System.out.println("Estado pago actualizado a: Pagado");
                }
                
                System.out.println("Estado actualizado a: " + guardada.getEstado());
                
                response.put("success", true);
                response.put("estadoAnterior", optReserva.get().getEstado());
                response.put("estadoNuevo", guardada.getEstado());
                response.put("message", "Estado actualizado correctamente");
                
            } else {
                response.put("success", false);
                response.put("message", "Reserva no encontrada");
            }
        } catch (Exception e) {
            System.out.println("ERROR al forzar estado: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
