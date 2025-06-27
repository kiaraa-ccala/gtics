package com.example.proyectosanmiguel.chatbot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/ai")
@ConditionalOnProperty(name = "chatbot.enabled", havingValue = "false", matchIfMissing = true)
public class MockChatController {

    @PostMapping("/chat")
    public String chat(@RequestParam String message) {
        return generateMockResponse(message);
    }    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam String message) {
        SseEmitter emitter = new SseEmitter(30000L);
        
        CompletableFuture.runAsync(() -> {
            try {
                String response = generateMockResponse(message);
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
    public SseEmitter chatStreamGet(@RequestParam String message) {
        SseEmitter emitter = new SseEmitter(30000L);
        
        CompletableFuture.runAsync(() -> {
            try {
                String response = generateMockResponse(message);
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

    private String generateMockResponse(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("horario") || lowerMessage.contains("hora")) {
            return "Los horarios de atención de nuestros complejos deportivos son: Lunes a Viernes de 6:00 AM a 10:00 PM, Sábados y Domingos de 7:00 AM a 8:00 PM. ¿Te gustaría información sobre algún complejo específico?";
        } else if (lowerMessage.contains("precio") || lowerMessage.contains("costo") || lowerMessage.contains("tarifa")) {
            return "Nuestras tarifas varían según el horario: Horario Regular (6:00-16:00): S/10 por hora. Horario Premium (16:00-22:00): S/15 por hora. Fines de semana: S/12 por hora. ¿Necesitas información sobre descuentos o membresías?";
        } else if (lowerMessage.contains("complejo") || lowerMessage.contains("cancha") || lowerMessage.contains("gimnasio")) {
            return "Contamos con varios complejos deportivos: Polideportivo Maranga, Parque Juan Pablo II, Complejo Deportivo Pando, Parque Media Luna, y más. Cada uno ofrece diferentes instalaciones como canchas de fútbol, básquet, vóley y gimnasios. ¿Sobre cuál te gustaría saber más?";
        } else if (lowerMessage.contains("reserva") || lowerMessage.contains("reservar")) {
            return "Para realizar una reserva, puedes hacerlo a través de nuestra plataforma web ingresando a tu cuenta de vecino. Necesitarás seleccionar el complejo, la cancha, fecha y horario disponible. ¿Te ayudo con algún paso específico del proceso?";
        } else if (lowerMessage.contains("hola") || lowerMessage.contains("buenos")) {
            return "¡Hola! Soy el asistente virtual de la Municipalidad de San Miguel. Estoy aquí para ayudarte con información sobre nuestros servicios deportivos, horarios, precios y reservas. ¿En qué puedo ayudarte hoy?";
        } else if (lowerMessage.contains("gracias")) {
            return "¡De nada! Es un placer ayudarte. Si tienes más preguntas sobre nuestros servicios deportivos, no dudes en consultarme. ¡Que tengas un excelente día!";
        } else {
            return "Gracias por tu consulta. Como asistente virtual de la Municipalidad de San Miguel, puedo ayudarte con información sobre: horarios de atención, precios y tarifas, complejos deportivos disponibles, y proceso de reservas. ¿Sobre qué tema específico te gustaría saber más?";
        }
    }
}
