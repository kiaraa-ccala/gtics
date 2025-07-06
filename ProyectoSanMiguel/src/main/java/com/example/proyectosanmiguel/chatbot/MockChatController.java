package com.example.proyectosanmiguel.chatbot;

import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.text.Normalizer;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
@ConditionalOnProperty(name = "chatbot.enabled", havingValue = "false", matchIfMissing = true)
public class MockChatController {

    @Autowired
    private ComplejoRepository complejoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CredencialRepository credencialRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private InstanciaServicioRepository instanciaServicioRepository;

    @Autowired
    private InformacionPagoRepository informacionPagoRepository;

    @PostMapping("/chat")
    public String chat(@RequestParam String message, HttpServletRequest request) {
        Usuario usuario = obtenerUsuarioAutenticado();
        HttpSession session = request.getSession();
        return generateMockResponse(message, usuario, session);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam String message, HttpServletRequest request) {
        SseEmitter emitter = new SseEmitter(30000L);

        CompletableFuture.runAsync(() -> {
            try {
                Usuario usuario = obtenerUsuarioAutenticado();
                HttpSession session = request.getSession();
                String response = generateMockResponse(message, usuario, session);
                String[] words = response.split(" ");

                for (String word : words) {
                    emitter.send(SseEmitter.event().data(word + " "));
                    Thread.sleep(100);
                }

                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStreamGet(@RequestParam String message, HttpServletRequest request) {
        SseEmitter emitter = new SseEmitter(30000L);

        CompletableFuture.runAsync(() -> {
            try {
                Usuario usuario = obtenerUsuarioAutenticado();
                HttpSession session = request.getSession();
                String response = generateMockResponse(message, usuario, session);
                String[] words = response.split(" ");

                for (String word : words) {
                    emitter.send(SseEmitter.event().data(word + " "));
                    Thread.sleep(100);
                }

                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * Método para obtener el usuario autenticado de la sesión
     */

    private Usuario obtenerUsuarioAutenticado() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String correo;
            if (principal instanceof UserDetails) {
                correo = ((UserDetails) principal).getUsername();
            } else {
                correo = principal.toString();
            }

            Credencial credencial = credencialRepository.findByCorreo(correo);
            if (credencial != null) {
                return credencial.getUsuario();
            }
        } catch (Exception e) {
            System.out.println("Error al obtener usuario autenticado: " + e.getMessage());
        }
        return null;
    }

    private String generateMockResponse(String message, Usuario usuario, HttpSession session) {

        String text = message.toLowerCase().trim();
        String firstName = usuario != null ? usuario.getNombre() : null;
        Integer idUsuario = usuario != null ? usuario.getIdUsuario() : null;

        // Verificar si el usuario quiere cancelar una operación en curso
        if (text.equals("cancelar") || text.equals("cancel")) {
            limpiarEstadoSesion(session);
            return "❌ Operación cancelada. ¿En qué más puedo ayudarte?";
        }

        // Verificar si estamos en medio de un flujo de reserva
        String estado = (String) session.getAttribute("chatbot_estado");
        if (estado != null) {
            return manejarFlujoReserva(message, usuario, session, estado);
        }

        // Determinar intención básica según palabras clave
        String intent = determinarIntencion(text);

        // Manejar cada caso con switch-case
        switch (intent) {
            case "SALUDO":
                return generarSaludo(firstName);

            case "LISTAR_COMPLEJOS":
                return listarComplejos();

            case "LISTAR_INSTANCIAS":
                return listarInstanciasDeComplejo(text);

            case "RESERVA":
                return manejarConsultaReservas(idUsuario, text, session);

            case "HACER_RESERVA":
                if (idUsuario == null) {
                    return "📋 Para hacer una reserva necesitas iniciar sesión. Una vez autenticado podré ayudarte a gestionar tus reservas.";
                }
                return iniciarFlujoReserva(text, session);

            case "AGRADECIMIENTO":
                return generarDespedida(firstName);

            default:
                return "🤔 No estoy seguro de entender tu consulta. Puedo ayudarte con:\n" +
                        "• 📋 'listar complejos' - Ver todos los complejos disponibles\n" +
                        "• 🏟️ 'instancias del [complejo]' - Ver instalaciones de un complejo\n" +
                        "• 📅 'mis reservas' - Ver el resumen de tus reservas\n" +
                        "• ➕ 'hacer reserva' - Crear una nueva reserva\n\n" +
                        "¿Podrías reformular tu pregunta o elegir uno de estos temas?";
        }
    }

    /**
     * Determina la intención del usuario basada en el mensaje
     */
    private String determinarIntencion(String text) {
        if (text.contains("hola") || text.contains("buenos") || text.contains("buenas") || text.contains("saludos")) {
            return "SALUDO";
        } else if (text.contains("complejo") && (text.contains("listar") || text.contains("lista") || text.contains("mostrar") || text.contains("cuales") || text.contains("cuáles") || text.contains("ver"))) {
            return "LISTAR_COMPLEJOS";
        } else if ((text.contains("instancia") || text.contains("cancha") || text.contains("gimnasio") || text.contains("instalacion") || text.contains("instalación")) &&
                (text.contains("listar") || text.contains("lista") || text.contains("mostrar") || text.contains("cuales") || text.contains("cuáles") || text.contains("ver") || text.contains("del") || text.contains("de"))) {
            return "LISTAR_INSTANCIAS";
        } else if (text.contains("mis reservas") || text.contains("resumen") || text.contains("estado")) {
            return "RESERVA";
        } else if (text.contains("hacer reserva") || text.contains("nueva reserva") || text.contains("reservar") || text.contains("quiero reservar")) {
            return "HACER_RESERVA";
        } else if (text.contains("gracias") || text.contains("gracia")) {
            return "AGRADECIMIENTO";
        } else {
            return "DEFAULT";
        }
    }

    /**
     * Genera mensaje de saludo personalizado
     */
    private String generarSaludo(String firstName) {
        String saludo = firstName != null ? "¡Hola " + firstName + "!" : "¡Hola!";
        return saludo + " 👋 Soy el asistente virtual deportivo de la municipalidad de San Miguel. ¿En qué puedo ayudarte hoy?\n\n" +
                "Puedo ayudarte con:\n" +
                "• 📋 Listar complejos deportivos\n" +
                "• 🏟️ Ver instancias de un complejo específico\n" +
                "• 📅 Resumen de tus reservas\n" +
                "• ➕ Hacer una nueva reserva";
    }

    /**
     * Genera mensaje de despedida personalizado
     */
    private String generarDespedida(String firstName) {
        String despedida = firstName != null ? "¡De nada, " + firstName + "!" : "¡De nada!";
        return despedida + " 😊 Ha sido un placer ayudarte. Si necesitas hacer más consultas sobre nuestros servicios deportivos, no dudes en preguntarme. ¡Que tengas un excelente día!";
    }

    /**
     * Limpia el estado de la sesión del chatbot
     */
    private void limpiarEstadoSesion(HttpSession session) {
        session.removeAttribute("chatbot_estado");
        session.removeAttribute("chatbot_complejo_id");
        session.removeAttribute("chatbot_instancia_id");
        session.removeAttribute("chatbot_fecha");
        session.removeAttribute("chatbot_hora_inicio");
        session.removeAttribute("chatbot_hora_fin");
        session.removeAttribute("chatbot_monto_total");
        session.removeAttribute("chatbot_bloques");
    }

    /**
     * Maneja las consultas sobre reservas del usuario
     */
    private String manejarConsultaReservas(Integer idUsuario, String text, HttpSession session) {
        if (idUsuario == null) {
            return "📋 Para consultar tus reservas necesitas iniciar sesión. Una vez autenticado podré mostrarte el estado de todas tus reservas.";
        }

        try {
            List<Reserva> misReservas = reservaRepository.findByUsuario_IdUsuario(idUsuario);
            if (!misReservas.isEmpty()) {
                // Contar reservas por estado
                long reservasActivas = misReservas.stream()
                        .filter(r -> r.getEstado() == 1) // Estado activo/pagado
                        .count();
                long reservasPendientesPago = misReservas.stream()
                        .filter(r -> r.getEstado() == 0) // Estado pendiente de pago
                        .count();
                long reservasPendientesVerificacion = misReservas.stream()
                        .filter(r -> r.getEstado() == 2) // Estado pendiente de verificación
                        .count();
                long reservasRechazadas = misReservas.stream()
                        .filter(r -> r.getEstado() == 3) // Estado rechazado
                        .count();

                // Calcular monto total pendiente de pago
                double montoPendiente = misReservas.stream()
                        .filter(r -> r.getEstado() == 0 && r.getInformacionPago() != null)
                        .mapToDouble(r -> r.getInformacionPago().getTotal().doubleValue())
                        .sum();

                StringBuilder response = new StringBuilder();
                response.append("📋 Resumen de tus reservas:\n\n");
                response.append("• ✅ Reservas activas: ").append(reservasActivas).append("\n");
                response.append("• ⏳ Pendientes de pago: ").append(reservasPendientesPago).append("\n");
                
                if (reservasPendientesVerificacion > 0) {
                    response.append("• 🔍 Pendientes de verificación: ").append(reservasPendientesVerificacion).append("\n");
                }
                
                if (reservasRechazadas > 0) {
                    response.append("• ❌ Rechazadas: ").append(reservasRechazadas).append("\n");
                }
                
                response.append("• 📊 Total de reservas: ").append(misReservas.size()).append("\n\n");
                
                if (reservasPendientesPago > 0) {
                    response.append("💰 Monto pendiente de pago: S/ ").append(String.format("%.2f", montoPendiente)).append("\n\n");
                    response.append("💡 Tienes reservas pendientes de pago. Puedes proceder al pago desde tu panel de usuario.\n\n");
                }

                // Mostrar las próximas reservas
                List<Reserva> proximasReservas = misReservas.stream()
                        .filter(r -> r.getFecha().isAfter(LocalDate.now().minusDays(1)))
                        .sorted((r1, r2) -> r1.getFecha().compareTo(r2.getFecha()))
                        .limit(3)
                        .toList();

                if (!proximasReservas.isEmpty()) {
                    response.append("📅 Próximas reservas:\n");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    
                    for (Reserva reserva : proximasReservas) {
                        String estadoTexto = obtenerTextoEstado(reserva.getEstado());
                        response.append(String.format("   • %s - %s (%s a %s) - %s\n",
                                reserva.getFecha().format(formatter),
                                reserva.getInstanciaServicio().getNombre(),
                                reserva.getHoraInicio().toString(),
                                reserva.getHoraFin().toString(),
                                estadoTexto));
                    }
                    response.append("\n");
                }

                response.append("💡 ¿Te gustaría hacer una nueva reserva? Escribe 'hacer reserva' para comenzar.");
                return response.toString();
                
            } else {
                return "📋 Actualmente no tienes reservas registradas.\n\n" +
                        "💡 ¿Te gustaría hacer una nueva reserva? Escribe 'hacer reserva' para comenzar.";
            }
        } catch (Exception e) {
            System.out.println("Error al obtener reservas: " + e.getMessage());
            return "❌ Ocurrió un error al consultar tus reservas. Por favor, intenta nuevamente.";
        }
    }

    /**
     * Obtiene el texto descriptivo del estado de una reserva
     */
    private String obtenerTextoEstado(int estado) {
        switch (estado) {
            case 0: return "Pendiente de Pago";
            case 1: return "Activa";
            case 2: return "Pendiente de Verificación";
            case 3: return "Rechazada";
            default: return "Estado Desconocido";
        }
    }

    /**
     * Inicia el flujo de creación de reserva
     */
    private String iniciarFlujoReserva(String mensaje, HttpSession session) {
        // Limpiar cualquier estado anterior
        limpiarEstadoSesion(session);

        // Establecer estado inicial del flujo
        session.setAttribute("chatbot_estado", "SELECCIONANDO_COMPLEJO");

        return "🎯 ¡Perfecto! Te ayudo a hacer una reserva.\n\n" +
                "Primero necesito saber en qué complejo te gustaría reservar.\n\n" +
                listarComplejos() + "\n\n" +
                "💡 Escribe el nombre del complejo que te interesa, por ejemplo: 'Complejo Central'";
    }

    /**
     * Maneja el flujo conversacional de reserva
     */
    private String manejarFlujoReserva(String mensaje, Usuario usuario, HttpSession session, String estado) {

        String text = mensaje.toLowerCase().trim();
        Integer idUsuario = usuario != null ? usuario.getIdUsuario() : null;

        if (idUsuario == null) {
            limpiarEstadoSesion(session);
            return "❌ Para hacer una reserva necesitas estar autenticado. Por favor, inicia sesión e intenta nuevamente.";
        }

        switch (estado) {
            case "SELECCIONANDO_COMPLEJO":
                return procesarSeleccionComplejo(mensaje, session);

            case "SELECCIONANDO_INSTANCIA":
                return procesarSeleccionInstancia(mensaje, session);

            case "SELECCIONANDO_FECHA":
                return procesarSeleccionFecha(mensaje, session);

            case "SELECCIONANDO_HORARIO":
                return procesarSeleccionHorario(mensaje, session);

            case "CONFIRMANDO_RESERVA":
                return confirmarReserva(mensaje, usuario, session);

            default:
                limpiarEstadoSesion(session);
                return "❌ Ocurrió un error en el proceso. Por favor, inicia nuevamente tu consulta.";
        }
    }

    /**
     * Procesa la selección del complejo deportivo
     */
    private String procesarSeleccionComplejo(String mensaje, HttpSession session) {
        try {
            String nombreComplejo = extraerNombreComplejo(mensaje);

            if (nombreComplejo.isEmpty()) {
                return "🤔 No pude identificar el nombre del complejo. Por favor, escribe claramente el nombre.\n\n" +
                        "Ejemplo: 'Complejo Central' o simplemente 'Central'";
            }

            List<ComplejoDeportivo> complejosCandidatos = buscarComplejosPorNombre(nombreComplejo);

            if (complejosCandidatos.isEmpty()) {
                return String.format("❌ No encontré ningún complejo con el nombre '%s'.\n\n" +
                        "Por favor, elige uno de los complejos de la lista anterior.", nombreComplejo);
            }

            if (complejosCandidatos.size() > 1) {
                StringBuilder response = new StringBuilder();
                response.append(String.format("🔍 Encontré %d complejos que coinciden:\n\n", complejosCandidatos.size()));

                for (int i = 0; i < complejosCandidatos.size(); i++) {
                    ComplejoDeportivo complejo = complejosCandidatos.get(i);
                    response.append(String.format("%d. %s\n", (i + 1), complejo.getNombre()));
                }

                response.append("\nPor favor, escribe el número o sé más específico con el nombre.");
                return response.toString();
            }

            // Solo hay un complejo, continuar con las instancias
            ComplejoDeportivo complejo = complejosCandidatos.get(0);
            List<InstanciaServicio> instancias = instanciaServicioRepository
                    .findInstanciaServicioByComplejoDeportivo(complejo.getIdComplejoDeportivo());

            if (instancias.isEmpty()) {
                limpiarEstadoSesion(session);
                return String.format("❌ El complejo %s no tiene instalaciones disponibles.", complejo.getNombre());
            }

            // Guardar ID del complejo seleccionado y cambiar estado
            session.setAttribute("chatbot_complejo_id", complejo.getIdComplejoDeportivo());
            session.setAttribute("chatbot_estado", "SELECCIONANDO_INSTANCIA");

            return mostrarInstanciasParaSeleccion(complejo, instancias);

        } catch (Exception e) {
            System.out.println("Error al procesar selección de complejo: " + e.getMessage());
            return "❌ Ocurrió un error. Por favor, intenta nuevamente.";
        }
    }

    /**
     * Procesa la selección de la instancia/servicio
     */
    private String procesarSeleccionInstancia(String mensaje, HttpSession session) {
        try {
            Integer complejoId = (Integer) session.getAttribute("chatbot_complejo_id");
            ComplejoDeportivo complejo = complejoRepository.findById(complejoId).orElse(null);
            
            if (complejo == null) {
                limpiarEstadoSesion(session);
                return "❌ Error al procesar la selección. Por favor, inicia nuevamente el proceso.";
            }
            
            List<InstanciaServicio> instancias = instanciaServicioRepository
                    .findInstanciaServicioByComplejoDeportivo(complejo.getIdComplejoDeportivo());

            String nombreInstancia = extraerNombreInstancia(mensaje);

            // Buscar la instancia seleccionada
            InstanciaServicio instanciaSeleccionada = null;
            for (InstanciaServicio instancia : instancias) {
                String nombreInstanciaCompleto = instancia.getNombre().toLowerCase();
                String nombreServicio = instancia.getServicio().getNombre().toLowerCase();

                if (nombreInstanciaCompleto.contains(nombreInstancia) ||
                        nombreServicio.contains(nombreInstancia) ||
                        nombreInstancia.contains(nombreServicio)) {
                    instanciaSeleccionada = instancia;
                    break;
                }
            }

            if (instanciaSeleccionada == null) {
                return "🤔 No pude identificar la instalación. Por favor, escribe el nombre exacto de la instalación que te interesa.\n\n" +
                        "Ejemplo: 'cancha de fútbol', 'gimnasio', 'piscina', etc.";
            }

            // Guardar ID de la instancia seleccionada y cambiar estado
            session.setAttribute("chatbot_instancia_id", instanciaSeleccionada.getIdInstanciaServicio());
            session.setAttribute("chatbot_estado", "SELECCIONANDO_FECHA");

            return "📅 ¡Perfecto! Has seleccionado: " + instanciaSeleccionada.getNombre() + "\n\n" +
                    "Ahora necesito saber para qué fecha quieres hacer la reserva.\n\n" +
                    "Puedes escribir:\n" +
                    "• 'mañana' para el día de mañana\n" +
                    "• 'hoy' para hoy\n" +
                    "• Una fecha específica como '8 de julio' o '2025-07-08'\n\n" +
                    "💡 ¿Para qué fecha te gustaría reservar?";

        } catch (Exception e) {
            System.out.println("Error al procesar selección de instancia: " + e.getMessage());
            return "❌ Ocurrió un error. Por favor, intenta nuevamente.";
        }
    }

    /**
     * Procesa la selección de fecha
     */
    private String procesarSeleccionFecha(String mensaje, HttpSession session) {
        try {
            LocalDate fecha = extraerFecha(mensaje);

            if (fecha == null) {
                return "🤔 No pude entender la fecha. Por favor, especifica la fecha de manera clara.\n\n" +
                        "Ejemplos válidos:\n" +
                        "• 'mañana'\n" +
                        "• 'hoy'\n" +
                        "• '8 de julio'\n" +
                        "• '2025-07-08'";
            }

            // Validar que la fecha sea razonable
            if (fecha.isBefore(LocalDate.now())) {
                return "❌ No se pueden hacer reservas para fechas pasadas. Por favor, elige una fecha futura.";
            }

            if (fecha.isAfter(LocalDate.now().plusMonths(3))) {
                return "❌ Solo se pueden hacer reservas hasta 3 meses en adelante. Por favor, elige una fecha más cercana.";
            }

            // Verificar disponibilidad para esa fecha
            Integer instanciaId = (Integer) session.getAttribute("chatbot_instancia_id");
            InstanciaServicio instancia = instanciaServicioRepository.findById(instanciaId).orElse(null);
            
            if (instancia == null) {
                limpiarEstadoSesion(session);
                return "❌ Ha ocurrido un error. Por favor, inicia nuevamente el proceso de reserva.";
            }
            
            Map<String, Object> disponibilidad = verificarDisponibilidadInstancia(instancia.getIdInstanciaServicio(), fecha);

            Boolean fechaLlena = (Boolean) disponibilidad.get("fechaLlena");
            if (fechaLlena) {
                return String.format("❌ Lo siento, no hay horarios disponibles para %s en esa fecha.\n\n" +
                                "💡 ¿Te gustaría intentar con otra fecha?",
                        fecha.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", new Locale("es", "ES"))));
            }

            // Guardar fecha y cambiar estado
            session.setAttribute("chatbot_fecha", fecha);
            session.setAttribute("chatbot_bloques", disponibilidad.get("bloques"));
            session.setAttribute("chatbot_estado", "SELECCIONANDO_HORARIO");

            return mostrarHorariosDisponibles(instancia, fecha, disponibilidad);

        } catch (Exception e) {
            System.out.println("Error al procesar selección de fecha: " + e.getMessage());
            return "❌ Ocurrió un error. Por favor, intenta nuevamente.";
        }
    }

    /**
     * Procesa la selección de horario
     */
    private String procesarSeleccionHorario(String mensaje, HttpSession session) {
        try {
            // Extraer horarios del mensaje
            LocalTime horaInicio = null;
            LocalTime horaFin = null;

            String text = mensaje.toLowerCase().trim();

            // Intentar extraer horario en formato "de X a Y"
            if (text.matches(".*de\\s+(\\d{1,2}):?(\\d{0,2})\\s*a\\s+(\\d{1,2}):?(\\d{0,2}).*")) {
                String[] partes = text.split("\\s+");
                for (int i = 0; i < partes.length - 2; i++) {
                    if (partes[i].equals("de")) {
                        horaInicio = parsearHora(partes[i + 1]);
                        if (i + 3 < partes.length && partes[i + 2].equals("a")) {
                            horaFin = parsearHora(partes[i + 3]);
                        }
                        break;
                    }
                }
            }

            // Si no encontró formato "de X a Y", buscar hora única y asumir 1 hora
            if (horaInicio == null) {
                String patronHora = "\\b(\\d{1,2}):?(\\d{0,2})\\b";
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patronHora);
                java.util.regex.Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    horaInicio = parsearHora(matcher.group(0));
                    horaFin = horaInicio.plusHours(1); // Asumir 1 hora por defecto
                }
            }

            if (horaInicio == null) {
                return "🤔 No pude entender el horario. Por favor, especifica la hora de manera clara.\n\n" +
                        "Ejemplos válidos:\n" +
                        "• 'de 15:00 a 16:00'\n" +
                        "• 'a las 15:00' (se reservará 1 hora)\n" +
                        "• '15:00 por 2 horas'";
            }

            // Validar que el horario esté disponible
            List<Map<String, Object>> bloques = (List<Map<String, Object>>) session.getAttribute("chatbot_bloques");
            boolean horarioValido = false;
            Double montoTotal = 0.0;

            for (Map<String, Object> bloque : bloques) {
                List<String> horasDisponibles = (List<String>) bloque.get("horasDisponibles");
                Double monto = (Double) bloque.get("monto");

                // Verificar si las horas solicitadas están disponibles
                LocalTime horaActual = horaInicio;
                boolean todasDisponibles = true;
                int horasReservadas = 0;

                while (horaActual.isBefore(horaFin)) {
                    if (!horasDisponibles.contains(horaActual.toString())) {
                        todasDisponibles = false;
                        break;
                    }
                    horaActual = horaActual.plusHours(1);
                    horasReservadas++;
                }

                if (todasDisponibles && horasReservadas > 0) {
                    horarioValido = true;
                    montoTotal = monto * horasReservadas;
                    break;
                }
            }

            if (!horarioValido) {
                return "❌ El horario seleccionado no está disponible. Por favor, elige un horario de los disponibles mostrados anteriormente.";
            }

            // Guardar selección y cambiar estado
            session.setAttribute("chatbot_hora_inicio", horaInicio);
            session.setAttribute("chatbot_hora_fin", horaFin);
            session.setAttribute("chatbot_monto_total", montoTotal);
            session.setAttribute("chatbot_estado", "CONFIRMANDO_RESERVA");

            return generarResumenReserva(session);

        } catch (Exception e) {
            System.out.println("Error al procesar selección de horario: " + e.getMessage());
            return "❌ Ocurrió un error. Por favor, intenta nuevamente.";
        }
    }

    /**
     * Muestra las instancias para selección
     */
    private String mostrarInstanciasParaSeleccion(ComplejoDeportivo complejo, List<InstanciaServicio> instancias) {
        StringBuilder response = new StringBuilder();
        response.append(String.format("🏟️ Instalaciones disponibles en %s:\n\n", complejo.getNombre()));

        // Agrupar por tipo de servicio
        var instanciasPorServicio = instancias.stream()
                .collect(Collectors.groupingBy(i -> i.getServicio().getNombre()));

        for (var entrada : instanciasPorServicio.entrySet()) {
            String tipoServicio = entrada.getKey();
            List<InstanciaServicio> instanciasDelServicio = entrada.getValue();

            response.append(String.format("⚽ %s:\n", tipoServicio));
            for (InstanciaServicio instancia : instanciasDelServicio) {
                response.append(String.format("   • %s\n", instancia.getNombre()));
            }
            response.append("\n");
        }

        response.append("💡 Escribe el nombre de la instalación que te interesa, por ejemplo:\n");
        response.append("'cancha de fútbol', 'gimnasio', 'piscina', etc.");

        return response.toString();
    }

    /**
     * Muestra los horarios disponibles para una fecha
     */
    private String mostrarHorariosDisponibles(InstanciaServicio instancia, LocalDate fecha, Map<String, Object> disponibilidad) {
        List<Map<String, Object>> bloques = (List<Map<String, Object>>) disponibilidad.get("bloques");

        DateTimeFormatter fechaFormateada = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

        StringBuilder response = new StringBuilder();
        response.append(String.format("⏰ Horarios disponibles para %s\n", instancia.getNombre()));
        response.append(String.format("📅 Fecha: %s\n\n", fecha.format(fechaFormateada)));

        for (int i = 0; i < bloques.size(); i++) {
            Map<String, Object> bloque = bloques.get(i);
            String inicioTarifa = (String) bloque.get("inicioTarifa");
            String finTarifa = (String) bloque.get("finTarifa");
            Double monto = (Double) bloque.get("monto");
            List<String> horasDisponibles = (List<String>) bloque.get("horasDisponibles");

            response.append(String.format("🕐 Bloque %d: %s - %s (S/ %.2f/hora)\n",
                    (i + 1), inicioTarifa, finTarifa, monto));
            response.append("   Horas disponibles: ");
            response.append(String.join(", ", horasDisponibles));
            response.append("\n\n");
        }

        response.append("💡 Escribe el horario que prefieres, por ejemplo:\n");
        response.append("'de 15:00 a 16:00' o 'a las 15:00' (se reservará 1 hora)");

        return response.toString();
    }

    /**
     * Genera el resumen de la reserva antes de confirmar
     */
    private String generarResumenReserva(HttpSession session) {
        Integer complejoId = (Integer) session.getAttribute("chatbot_complejo_id");
        Integer instanciaId = (Integer) session.getAttribute("chatbot_instancia_id");
        LocalDate fecha = (LocalDate) session.getAttribute("chatbot_fecha");
        LocalTime horaInicio = (LocalTime) session.getAttribute("chatbot_hora_inicio");
        LocalTime horaFin = (LocalTime) session.getAttribute("chatbot_hora_fin");
        
        // Buscar las entidades por ID
        ComplejoDeportivo complejo = complejoRepository.findById(complejoId).orElse(null);
        InstanciaServicio instancia = instanciaServicioRepository.findById(instanciaId).orElse(null);
        
        if (complejo == null || instancia == null) {
            return "❌ Error al generar el resumen. Por favor, inicia nuevamente el proceso.";
        }
        Double montoTotal = (Double) session.getAttribute("chatbot_monto_total");

        DateTimeFormatter fechaFormateada = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

        return String.format("📋 Resumen de tu reserva:\n\n" +
                        "🏟️ Complejo: %s\n" +
                        "⚽ Instalación: %s\n" +
                        "📅 Fecha: %s\n" +
                        "🕐 Horario: %s a %s\n" +
                        "💰 Costo total: S/ %.2f\n\n" +
                        "¿Confirmas esta reserva? Responde 'confirmar' para proceder o 'cancelar' para salir.",
                complejo.getNombre(),
                instancia.getNombre(),
                fecha.format(fechaFormateada),
                horaInicio.toString(),
                horaFin.toString(),
                montoTotal);
    }

    /**
     * Confirma y crea la reserva
     */
    private String confirmarReserva(String mensaje, Usuario usuario, HttpSession session) {
        String text = mensaje.toLowerCase().trim();

        if (text.contains("confirmar") || text.contains("confirmo") || text.contains("sí") || text.contains("si")) {
            try {
                // Obtener datos de la sesión
                Integer instanciaId = (Integer) session.getAttribute("chatbot_instancia_id");
                InstanciaServicio instancia = instanciaServicioRepository.findById(instanciaId).orElse(null);
                
                if (instancia == null) {
                    limpiarEstadoSesion(session);
                    return "❌ Error al procesar la reserva. Por favor, inicia nuevamente el proceso.";
                }
                
                LocalDate fecha = (LocalDate) session.getAttribute("chatbot_fecha");
                LocalTime horaInicio = (LocalTime) session.getAttribute("chatbot_hora_inicio");
                LocalTime horaFin = (LocalTime) session.getAttribute("chatbot_hora_fin");
                Double montoTotal = (Double) session.getAttribute("chatbot_monto_total");
                LocalDateTime fechaHoraReserva = LocalDateTime.now();

                // Crear la información de pago primero
                InformacionPago informacionPago = new InformacionPago();
                informacionPago.setTotal(montoTotal);
                informacionPago.setEstado("Pendiente"); // Estado pendiente de pago
                informacionPago.setFecha(LocalDate.now());
                
                // Guardar la información de pago
                InformacionPago pagoGuardado = informacionPagoRepository.save(informacionPago);
                
                // Crear la reserva
                Reserva nuevaReserva = new Reserva();
                nuevaReserva.setUsuario(usuario);
                nuevaReserva.setInstanciaServicio(instancia);
                nuevaReserva.setFecha(fecha);
                nuevaReserva.setHoraInicio(horaInicio);
                nuevaReserva.setHoraFin(horaFin);
                nuevaReserva.setInformacionPago(pagoGuardado); // Asociar la información de pago}
                nuevaReserva.setFechaHoraRegistro(fechaHoraReserva);
                nuevaReserva.setEstado(0); // Estado pendiente de pago

                // Guardar la reserva
                Reserva reservaGuardada = reservaRepository.save(nuevaReserva);

                limpiarEstadoSesion(session);

                return "✅ ¡Reserva creada exitosamente!\n\n" +
                        "Tu reserva ha sido registrada con estado 'Pendiente de Pago'. " +
                        "Puedes proceder al pago desde tu panel de reservas.\n\n" +
                        "💡 Detalles de tu reserva:\n" +
                        "• ID de Reserva: " + reservaGuardada.getIdReserva() + "\n" +
                        "• Monto a pagar: S/ " + String.format("%.2f", montoTotal) + "\n\n" +
                        "Para consultar tus reservas escribe 'mis reservas'.\n\n" +
                        "¿Hay algo más en lo que pueda ayudarte?";

            } catch (Exception e) {
                System.out.println("Error al crear reserva: " + e.getMessage());
                e.printStackTrace();
                limpiarEstadoSesion(session);
                return "❌ Ocurrió un error al crear la reserva. Por favor, intenta nuevamente más tarde.";
            }
        } else {
            limpiarEstadoSesion(session);
            return "❌ Reserva cancelada. ¿Hay algo más en lo que pueda ayudarte?";
        }
    }

    /**
     * Lista todos los complejos deportivos disponibles en San Miguel
     */
    private String listarComplejos() {
        try {
            List<ComplejoDeportivo> complejos = complejoRepository.findAll();
            if (complejos.isEmpty()) {
                return "❌ No se encontraron complejos deportivos registrados en el sistema.";
            }

            StringBuilder response = new StringBuilder();
            response.append("🏟️ Complejos Deportivos en San Miguel:\n\n");

            for (int i = 0; i < complejos.size(); i++) {
                ComplejoDeportivo complejo = complejos.get(i);
                response.append(String.format("%d. %s\n", (i + 1), complejo.getNombre()));
                response.append(String.format("   📍 Dirección: %s\n", complejo.getDireccion()));
                response.append("\n");
            }

            response.append("💡 Tip: Para ver las instalaciones de un complejo específico, escribe: ");
            response.append("'instancias del [nombre del complejo]'");

            return response.toString();

        } catch (Exception e) {
            System.out.println("Error al listar complejos: " + e.getMessage());
            return "❌ Ocurrió un error al obtener la lista de complejos deportivos. Por favor, intenta nuevamente.";
        }
    }

    /**
     * Lista las instancias deportivas de un complejo específico
     */
    private String listarInstanciasDeComplejo(String mensaje) {
        try {
            // Extraer el nombre del complejo del mensaje
            String nombreComplejo = extraerNombreComplejo(mensaje);

            if (nombreComplejo.isEmpty()) {
                return "🤔 No pude identificar el nombre del complejo. Por favor, especifica de qué complejo quieres ver las instalaciones.\n\n" +
                        "Ejemplo: 'instancias del Complejo Central' o 'canchas del estadio municipal'";
            }

            // Buscar complejos que coincidan con el nombre
            List<ComplejoDeportivo> complejosCandidatos = buscarComplejosPorNombre(nombreComplejo);

            if (complejosCandidatos.isEmpty()) {
                return String.format("❌ No encontré ningún complejo con el nombre '%s'.\n\n" +
                        "Para ver todos los complejos disponibles, escribe: 'listar complejos'", nombreComplejo);
            }

            if (complejosCandidatos.size() > 1) {
                StringBuilder response = new StringBuilder();
                response.append(String.format("🔍 Encontré %d complejos que coinciden con '%s':\n\n",
                        complejosCandidatos.size(), nombreComplejo));

                for (int i = 0; i < complejosCandidatos.size(); i++) {
                    ComplejoDeportivo complejo = complejosCandidatos.get(i);
                    response.append(String.format("%d. %s - %s\n", (i + 1), complejo.getNombre(), complejo.getDireccion()));
                }

                response.append("\nPor favor, sé más específico con el nombre del complejo que te interesa.");
                return response.toString();
            }

            // Solo hay un complejo, mostrar sus instancias
            ComplejoDeportivo complejo = complejosCandidatos.get(0);
            List<InstanciaServicio> instancias = instanciaServicioRepository
                    .findInstanciaServicioByComplejoDeportivo(complejo.getIdComplejoDeportivo());

            if (instancias.isEmpty()) {
                return String.format("❌ El complejo %s no tiene instalaciones deportivas registradas.", complejo.getNombre());
            }

            StringBuilder response = new StringBuilder();
            response.append(String.format("🏟️ Instalaciones del %s:\n", complejo.getNombre()));
            response.append(String.format("📍 Ubicado en: %s\n\n", complejo.getDireccion()));

            // Agrupar por tipo de servicio
            var instanciasPorServicio = instancias.stream()
                    .collect(Collectors.groupingBy(i -> i.getServicio().getNombre()));

            for (var entrada : instanciasPorServicio.entrySet()) {
                String tipoServicio = entrada.getKey();
                List<InstanciaServicio> instanciasDelServicio = entrada.getValue();

                response.append(String.format("⚽ %s:\n", tipoServicio));
                for (InstanciaServicio instancia : instanciasDelServicio) {
                    response.append(String.format("   • %s", instancia.getNombre()));
                    if (instancia.getCapacidadMaxima() != null && !instancia.getCapacidadMaxima().isEmpty()) {
                        response.append(String.format(" (Capacidad: %s)", instancia.getCapacidadMaxima()));
                    }
                    response.append("\n");
                }
                response.append("\n");
            }

            response.append("💡 Tips útiles:\n");
            response.append("• Para hacer una reserva: 'hacer reserva'\n");
            response.append("• Para ver todos los complejos: 'listar complejos'\n");
            response.append("• Para consultar tus reservas: 'mis reservas'");

            return response.toString();

        } catch (Exception e) {
            System.out.println("Error al listar instancias: " + e.getMessage());
            return "❌ Ocurrió un error al obtener las instalaciones del complejo. Por favor, intenta nuevamente.";
        }
    }

    /**
     * Extrae el nombre del complejo del mensaje del usuario
     */
    private String extraerNombreComplejo(String mensaje) {
        String mensajeLimpio = mensaje.toLowerCase().trim();

        // Buscar después de "del" o "de"
        String[] palabras = mensajeLimpio.split("\\s+");
        String nombreExtraido = "";

        // Buscar el patrón "del [nombre del complejo]"
        for (int i = 0; i < palabras.length - 1; i++) {
            if (palabras[i].equals("del") || palabras[i].equals("de")) {
                StringBuilder nombre = new StringBuilder();

                // Capturar las siguientes palabras hasta encontrar un delimitador
                for (int j = i + 1; j < palabras.length; j++) {
                    String palabra = palabras[j];

                    // Parar en palabras que indican fin del nombre del complejo
                    if (palabra.equals("para") || palabra.equals("en") || palabra.equals("con") ||
                            palabra.equals("y") || palabra.equals("o") || palabra.equals("el") ||
                            palabra.equals("la") || palabra.equals("los") || palabra.equals("las")) {
                        break;
                    }

                    // Incluir la palabra si no es genérica
                    if (!palabra.equals("complejo") && !palabra.equals("deportivo") && !palabra.equals("municipal")) {
                        if (nombre.length() > 0) {
                            nombre.append(" ");
                        }
                        nombre.append(palabra);
                    }
                }

                if (nombre.length() > 0) {
                    nombreExtraido = nombre.toString().trim();
                    break;
                }
            }
        }

        // Si no encontramos nada con "del/de", intentar extraer cualquier nombre probable
        if (nombreExtraido.isEmpty()) {
            for (String palabra : palabras) {
                // Saltar palabras muy comunes
                if (!palabra.equals("instancias") && !palabra.equals("instalaciones") &&
                        !palabra.equals("canchas") && !palabra.equals("para") && !palabra.equals("del") &&
                        !palabra.equals("de") && !palabra.equals("la") && !palabra.equals("el") &&
                        !palabra.equals("los") && !palabra.equals("las") && !palabra.equals("complejo") &&
                        !palabra.equals("deportivo") && palabra.length() > 2) {

                    if (palabra.matches("[a-zA-Z]+")) {
                        nombreExtraido = palabra;
                        break;
                    }
                }
            }
        }

        return nombreExtraido;
    }

    /**
     * Busca complejos que coincidan con el nombre proporcionado
     */
    private List<ComplejoDeportivo> buscarComplejosPorNombre(String nombre) {
        try {
            List<ComplejoDeportivo> todosComplejos = complejoRepository.findAll();
            String nombreBuscado = nombre.toLowerCase().trim();

            // Primero buscar coincidencia exacta
            List<ComplejoDeportivo> coincidenciasExactas = todosComplejos.stream()
                    .filter(complejo -> {
                        String nombreComplejo = complejo.getNombre().toLowerCase();

                        // Coincidencia exacta del nombre completo
                        if (nombreComplejo.equals(nombreBuscado)) {
                            return true;
                        }

                        // Coincidencia exacta sin "complejo deportivo"
                        String nombreSinPrefijo = nombreComplejo.replaceAll("\\b(complejo\\s+deportivo|complejo)\\s+", "").trim();
                        if (nombreSinPrefijo.equals(nombreBuscado)) {
                            return true;
                        }

                        return false;
                    })
                    .collect(Collectors.toList());

            if (!coincidenciasExactas.isEmpty()) {
                return coincidenciasExactas;
            }

            // Buscar coincidencias que contengan el nombre
            return todosComplejos.stream()
                    .filter(complejo -> {
                        String nombreComplejo = complejo.getNombre().toLowerCase();
                        return nombreComplejo.contains(nombreBuscado) || nombreBuscado.contains(nombreComplejo);
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.out.println("Error al buscar complejos: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Extrae el nombre de la instancia del mensaje
     */
    private String extraerNombreInstancia(String mensaje) {
        String mensajeLimpio = mensaje.toLowerCase().trim();

        // Buscar palabras clave específicas de deportes
        if (mensajeLimpio.contains("fútbol") || mensajeLimpio.contains("futbol")) return "fútbol";
        if (mensajeLimpio.contains("básquet") || mensajeLimpio.contains("basquet") || mensajeLimpio.contains("basketball")) return "básquet";
        if (mensajeLimpio.contains("vóley") || mensajeLimpio.contains("voley") || mensajeLimpio.contains("volleyball")) return "vóley";
        if (mensajeLimpio.contains("tenis")) return "tenis";
        if (mensajeLimpio.contains("gimnasio")) return "gimnasio";
        if (mensajeLimpio.contains("piscina")) return "piscina";
        if (mensajeLimpio.contains("grass") || mensajeLimpio.contains("césped")) return "grass";
        if (mensajeLimpio.contains("cancha")) return "cancha";

        return mensajeLimpio; // Devolver el mensaje completo si no hay coincidencias específicas
    }

    /**
     * Extrae la fecha del mensaje
     */
    private LocalDate extraerFecha(String mensaje) {
        String mensajeLimpio = mensaje.toLowerCase().trim();

        try {
            // Casos especiales
            if (mensajeLimpio.contains("hoy")) {
                return LocalDate.now();
            }
            if (mensajeLimpio.contains("mañana")) {
                return LocalDate.now().plusDays(1);
            }
            if (mensajeLimpio.contains("pasado mañana")) {
                return LocalDate.now().plusDays(2);
            }

            // Formato ISO (YYYY-MM-DD)
            if (mensajeLimpio.matches(".*\\b(\\d{4}-\\d{2}-\\d{2})\\b.*")) {
                String fechaStr = mensajeLimpio.replaceFirst(".*\\b(\\d{4}-\\d{2}-\\d{2})\\b.*", "$1");
                return LocalDate.parse(fechaStr);
            }

            // Formato DD/MM/YYYY o DD-MM-YYYY
            if (mensajeLimpio.matches(".*\\b(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})\\b.*")) {
                String[] partes = mensajeLimpio.replaceFirst(".*\\b(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})\\b.*", "$1/$2/$3").split("/");
                int dia = Integer.parseInt(partes[0]);
                int mes = Integer.parseInt(partes[1]);
                int año = Integer.parseInt(partes[2]);
                return LocalDate.of(año, mes, dia);
            }

            // Formato "el X de mes"
            Map<String, Integer> meses = new HashMap<>();
            meses.put("enero", 1);
            meses.put("febrero", 2);
            meses.put("marzo", 3);
            meses.put("abril", 4);
            meses.put("mayo", 5);
            meses.put("junio", 6);
            meses.put("julio", 7);
            meses.put("agosto", 8);
            meses.put("septiembre", 9);
            meses.put("octubre", 10);
            meses.put("noviembre", 11);
            meses.put("diciembre", 12);

            for (Map.Entry<String, Integer> mes : meses.entrySet()) {
                String patron = "\\b(\\d{1,2})\\s+de\\s+" + mes.getKey() + "\\b";
                if (mensajeLimpio.matches(".*" + patron + ".*")) {
                    String diaStr = mensajeLimpio.replaceFirst(".*" + patron + ".*", "$1");
                    int dia = Integer.parseInt(diaStr);
                    int año = LocalDate.now().getYear();

                    // Si la fecha ya pasó este año, asumir el próximo año
                    LocalDate fechaCalculada = LocalDate.of(año, mes.getValue(), dia);
                    if (fechaCalculada.isBefore(LocalDate.now())) {
                        fechaCalculada = fechaCalculada.plusYears(1);
                    }

                    return fechaCalculada;
                }
            }

        } catch (Exception e) {
            System.out.println("Error al parsear fecha: " + e.getMessage());
        }

        return null;
    }

    /**
     * Verifica la disponibilidad de una instancia para una fecha específica
     */
    private Map<String, Object> verificarDisponibilidadInstancia(Integer idInstancia, LocalDate fecha) {
        Map<String, Object> respuesta = new HashMap<>();

        try {
            Optional<InstanciaServicio> instanciaOpt = instanciaServicioRepository.findById(idInstancia);
            if (instanciaOpt.isEmpty()) {
                respuesta.put("fechaLlena", true);
                respuesta.put("bloques", List.of());
                return respuesta;
            }

            Servicio servicio = instanciaOpt.get().getServicio();
            String diaSemana = fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

            // Normalizar para comparación robusta
            String diaNormalizado = Normalizer.normalize(diaSemana.toLowerCase(), Normalizer.Form.NFD)
                    .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

            // Buscar tarifas para ese día y servicio
            List<Tarifa> tarifas = tarifaRepository.findAll().stream()
                    .filter(t -> t.getServicio().getIdServicio().equals(servicio.getIdServicio()))
                    .filter(t -> {
                        String diaTarifa = Normalizer.normalize(t.getDiaSemana().toLowerCase(), Normalizer.Form.NFD)
                                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                        return diaTarifa.equals(diaNormalizado);
                    })
                    .toList();

            if (tarifas.isEmpty()) {
                respuesta.put("fechaLlena", true);
                respuesta.put("bloques", List.of());
                return respuesta;
            }

            // Horas ocupadas por reservas
            List<Reserva> reservas = reservaRepository.findByInstanciaServicio_IdInstanciaServicioAndFecha(idInstancia, fecha);
            List<LocalTime> horasOcupadas = reservas.stream()
                    .flatMap(r -> {
                        List<LocalTime> horas = new ArrayList<>();
                        LocalTime hora = r.getHoraInicio();
                        while (hora.isBefore(r.getHoraFin())) {
                            horas.add(hora);
                            hora = hora.plusHours(1);
                        }
                        return horas.stream();
                    }).toList();

            // Armar bloques con horas disponibles
            List<Map<String, Object>> bloques = new ArrayList<>();

            for (Tarifa tarifa : tarifas) {
                Map<String, Object> bloque = new HashMap<>();
                bloque.put("inicioTarifa", tarifa.getHoraInicio().toString());
                bloque.put("finTarifa", tarifa.getHoraFin().toString());
                bloque.put("monto", tarifa.getMonto());

                List<String> horasDisponibles = new ArrayList<>();
                LocalTime hora = tarifa.getHoraInicio();
                while (hora.isBefore(tarifa.getHoraFin())) {
                    if (!horasOcupadas.contains(hora)) {
                        horasDisponibles.add(hora.toString());
                    }
                    hora = hora.plusHours(1);
                }

                bloque.put("horasDisponibles", horasDisponibles);
                if (!horasDisponibles.isEmpty()) {
                    bloques.add(bloque);
                }
            }

            respuesta.put("fechaLlena", bloques.isEmpty());
            respuesta.put("bloques", bloques);

        } catch (Exception e) {
            System.out.println("Error al verificar disponibilidad: " + e.getMessage());
            respuesta.put("fechaLlena", true);
            respuesta.put("bloques", List.of());
        }

        return respuesta;
    }

    /**
     * Parsea una hora en formato string a LocalTime
     */
    private LocalTime parsearHora(String horaStr) {
        try {
            horaStr = horaStr.replaceAll("[^\\d:]", ""); // Limpiar caracteres no numéricos

            if (horaStr.contains(":")) {
                return LocalTime.parse(horaStr);
            } else {
                // Formato sin dos puntos, ej: "15" o "1500"
                if (horaStr.length() <= 2) {
                    return LocalTime.of(Integer.parseInt(horaStr), 0);
                } else {
                    String horas = horaStr.substring(0, horaStr.length() - 2);
                    String minutos = horaStr.substring(horaStr.length() - 2);
                    return LocalTime.of(Integer.parseInt(horas), Integer.parseInt(minutos));
                }
            }
        } catch (Exception e) {
            System.out.println("Error al parsear hora: " + horaStr + " - " + e.getMessage());
            return null;
        }
    }
}
