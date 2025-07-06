package com.example.proyectosanmiguel.chatbot;

import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
                    Thread.sleep(100); // Simular delay de streaming
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
                    Thread.sleep(100); // Simular delay de streaming
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
            session.removeAttribute("chatbot_estado");
            session.removeAttribute("chatbot_complejo");
            session.removeAttribute("chatbot_instancia");
            session.removeAttribute("chatbot_fecha");
            return "❌ Operación cancelada. ¿En qué más puedo ayudarte?";
        }

        // Verificar si estamos en medio de un flujo de reserva
        String estado = (String) session.getAttribute("chatbot_estado");
        if (estado != null) {
            return manejarFlujoReserva(message, usuario, session, estado);
        }

        // 1. Determinar intención básica según palabras clave:
        String intent;
        if (text.contains("hola") || text.contains("buenos") || text.contains("buenas") || text.contains("saludos")) {
            intent = "SALUDO";
        } else if (text.contains("complejo") && (text.contains("listar") || text.contains("lista") || text.contains("mostrar") || text.contains("cuales") || text.contains("cuáles") || text.contains("ver") || text.contains("qué") || text.contains("que"))) {
            intent = "LISTAR_COMPLEJOS";
        } else if ((text.contains("instancia") || text.contains("cancha") || text.contains("gimnasio") || text.contains("instalacion") || text.contains("instalación")) && 
                   (text.contains("listar") || text.contains("lista") || text.contains("mostrar") || text.contains("cuales") || text.contains("cuáles") || text.contains("ver") || text.contains("del") || text.contains("de"))) {
            intent = "LISTAR_INSTANCIAS";
        } else if (text.contains("disponibilidad") || text.contains("horarios") || text.contains("precio") || text.contains("precios") || 
                   (text.contains("horario") && (text.contains("fecha") || text.contains("día") || text.contains("dia")))) {
            intent = "CONSULTAR_DISPONIBILIDAD";
        } else if (text.contains("hora") || text.contains("horario") || text.contains("horarios") || text.contains("abierto") || text.contains("abren") || text.contains("cierran")) {
            intent = "HORA";
        } else if (text.contains("reserva") || text.contains("reservar") || text.contains("cita") || text.contains("turno") || text.contains("mis reservas")) {
            intent = "RESERVA";
        } else if (text.contains("servicio") || text.contains("deporte") || text.contains("actividad") || text.contains("que ofrecen") || text.contains("qué ofrecen")) {
            intent = "SERVICIOS";
        } else if (text.contains("gracias") || text.contains("gracia")) {
            intent = "AGRADECIMIENTO";
        } else {
            intent = "DEFAULT";
        }

        // 2. Manejar cada caso con switch-case:
        switch (intent) {
            case "SALUDO":
                if (firstName != null) {
                    return "¡Hola " + firstName + "! 👋 Soy el asistente virtual del Complejo Deportivo San Miguel. ¿En qué puedo ayudarte hoy?\n\n" +
                            "Puedo ayudarte con:\n" +
                            "• 📋 Listar complejos deportivos\n" +
                            "• 🏟️ Ver instancias de un complejo específico\n" +
                            "• ⏰ Consultar horarios y precios\n" +
                            "• 📅 Información sobre tus reservas";
                }
                return "¡Hola! 👋 Soy el asistente virtual del Complejo Deportivo San Miguel. ¿En qué puedo ayudarte hoy?\n\n" +
                       "Puedo ayudarte con:\n" +
                       "• 📋 Listar complejos deportivos\n" +
                       "• 🏟️ Ver instancias de un complejo específico\n" +
                       "• ⏰ Consultar horarios y precios\n" +
                       "• 📅 Información sobre tus reservas";

            case "LISTAR_COMPLEJOS":
                return listarComplejos();

            case "LISTAR_INSTANCIAS":
                return listarInstanciasDeComplejo(text);

            case "CONSULTAR_DISPONIBILIDAD":
                return consultarDisponibilidad(text, session);

            case "HORA":
                return "🕒 Nuestros horarios de atención son:\n" +
                       "• Lunes a Viernes: 6:00 AM a 10:00 PM\n" +
                       "• Sábados y Domingos: 7:00 AM a 8:00 PM\n\n" +
                       "Los complejos deportivos están disponibles durante estos horarios para reservas y actividades.";

            case "RESERVA":
                if (idUsuario != null) {
                    try {
                        List<Reserva> misReservas = reservaRepository.findByUsuario_IdUsuario(idUsuario);
                        if (!misReservas.isEmpty()) {
                            long reservasActivas = misReservas.stream()
                                    .filter(r -> r.getEstado() == 1) // Estado activo
                                    .count();
                            long reservasPendientes = misReservas.stream()
                                    .filter(r -> r.getEstado() == 0) // Estado pendiente
                                    .count();
                            
                            return "📋 Estado de tus reservas:\n" +
                                   "• Reservas activas: " + reservasActivas + "\n" +
                                   "• Reservas pendientes: " + reservasPendientes + "\n" +
                                   "• Total de reservas: " + misReservas.size() + "\n\n" +
                                   "Puedes gestionar tus reservas desde el panel de usuario o crear nuevas reservas cuando gustes.";
                        } else {
                            return "📋 Actualmente no tienes reservas registradas. ¿Te gustaría hacer una nueva reserva? Puedo ayudarte a encontrar el complejo y horario que mejor se adapte a tus necesidades.";
                        }
                    } catch (Exception e) {
                        System.out.println("Error al obtener reservas: " + e.getMessage());
                    }
                }
                return "📋 Para consultar tus reservas necesito que inicies sesión en tu cuenta. Una vez autenticado podré mostrarte el estado de todas tus reservas y ayudarte a gestionar nuevas citas.";

            case "SERVICIOS":
                try {
                    List<Servicio> servicios = servicioRepository.findAll();
                    if (!servicios.isEmpty()) {
                        String nombresServicios = servicios.stream()
                                .map(Servicio::getNombre)
                                .collect(Collectors.joining(", "));
                        return "⚽ Ofrecemos los siguientes servicios deportivos: " + nombresServicios + ".\n\n" +
                               "Cada servicio cuenta con instalaciones especializadas y horarios flexibles. ¿Te interesa información específica sobre algún deporte?";
                    }
                } catch (Exception e) {
                    System.out.println("Error al obtener servicios: " + e.getMessage());
                }
                return "⚽ Ofrecemos una amplia variedad de servicios deportivos incluyendo fútbol, básquet, vóley, tenis y más. ¿Te interesa información específica sobre algún deporte?";

            case "AGRADECIMIENTO":
                if (firstName != null) {
                    return "¡De nada, " + firstName + "! 😊 Ha sido un placer ayudarte. Si tienes más consultas sobre nuestros servicios deportivos, no dudes en preguntarme. ¡Que tengas un excelente día!";
                }
                return "¡De nada! 😊 Ha sido un placer ayudarte. Si tienes más consultas sobre nuestros servicios deportivos, no dudes en preguntarme. ¡Que tengas un excelente día!";

            default:
                return "🤔 No estoy seguro de entender tu consulta. Puedo ayudarte con información sobre:\n" +
                       "• 📋 Complejos deportivos\n" +
                       "• 🏟️ Instancias de un complejo\n" +
                       "• ⏰ Horarios de atención y precios\n" +
                       "• 📅 Resumen de tus reservas\n\n" +
                       "¿Podrías reformular tu pregunta o elegir uno de estos temas?";
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
            response.append("• Para consultar disponibilidad: 'disponibilidad de [instalación] para [fecha]'\n");
            response.append("• Para hacer una reserva directa: 'horarios de [instalación] para mañana'\n");
            response.append("• Ejemplos: 'disponibilidad de la cancha de fútbol para el 8 de julio'\n");
            response.append("           'horarios del gimnasio para mañana'");
            
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
        
        // Determinar si es para listar instancias o consultar disponibilidad
        boolean esListarInstancias = mensajeLimpio.contains("instancias") || 
                                   mensajeLimpio.contains("instalaciones") || 
                                   mensajeLimpio.contains("canchas");
        
        // Método principal: buscar después de "del" o "de" 
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
        
        // Si no encontramos nada con "del/de", intentar otros patrones
        if (nombreExtraido.isEmpty()) {
            // Buscar patrones específicos según el tipo de consulta
            if (esListarInstancias) {
                // Para listar instancias: "instancias complejo central"
                String patron = "(?:instancias|instalaciones|canchas)\\s+(.+)";
                if (mensajeLimpio.matches(patron)) {
                    nombreExtraido = mensajeLimpio.replaceFirst(patron, "$1").trim();
                }
            } else {
                // Para disponibilidad: "disponibilidad cancha futbol complejo central para fecha"
                if (mensajeLimpio.contains("complejo")) {
                    String[] partes = mensajeLimpio.split("complejo");
                    if (partes.length > 1) {
                        String despuesComplejo = partes[1].trim();
                        String[] palabrasComplejo = despuesComplejo.split("\\s+");
                        StringBuilder nombre = new StringBuilder();
                        
                        for (String palabra : palabrasComplejo) {
                            if (palabra.equals("para") || palabra.equals("en") || palabra.equals("el") || 
                                palabra.equals("la") || palabra.equals("los") || palabra.equals("las")) {
                                break;
                            }
                            if (!palabra.equals("deportivo") && !palabra.equals("municipal")) {
                                if (nombre.length() > 0) {
                                    nombre.append(" ");
                                }
                                nombre.append(palabra);
                            }
                        }
                        nombreExtraido = nombre.toString().trim();
                    }
                }
            }
        }
        
        // Limpiar el resultado final
        nombreExtraido = nombreExtraido.replaceAll("\\s+", " ").trim();
        
        // Si aún está vacío, intentar extraer cualquier nombre propio que parezca ser de un complejo
        if (nombreExtraido.isEmpty()) {
            // Buscar palabras capitalizadas o que podrían ser nombres
            for (String palabra : palabras) {
                // Saltar palabras muy comunes
                if (!palabra.equals("instancias") && !palabra.equals("instalaciones") && 
                    !palabra.equals("canchas") && !palabra.equals("disponibilidad") &&
                    !palabra.equals("horarios") && !palabra.equals("precios") && 
                    !palabra.equals("para") && !palabra.equals("del") && !palabra.equals("de") &&
                    !palabra.equals("la") && !palabra.equals("el") && !palabra.equals("los") &&
                    !palabra.equals("las") && !palabra.equals("complejo") && !palabra.equals("deportivo") &&
                    palabra.length() > 2) {
                    
                    // Si encontramos una palabra que podría ser nombre de complejo
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
            
            // Buscar coincidencias que contengan todas las palabras importantes
            List<ComplejoDeportivo> coincidenciasPalabras = todosComplejos.stream()
                    .filter(complejo -> {
                        String nombreComplejo = complejo.getNombre().toLowerCase();
                        String[] palabrasBuscadas = nombreBuscado.split("\\s+");
                        
                        // Solo buscar si hay al menos una palabra significativa
                        if (palabrasBuscadas.length == 0) return false;
                        
                        // Contar coincidencias de palabras importantes (más de 2 caracteres)
                        int coincidencias = 0;
                        int palabrasImportantes = 0;
                        
                        for (String palabra : palabrasBuscadas) {
                            if (palabra.length() > 2) {
                                palabrasImportantes++;
                                if (nombreComplejo.contains(palabra)) {
                                    coincidencias++;
                                }
                            }
                        }
                        
                        // Debe coincidir al menos el 80% de las palabras importantes
                        return palabrasImportantes > 0 && coincidencias >= Math.ceil(palabrasImportantes * 0.8);
                    })
                    .collect(Collectors.toList());
            
            if (!coincidenciasPalabras.isEmpty()) {
                return coincidenciasPalabras;
            }
            
            // Buscar coincidencias más permisivas (contiene al menos una palabra clave)
            return todosComplejos.stream()
                    .filter(complejo -> {
                        String nombreComplejo = complejo.getNombre().toLowerCase();
                        String[] palabrasBuscadas = nombreBuscado.split("\\s+");
                        
                        // Buscar al menos una palabra significativa (más de 3 caracteres)
                        for (String palabra : palabrasBuscadas) {
                            if (palabra.length() > 3 && nombreComplejo.contains(palabra)) {
                                return true;
                            }
                        }
                        
                        return false;
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            System.out.println("Error al buscar complejos: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Consulta la disponibilidad de horarios y precios para una instancia y fecha específica
     */
    private String consultarDisponibilidad(String mensaje, HttpSession session) {
        try {
            // Extraer información del mensaje
            String nombreComplejo = extraerNombreComplejo(mensaje);
            String nombreInstancia = extraerNombreInstancia(mensaje);
            LocalDate fecha = extraerFecha(mensaje);

            if (nombreComplejo.isEmpty() && nombreInstancia.isEmpty()) {
                return "🤔 Para consultar disponibilidad, necesito que especifiques:\n" +
                       "• El complejo deportivo\n" +
                       "• La instalación (cancha, gimnasio, etc.)\n" +
                       "• La fecha\n\n" +
                       "Ejemplo: 'disponibilidad de la cancha de fútbol del complejo central para el 25 de enero'";
            }

            if (fecha == null) {
                return "📅 No pude identificar la fecha. Por favor especifica una fecha válida.\n\n" +
                       "Ejemplos de formato válido:\n" +
                       "• 'para mañana'\n" +
                       "• 'para el 25 de enero'\n" +
                       "• 'para el 2025-01-25'";
            }

            // Validar que la fecha sea razonable
            if (fecha.isBefore(LocalDate.now())) {
                return "❌ No puedo consultar disponibilidad para fechas pasadas. Por favor especifica una fecha futura.";
            }

            if (fecha.isAfter(LocalDate.now().plusMonths(3))) {
                return "❌ Solo puedo consultar disponibilidad hasta 3 meses en el futuro. Por favor especifica una fecha más cercana.";
            }

            // Buscar el complejo y la instancia
            List<ComplejoDeportivo> complejos = buscarComplejosPorNombre(nombreComplejo);
            if (complejos.isEmpty()) {
                return String.format("❌ No encontré ningún complejo con el nombre '%s'.\n\n" +
                                   "Para ver todos los complejos disponibles, escribe: 'listar complejos'", nombreComplejo);
            }

            if (complejos.size() > 1) {
                StringBuilder response = new StringBuilder();
                response.append("🔍 Encontré varios complejos que coinciden. Por favor sé más específico:\n\n");
                for (int i = 0; i < complejos.size(); i++) {
                    response.append(String.format("%d. %s\n", (i + 1), complejos.get(i).getNombre()));
                }
                return response.toString();
            }

            ComplejoDeportivo complejo = complejos.get(0);
            List<InstanciaServicio> instancias = instanciaServicioRepository
                    .findInstanciaServicioByComplejoDeportivo(complejo.getIdComplejoDeportivo());

            // Buscar la instancia específica
            InstanciaServicio instanciaEncontrada = null;
            if (!nombreInstancia.isEmpty()) {
                instanciaEncontrada = instancias.stream()
                        .filter(inst -> inst.getNombre().toLowerCase().contains(nombreInstancia.toLowerCase()))
                        .findFirst()
                        .orElse(null);
            }

            if (instanciaEncontrada == null) {
                StringBuilder response = new StringBuilder();
                response.append(String.format("❌ No encontré la instalación '%s' en el %s.\n\n", nombreInstancia, complejo.getNombre()));
                response.append("Instalaciones disponibles:\n");
                
                var instanciasPorServicio = instancias.stream()
                        .collect(Collectors.groupingBy(i -> i.getServicio().getNombre()));
                
                for (var entrada : instanciasPorServicio.entrySet()) {
                    response.append(String.format("⚽ %s:\n", entrada.getKey()));
                    for (InstanciaServicio inst : entrada.getValue()) {
                        response.append(String.format("   • %s\n", inst.getNombre()));
                    }
                }
                
                return response.toString();
            }

            // Consultar disponibilidad usando la lógica del endpoint
            Map<String, Object> disponibilidad = verificarDisponibilidadInstancia(instanciaEncontrada.getIdInstanciaServicio(), fecha);
            
            return formatearRespuestaDisponibilidad(instanciaEncontrada, complejo, fecha, disponibilidad, session);
            
        } catch (Exception e) {
            System.out.println("Error al consultar disponibilidad: " + e.getMessage());
            return "❌ Ocurrió un error al consultar la disponibilidad. Por favor, intenta nuevamente.";
        }
    }

    /**
     * Extrae el nombre de la instancia del mensaje
     */
    private String extraerNombreInstancia(String mensaje) {
        String mensajeLimpio = mensaje.toLowerCase().trim();
        
        // Patrones más específicos para extraer nombres de instancias
        String[] patrones = {
            // Patrones para "disponibilidad de la [instancia] del [complejo]"
            "(?:disponibilidad|horarios?|precios?)\\s+de\\s+la\\s+(.+?)\\s+del\\s+",
            "(?:disponibilidad|horarios?|precios?)\\s+del\\s+(.+?)\\s+del\\s+",
            "(?:disponibilidad|horarios?|precios?)\\s+de\\s+(.+?)\\s+del\\s+",
            // Patrones para tipos específicos
            "(cancha\\s+de\\s+\\w+)(?:\\s+del\\s+|\\s+en\\s+|\\s+para\\s+)",
            "(gimnasio)(?:\\s+del\\s+|\\s+en\\s+|\\s+para\\s+)",
            "(piscina)(?:\\s+del\\s+|\\s+en\\s+|\\s+para\\s+)",
            "(salon|salón)(?:\\s+del\\s+|\\s+en\\s+|\\s+para\\s+)"
        };
        
        for (String patron : patrones) {
            if (mensajeLimpio.matches(".*" + patron + ".*")) {
                String resultado = mensajeLimpio.replaceFirst(".*" + patron + ".*", "$1").trim();
                
                // Limpiar palabras que no son parte del nombre de la instancia
                resultado = resultado.replaceAll("\\b(complejo|deportivo|municipal)\\b", "").trim();
                resultado = resultado.replaceAll("\\s+", " ");
                
                if (!resultado.isEmpty() && resultado.length() > 2) {
                    return resultado;
                }
            }
        }
        
        // Buscar palabras clave específicas de deportes
        if (mensajeLimpio.contains("fútbol") || mensajeLimpio.contains("futbol")) return "fútbol";
        if (mensajeLimpio.contains("básquet") || mensajeLimpio.contains("basquet") || mensajeLimpio.contains("basketball")) return "básquet";
        if (mensajeLimpio.contains("vóley") || mensajeLimpio.contains("voley") || mensajeLimpio.contains("volleyball")) return "vóley";
        if (mensajeLimpio.contains("tenis")) return "tenis";
        if (mensajeLimpio.contains("gimnasio")) return "gimnasio";
        if (mensajeLimpio.contains("piscina")) return "piscina";
        if (mensajeLimpio.contains("grass") || mensajeLimpio.contains("césped")) return "grass";
        
        return "";
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
            String patronISO = "\\b(\\d{4}-\\d{2}-\\d{2})\\b";
            if (mensajeLimpio.matches(".*" + patronISO + ".*")) {
                String fechaStr = mensajeLimpio.replaceFirst(".*" + patronISO + ".*", "$1");
                return LocalDate.parse(fechaStr);
            }
            
            // Formato DD/MM/YYYY o DD-MM-YYYY
            String patronDDMM = "\\b(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})\\b";
            if (mensajeLimpio.matches(".*" + patronDDMM + ".*")) {
                String[] partes = mensajeLimpio.replaceFirst(".*" + patronDDMM + ".*", "$1/$2/$3").split("/");
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
                String patron = "(?:el\\s+)?(\\d{1,2})\\s+de\\s+" + mes.getKey();
                if (mensajeLimpio.matches(".*" + patron + ".*")) {
                    String diaStr = mensajeLimpio.replaceFirst(".*" + patron + ".*", "$1");
                    int dia = Integer.parseInt(diaStr);
                    int año = LocalDate.now().getYear();
                    
                    LocalDate fecha = LocalDate.of(año, mes.getValue(), dia);
                    
                    // Si la fecha ya pasó, asumir que es para el próximo año
                    if (fecha.isBefore(LocalDate.now())) {
                        fecha = fecha.plusYears(1);
                    }
                    
                    return fecha;
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
                        String diaT = Normalizer.normalize(t.getDiaSemana().toLowerCase(), Normalizer.Form.NFD)
                                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                        return diaT.equals(diaNormalizado);
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
                        for (LocalTime h = r.getHoraInicio(); h.isBefore(r.getHoraFin()); h = h.plusHours(1)) {
                            horas.add(h);
                        }
                        return horas.stream();
                    }).toList();

            // Armar bloques con horas disponibles
            List<Map<String, Object>> bloques = new ArrayList<>();

            for (Tarifa tarifa : tarifas) {
                List<String> horasDisponibles = new ArrayList<>();

                for (LocalTime h = tarifa.getHoraInicio(); h.isBefore(tarifa.getHoraFin()); h = h.plusHours(1)) {
                    if (!horasOcupadas.contains(h)) {
                        horasDisponibles.add(h.toString());
                    }
                }

                if (!horasDisponibles.isEmpty()) {
                    Map<String, Object> bloque = new HashMap<>();
                    bloque.put("inicioTarifa", tarifa.getHoraInicio().toString());
                    bloque.put("finTarifa", tarifa.getHoraFin().toString());
                    bloque.put("monto", tarifa.getMonto());
                    bloque.put("horasDisponibles", horasDisponibles);
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
     * Formatea la respuesta de disponibilidad para mostrar al usuario
     */
    private String formatearRespuestaDisponibilidad(InstanciaServicio instancia, ComplejoDeportivo complejo, 
                                                    LocalDate fecha, Map<String, Object> disponibilidad, HttpSession session) {
        
        Boolean fechaLlena = (Boolean) disponibilidad.get("fechaLlena");
        List<Map<String, Object>> bloques = (List<Map<String, Object>>) disponibilidad.get("bloques");
        
        DateTimeFormatter fechaFormateada = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        
        StringBuilder response = new StringBuilder();
        response.append(String.format("📅 Disponibilidad para %s\n", instancia.getNombre()));
        response.append(String.format("🏟️ Complejo: %s\n", complejo.getNombre()));
        response.append(String.format("📆 Fecha: %s\n\n", fecha.format(fechaFormateada)));
        
        if (fechaLlena || bloques.isEmpty()) {
            response.append("❌ No hay horarios disponibles para esta fecha.\n\n");
            response.append("💡 Tip: Intenta con otra fecha o consulta las instalaciones disponibles en otros complejos.");
            return response.toString();
        }
        
        response.append("✅ Horarios disponibles:\n\n");
        
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
        
        // Guardar información en sesión para posible reserva
        session.setAttribute("chatbot_complejo", complejo);
        session.setAttribute("chatbot_instancia", instancia);
        session.setAttribute("chatbot_fecha", fecha);
        session.setAttribute("chatbot_bloques", bloques);
        
        response.append("🎯 ¿Te gustaría hacer una reserva para alguno de estos horarios?\n");
        response.append("Responde 'sí' para iniciar el proceso de reserva o 'cancelar' para salir.");
        
        // Establecer estado para flujo de reserva
        session.setAttribute("chatbot_estado", "ESPERANDO_CONFIRMACION_RESERVA");
        
        return response.toString();
    }

    /**
     * Maneja el flujo conversacional de reserva
     */
    private String manejarFlujoReserva(String mensaje, Usuario usuario, HttpSession session, String estado) {
        String text = mensaje.toLowerCase().trim();
        
        switch (estado) {
            case "ESPERANDO_CONFIRMACION_RESERVA":
                if (text.contains("sí") || text.contains("si") || text.contains("yes") || text.contains("ok") || text.contains("vale")) {
                    session.setAttribute("chatbot_estado", "SELECCIONANDO_HORARIO");
                    return "🎯 Perfecto! Para continuar con la reserva, por favor especifica:\n\n" +
                           "• La hora exacta que deseas reservar\n" +
                           "• La duración (1 hora, 2 horas, etc.)\n\n" +
                           "Ejemplo: 'reservar de 14:00 a 16:00' o 'quiero la 15:00 por 2 horas'\n\n" +
                           "💡 Recuerda que puedes escribir 'cancelar' en cualquier momento para salir.";
                } else {
                    // Limpiar sesión
                    session.removeAttribute("chatbot_estado");
                    session.removeAttribute("chatbot_complejo");
                    session.removeAttribute("chatbot_instancia");
                    session.removeAttribute("chatbot_fecha");
                    session.removeAttribute("chatbot_bloques");
                    return "👍 Entendido. Si cambias de opinión y quieres hacer una reserva, puedes consultar la disponibilidad nuevamente.";
                }
                
            case "SELECCIONANDO_HORARIO":
                return procesarSeleccionHorario(mensaje, usuario, session);
                
            case "CONFIRMANDO_RESERVA":
                return confirmarReserva(mensaje, usuario, session);
                
            default:
                // Estado desconocido, limpiar
                session.removeAttribute("chatbot_estado");
                return "❌ Ocurrió un error en el proceso. Por favor, inicia nuevamente tu consulta.";
        }
    }

    /**
     * Procesa la selección de horario del usuario
     */
    private String procesarSeleccionHorario(String mensaje, Usuario usuario, HttpSession session) {
        try {
            // Extraer horarios del mensaje
            LocalTime horaInicio = null;
            LocalTime horaFin = null;
            
            String text = mensaje.toLowerCase().trim();
            
            // Patrones para extraer horarios
            String patron1 = ".*(?:de\\s+|desde\\s+|a\\s+partir\\s+de\\s+)(\\d{1,2}:?\\d{0,2}).*(?:hasta\\s+|a\\s+|\\-|–)(\\d{1,2}:?\\d{0,2}).*";
            String patron2 = ".*(?:la\\s+|las\\s+)?(\\d{1,2}:?\\d{0,2}).*(?:por\\s+|durante\\s+)(\\d+)\\s*horas?.*";
            String patron3 = ".*(\\d{1,2}:?\\d{0,2}).*";
            
            if (text.matches(patron1)) {
                String hora1 = text.replaceFirst(patron1, "$1").replace(":", "");
                String hora2 = text.replaceFirst(patron1, "$2").replace(":", "");
                
                horaInicio = parsearHora(hora1);
                horaFin = parsearHora(hora2);
            } else if (text.matches(patron2)) {
                String hora = text.replaceFirst(patron2, "$1").replace(":", "");
                String duracionStr = text.replaceFirst(patron2, "$2");
                
                horaInicio = parsearHora(hora);
                if (horaInicio != null) {
                    int duracion = Integer.parseInt(duracionStr);
                    horaFin = horaInicio.plusHours(duracion);
                }
            } else if (text.matches(patron3)) {
                String hora = text.replaceFirst(patron3, "$1").replace(":", "");
                horaInicio = parsearHora(hora);
                if (horaInicio != null) {
                    horaFin = horaInicio.plusHours(1); // Por defecto 1 hora
                }
            }
            
            if (horaInicio == null) {
                return "🤔 No pude entender el horario. Por favor especifica la hora de manera más clara.\n\n" +
                       "Ejemplos válidos:\n" +
                       "• 'de 14:00 a 16:00'\n" +
                       "• 'las 15:00 por 2 horas'\n" +
                       "• 'quiero la 16:00'\n\n" +
                       "O escribe 'cancelar' para salir.";
            }
            
            // Validar que el horario esté disponible
            List<Map<String, Object>> bloques = (List<Map<String, Object>>) session.getAttribute("chatbot_bloques");
            boolean horarioValido = false;
            Double montoTotal = 0.0;
            
            for (Map<String, Object> bloque : bloques) {
                List<String> horasDisponibles = (List<String>) bloque.get("horasDisponibles");
                Double monto = (Double) bloque.get("monto");
                
                // Verificar si todas las horas solicitadas están disponibles
                boolean todasDisponibles = true;
                long horasNecesarias = java.time.Duration.between(horaInicio, horaFin).toHours();
                
                for (LocalTime h = horaInicio; h.isBefore(horaFin); h = h.plusHours(1)) {
                    if (!horasDisponibles.contains(h.toString())) {
                        todasDisponibles = false;
                        break;
                    }
                }
                
                if (todasDisponibles) {
                    horarioValido = true;
                    montoTotal = monto * horasNecesarias;
                    break;
                }
            }
            
            if (!horarioValido) {
                return "❌ El horario solicitado no está completamente disponible.\n\n" +
                       "Por favor elige un horario que esté dentro de los bloques disponibles mostrados anteriormente, " +
                       "o escribe 'cancelar' para salir.";
            }
            
            // Guardar selección y mostrar resumen
            session.setAttribute("chatbot_hora_inicio", horaInicio);
            session.setAttribute("chatbot_hora_fin", horaFin);
            session.setAttribute("chatbot_monto_total", montoTotal);
            session.setAttribute("chatbot_estado", "CONFIRMANDO_RESERVA");
            
            ComplejoDeportivo complejo = (ComplejoDeportivo) session.getAttribute("chatbot_complejo");
            InstanciaServicio instancia = (InstanciaServicio) session.getAttribute("chatbot_instancia");
            LocalDate fecha = (LocalDate) session.getAttribute("chatbot_fecha");
            
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
            
        } catch (Exception e) {
            System.out.println("Error al procesar selección de horario: " + e.getMessage());
            return "❌ Ocurrió un error al procesar tu selección. Por favor intenta nuevamente o escribe 'cancelar'.";
        }
    }

    /**
     * Parsea una hora en formato string a LocalTime
     */
    private LocalTime parsearHora(String horaStr) {
        try {
            // Limpiar y normalizar
            horaStr = horaStr.trim();
            
            // Si no tiene : agregar
            if (!horaStr.contains(":")) {
                if (horaStr.length() <= 2) {
                    horaStr = horaStr + ":00";
                } else {
                    horaStr = horaStr.substring(0, horaStr.length() - 2) + ":" + horaStr.substring(horaStr.length() - 2);
                }
            }
            
            return LocalTime.parse(horaStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Confirma y crea la reserva
     */
    private String confirmarReserva(String mensaje, Usuario usuario, HttpSession session) {
        String text = mensaje.toLowerCase().trim();
        
        if (text.contains("confirmar") || text.contains("confirmo") || text.contains("sí") || text.contains("si")) {
            
            if (usuario == null) {
                return "❌ Para confirmar la reserva necesitas estar autenticado. Por favor inicia sesión y vuelve a intentar.";
            }
            
            try {
                // Obtener datos de la sesión
                ComplejoDeportivo complejo = (ComplejoDeportivo) session.getAttribute("chatbot_complejo");
                InstanciaServicio instancia = (InstanciaServicio) session.getAttribute("chatbot_instancia");
                LocalDate fecha = (LocalDate) session.getAttribute("chatbot_fecha");
                LocalTime horaInicio = (LocalTime) session.getAttribute("chatbot_hora_inicio");
                LocalTime horaFin = (LocalTime) session.getAttribute("chatbot_hora_fin");
                Double montoTotal = (Double) session.getAttribute("chatbot_monto_total");
                
                // Crear la información de pago primero
                InformacionPago pago = new InformacionPago();
                pago.setFecha(LocalDate.now());
                pago.setHora(LocalTime.now());
                pago.setTipo("Pendiente");
                pago.setTotal(montoTotal);
                pago.setEstado("Pendiente");
                InformacionPago pagoGuardado = informacionPagoRepository.save(pago);
                
                // Crear la reserva
                Reserva reserva = new Reserva();
                reserva.setUsuario(usuario);
                reserva.setInstanciaServicio(instancia);
                reserva.setFecha(fecha);
                reserva.setHoraInicio(horaInicio);
                reserva.setHoraFin(horaFin);
                reserva.setEstado(0); // Pendiente
                reserva.setFechaHoraRegistro(LocalDateTime.now());
                reserva.setInformacionPago(pagoGuardado);
                
                // Guardar la reserva
                Reserva reservaGuardada = reservaRepository.save(reserva);
                
                // Limpiar sesión
                session.removeAttribute("chatbot_estado");
                session.removeAttribute("chatbot_complejo");
                session.removeAttribute("chatbot_instancia");
                session.removeAttribute("chatbot_fecha");
                session.removeAttribute("chatbot_bloques");
                session.removeAttribute("chatbot_hora_inicio");
                session.removeAttribute("chatbot_hora_fin");
                session.removeAttribute("chatbot_monto_total");
                
                DateTimeFormatter fechaFormateada = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
                
                return String.format("✅ ¡Reserva confirmada exitosamente!\n\n" +
                                   "📋 Detalles de tu reserva:\n" +
                                   "🔢 ID de reserva: %d\n" +
                                   "🏟️ Complejo: %s\n" +
                                   "⚽ Instalación: %s\n" +
                                   "📅 Fecha: %s\n" +
                                   "🕐 Horario: %s a %s\n" +
                                   "💰 Costo total: S/ %.2f\n" +
                                   "📊 Estado: Pendiente de confirmación\n\n" +
                                   "📧 Recibirás un correo de confirmación pronto. " +
                                   "¡Gracias por confiar en nosotros!",
                                   reservaGuardada.getIdReserva(),
                                   complejo.getNombre(),
                                   instancia.getNombre(),
                                   fecha.format(fechaFormateada),
                                   horaInicio.toString(),
                                   horaFin.toString(),
                                   montoTotal);
                
            } catch (Exception e) {
                System.out.println("Error al crear reserva: " + e.getMessage());
                return "❌ Ocurrió un error al procesar tu reserva. Por favor intenta nuevamente más tarde o contacta al administrador.";
            }
            
        } else {
            // Cancelar reserva
            session.removeAttribute("chatbot_estado");
            session.removeAttribute("chatbot_complejo");
            session.removeAttribute("chatbot_instancia");
            session.removeAttribute("chatbot_fecha");
            session.removeAttribute("chatbot_bloques");
            session.removeAttribute("chatbot_hora_inicio");
            session.removeAttribute("chatbot_hora_fin");
            session.removeAttribute("chatbot_monto_total");
            
            return "❌ Reserva cancelada. Si cambias de opinión, puedes consultar la disponibilidad nuevamente cuando gustes.";
        }
    }
}
