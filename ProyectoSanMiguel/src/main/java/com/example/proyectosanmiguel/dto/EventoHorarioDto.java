package com.example.proyectosanmiguel.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public interface EventoHorarioDto {
    Integer getIdHorario();
    LocalDate getFecha();
    LocalTime getHoraIngreso();
    LocalTime getHoraSalida();
    String getNombreComplejo();
    Integer getIdComplejoDeportivo();
    Integer getIdCoordinador();
    String getNombreCoordinador();
}