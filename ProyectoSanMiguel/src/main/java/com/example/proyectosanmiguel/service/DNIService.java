package com.example.proyectosanmiguel.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class DNIService {


    @Value("${api.token.reniec}")
    private String token;

    public String obtenerDatosDni(String numeroDni) {
        try {
            String url = "https://api.apis.net.pe/v2/reniec/dni?numero=" + numeroDni;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body(); // JSON como String

        } catch (Exception e) {
            return "{\"error\":\"No se pudo consultar el DNI\"}";
        }
    }

    public Map<String, String> obtenerNombresDni(String numeroDni) {
        Map<String, String> resultado = new HashMap<>();
        try {
            String json = obtenerDatosDni(numeroDni);
            JSONObject obj = new JSONObject(json);

            resultado.put("nombres", obj.getString("nombres"));
            resultado.put("apellidoPaterno", obj.getString("apellidoPaterno"));
            resultado.put("apellidoMaterno", obj.getString("apellidoMaterno"));

        } catch (Exception e) {
            resultado.put("error", "No se pudo procesar el DNI");
        }
        return resultado;
    }

}
