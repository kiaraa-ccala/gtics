package com.example.proyectosanmiguel.config;


import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public HttpClient httpClient() {
        return HttpClients.custom()
                .setMaxConnTotal(100) // Configura el número máximo de conexiones totales
                .setMaxConnPerRoute(20) // Configura el número máximo de conexiones por ruta
                .build();
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
