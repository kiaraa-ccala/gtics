package com.example.proyectosanmiguel.chatbot;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/ai")
@ConditionalOnProperty(name = "chatbot.enabled", havingValue = "true", matchIfMissing = false)
public class ChatController {

    private final ChatClient chatClient;
    private final EmbeddingHelper embeddingHelper;
    private final List<DocumentEmbedding> embeddings;    public ChatController(ChatClient.Builder builder, EmbeddingHelper embeddingHelper, PdfTxtLoader pdfTxtLoader) throws Exception {

        this.embeddingHelper = embeddingHelper;
        this.chatClient = builder
                .defaultSystem("Eres un asistente virtual para una página web de la Municipalidad de San Miguel, Lima, Perú. \n" +
                        "Estás orientado a resolver consultas y ayudar al usuario a conocer información de los servicios deportivos que existen en este distrito, \n" +
                        "los horarios de atención y los precios de los servicios según cada horario.\n" +
                        "Responde en un tono claro, amigable y técnico si es necesario.\n" +
                        "Si no tienes información suficiente, indica que no puedes responder la pregunta.")
                .build();

        //Cargamos los documentos PDF y TXT y vectorizamos el texto
        try {
            String fullText = pdfTxtLoader.loadTxt(Path.of("src/main/resources/context/vectorizable_sistema_reservas.txt"))
                    + "\n" + pdfTxtLoader.loadTxt(Path.of("src/main/resources/context/GeneralData.txt"));
            List<String> chunks = embeddingHelper.splitIntoChunks(fullText, 150);
            this.embeddings = embeddingHelper.vectorizeChunks(chunks);
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar los documentos de contexto: " + e.getMessage(), e);
        }
    }

    @PostMapping("/chat")
    public String chat(@RequestParam String message) {
        // Buscar fragmentos relevantes del contexto
        List<DocumentEmbedding> relevantChunks = embeddingHelper.findRelevantChunks(embeddings, message, 3);
        
        // Construir contexto con los fragmentos más relevantes
        StringBuilder context = new StringBuilder();
        for (DocumentEmbedding chunk : relevantChunks) {
            context.append(chunk.content()).append("\n\n");
        }
        
        return chatClient.prompt()
                .system("Basándote en el siguiente contexto sobre los servicios deportivos de la Municipalidad de San Miguel:\n\n" + 
                        context +
                        "\n\nResponde la pregunta del usuario de manera clara y precisa.")
                .user(message)
                .call()
                .content();
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam String message) {
        SseEmitter emitter = new SseEmitter(30000L); // 30 segundos timeout
        
        try {
            // Buscar fragmentos relevantes del contexto
            List<DocumentEmbedding> relevantChunks = embeddingHelper.findRelevantChunks(embeddings, message, 3);
            
            // Construir contexto con los fragmentos más relevantes
            StringBuilder context = new StringBuilder();
            for (DocumentEmbedding chunk : relevantChunks) {
                context.append(chunk.content()).append("\n\n");
            }
            
            // Crear el stream usando ChatClient
            Flux<String> responseStream = chatClient.prompt()
                    .system("Basándote en el siguiente contexto sobre los servicios deportivos de la Municipalidad de San Miguel:\n\n" + 
                            context +
                            "\n\nResponde la pregunta del usuario de manera clara y precisa.")
                    .user(message)
                    .stream()
                    .content();
            
            // Procesar el stream
            responseStream.subscribe(
                chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                },
                error -> emitter.completeWithError(error),
                () -> emitter.complete()
            );
            
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        
        return emitter;
    }

}
