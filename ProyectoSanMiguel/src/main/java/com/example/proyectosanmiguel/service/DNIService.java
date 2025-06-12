package com.example.proyectosanmiguel.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class DNIService {

    private final WebClient webClient;

    @Value("${api.token.reniec}")
    private String token;

    public DNIService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.apis.net.pe/v2/reniec/").build();
    }

    // Metodo para obtener datos del DNI
    public Mono<String> obtenerDatosDni(String numeroDni) {
        return webClient.get()
                .uri("dni?numero={numero}", numeroDni)  // La URL de la API con el parámetro número
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)  // Devuelve la respuesta como un Mono<String> (JSON)
                .onErrorReturn("Error al obtener los datos del DNI");
    }

}
