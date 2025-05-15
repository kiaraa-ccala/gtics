package com.example.proyectosanmiguel.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HorarioDiarioTurnoGuardarDto {
    private Integer id;
    private Integer idHorarioSemanal;
    private String diaSemana;
    private String horaInicio;
    private String horaFin;
    private String nombreComplejo;
    private Integer idComplejoDeportivo;


}
