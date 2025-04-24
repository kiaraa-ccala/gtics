package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@Table(name = "Horario")
public class Horario {

    @EmbeddedId
    private HorarioId id;

    @MapsId("idHorarioSemanal")
    @ManyToOne
    @JoinColumn(name = "idHorarioSemanal")
    private HorarioSemanal horarioSemanal;

    @MapsId("idHorarioSemanal")
    @ManyToOne
    @JoinColumn(name = "idComplejoDeportivo")
    private ComplejoDeportivo complejoDeportivo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "horaIngreso", nullable = false)
    private LocalTime horaIngreso;

    @Column(name = "horaSalida", nullable = false)
    private LocalTime horaSalida;

}
