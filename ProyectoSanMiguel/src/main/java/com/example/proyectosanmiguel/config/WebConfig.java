package com.example.proyectosanmiguel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configurar el mapeo para servir archivos de comprobantes
        registry.addResourceHandler("/uploads/comprobantes/**")
                .addResourceLocations("file:uploads/comprobantes/")
                .setCachePeriod(3600); // Cache por 1 hora
    }
}
