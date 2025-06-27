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
                    return procesarPagoTarjeta(reserva, codigoCupon, redirectAttributes);
                case "yape":
                case "plin":
                    return procesarPagoMovil(reserva, codigoCupon, metodoPago, redirectAttributes);
                case "transferencia":
                    return procesarPagoTransferencia(reserva, codigoCupon, redirectAttributes);
                default:
                    redirectAttributes.addFlashAttribute("error", "Método de pago no válido.");
                    return "redirect:/vecino/pagos";
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
        System.out.println("Procesando pago con tarjeta...");
        
        double total = calcularTotalConDescuento(reserva, codigoCupon);
        
        // Simular procesamiento de tarjeta (siempre exitoso en demo)
        return confirmarPago(reserva, total, "Tarjeta", redirectAttributes);
    }

    /**
     * Procesa pago móvil (Yape/Plin)
     */
    private String procesarPagoMovil(Reserva reserva, String codigoCupon, String metodo, RedirectAttributes redirectAttributes) {
        System.out.println("Procesando pago móvil con " + metodo + "...");
        
        double total = calcularTotalConDescuento(reserva, codigoCupon);
        
        // Simular procesamiento móvil (siempre exitoso en demo)
        return confirmarPago(reserva, total, metodo, redirectAttributes);
    }

    /**
     * Procesa pago por transferencia bancaria
     */
    private String procesarPagoTransferencia(Reserva reserva, String codigoCupon, RedirectAttributes redirectAttributes) {
        System.out.println("Procesando pago por transferencia...");
        
        double total = calcularTotalConDescuento(reserva, codigoCupon);
        
        // Para transferencias, el estado queda pendiente de verificación
        InformacionPago pago = reserva.getInformacionPago();
        pago.setTotal(BigDecimal.valueOf(total));
        pago.setEstado("Pendiente_Verificacion");
        pago.setFechaPago(new Date());
        informacionPagoRepository.save(pago);

        reserva.setEstado(2); // 2 = Pendiente de Verificación
        reservaRepository.save(reserva);

        System.out.println("Pago por transferencia registrado como pendiente de verificación");
        
        redirectAttributes.addFlashAttribute("mensajeExito", 
            "Transferencia registrada. Su pago será verificado en las próximas horas.");
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
     * Confirma el pago y actualiza los estados
     */
    private String confirmarPago(Reserva reserva, double total, String metodoPago, RedirectAttributes redirectAttributes) {
        try {
            // Actualizar información de pago
            InformacionPago pago = reserva.getInformacionPago();
            pago.setTotal(BigDecimal.valueOf(total));
            pago.setEstado("Pagado");
            pago.setFechaPago(new Date());
            pago.setMetodoPago(metodoPago); // Si existe este campo en la entidad
            informacionPagoRepository.save(pago);

            // Actualizar estado de la reserva
            reserva.setEstado(1); // 1 = Pagado/Activo
            reservaRepository.save(reserva);

            System.out.println("Pago confirmado exitosamente. Método: " + metodoPago + ", Total: " + total);

            redirectAttributes.addFlashAttribute("mensajeExito", 
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

// ...existing code...
