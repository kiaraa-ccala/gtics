package com.example.proyectosanmiguel.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class HorarioSemanalGuardarDto {
    private Integer coordinadorId;
    private String semana;
    private List<HorarioDiarioTurnoGuardarDto> horariosGuardar;
    private List<HorarioEliminarDto> horariosEliminar;
}
