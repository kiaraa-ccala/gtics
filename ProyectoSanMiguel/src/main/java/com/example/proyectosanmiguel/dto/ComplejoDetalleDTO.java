package com.example.proyectosanmiguel.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class ComplejoDetalleDTO {
    private int id;
    private String nombre;
    private String direccion;
    private String sector;
    private BigDecimal latitud;
    private BigDecimal longitud;

    private List<ServicioDTO> servicios;
    private List<TarifaDTO> tarifas;
    private List<FotoDTO> fotos;
    private List<ServicioAdicionalDTO> serviciosAdicionales; // Servicios como WiFi, duchas, etc.

    private String horarioAperturaSemana;
    private String horarioCierreSemana;
    private String horarioAperturaFin;
    private String horarioCierreFin;

    @Getter
    @Setter
    public static class ServicioDTO {
        private String nombre;
        private String nombreInstancia;
        private String capacidadMaxima;
        private String modoAcceso;
        private int idInstancia; // Agregado para permitir selección de instancia
    }

    @Getter
    @Setter
    public static class TarifaDTO {
        private String nombreServicio;
        private String diaSemana;
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private BigDecimal monto;
    }

    @Getter
    @Setter
    public static class FotoDTO {
        private String url;
        private String nombre;
    }

    @Getter
    @Setter
    public static class ServicioAdicionalDTO {
        private String nombre;
        private String descripcion;
        private String icono; // nombre de icono opcional para visualización (ej: bi-wifi)
    }
}
