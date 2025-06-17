package com.example.proyectosanmiguel.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroAsistenciaRequest {
    private String tipoRegistro;
    private Double latitud;
    private Double longitud;
    private Double precision;
    private Double distanciaComplejo;
    private Integer idComplejo;
}