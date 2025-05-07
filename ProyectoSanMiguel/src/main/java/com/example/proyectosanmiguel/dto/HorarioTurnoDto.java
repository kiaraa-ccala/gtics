package com.example.proyectosanmiguel.dto;

import java.time.LocalTime;

public interface HorarioTurnoDto {
    String getDiaSemana();
    LocalTime getHoraInicio();
    LocalTime getHoraFin();
    String getNombreComplejo();
}
