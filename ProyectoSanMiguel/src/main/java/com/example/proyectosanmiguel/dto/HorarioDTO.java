package com.example.proyectosanmiguel.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class HorarioDTO {

    private Integer id;
    private LocalDate fecha;
    private String diaSemana;
    private LocalTime horaIngreso;
    private LocalTime horaSalida;
    private String complejoNombre;
    private String direccionComplejo;
    private String semanaTexto;


}
