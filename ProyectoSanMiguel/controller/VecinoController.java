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
        model.addAttribute("totalReservas", total);
        model.addAttribute("descuento", descuentoAplicado);
        model.addAttribute("totalConDescuento", totalConDescuento);

        return "Vecino/vecino_pagos";
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
            // Opcional: guardar el descuento aplicado en un campo si existe
            // pago.setDescuento(BigDecimal.valueOf(descuentoAplicado));
            informacionPagoRepository.save(pago);

            reserva.setEstado(1); // Pagado
            reservaRepository.save(reserva);

            redirectAttributes.addFlashAttribute("mensajeExito", "Pago realizado correctamente.");
            return "redirect:/vecino/misReservas";
        } else {
            redirectAttributes.addFlashAttribute("error", descuentoData.get("mensaje"));
            return "redirect:/vecino/pagos";
        }
    }

    @PostMapping("/aplicarDescuento")
    @ResponseBody
    public Map<String, Object> aplicarDescuento(@RequestParam("codigo") String codigo,
                                                @RequestParam("idReserva") Integer idReserva) {
        Map<String, Object> resp = new HashMap<>();

        if (idReserva == null) {
            resp.put("valido", false);
            resp.put("mensaje", "El ID de reserva no puede ser nulo.");
            return resp;
        }

        LocalDate hoy = LocalDate.now();

        Optional<Descuento> descOpt = descuentoRepository.findByCodigoAndFechaInicioLessThanEqualAndFechaFinalGreaterThanEqual(
                codigo.trim(), hoy, hoy);

        if (descOpt.isEmpty()) {
            resp.put("valido", false);
            resp.put("mensaje", "Código de descuento inválido o expirado.");
            return resp;
        }

        Descuento descuento = descOpt.get();

        Optional<Reserva> reservaOpt = reservaRepository.findById(idReserva);
        if (reservaOpt.isEmpty()) {
            resp.put("valido", false);
            resp.put("mensaje", "Reserva no encontrada.");
            return resp;
        }

        Reserva reserva = reservaOpt.get();

        Integer idServicioReserva = reserva.getInstanciaServicio().getServicio().getIdServicio();

        if (!descuento.getServicio().getIdServicio().equals(idServicioReserva)) {
            resp.put("valido", false);
            resp.put("mensaje", "El descuento no aplica para este servicio.");
            return resp;
        }

        // Usar el total de la reserva (ya calculado en InformacionPago)
        double total = reserva.getInformacionPago().getTotal().doubleValue();

        double descuentoAplicado = 0.0;
        if ("PORCENTAJE".equalsIgnoreCase(descuento.getTipoDescuento())) {
            descuentoAplicado = total * (descuento.getValor() / 100.0);
        } else if ("FIJO".equalsIgnoreCase(descuento.getTipoDescuento())) {
            descuentoAplicado = descuento.getValor();
        }

        double totalConDescuento = Math.max(0, total - descuentoAplicado);

        resp.put("valido", true);
        resp.put("descuento", descuentoAplicado);
        resp.put("totalConDescuento", totalConDescuento);
        resp.put("mensaje", "Descuento aplicado correctamente.");

        return resp;
    }

// ...existing code...
