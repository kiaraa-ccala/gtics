package com.example.proyectosanmiguel.dto;

import java.time.LocalTime;

public interface HorarioTurnoDto {
    Integer getId();
    Integer getIdHorarioSemanal();
    String getDiaSemana();
    LocalTime getHoraInicio();
    LocalTime getHoraFin();
    String getNombreComplejo();
}
