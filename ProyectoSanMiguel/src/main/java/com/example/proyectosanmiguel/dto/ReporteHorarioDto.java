package com.example.proyectosanmiguel.dto;

import java.time.LocalDate;

public interface ReporteHorarioDto {

    String getNombreCoordinador();
    String getNombreComplejo();
    LocalDate getFecha();
    String getEstadoFinal();
}
