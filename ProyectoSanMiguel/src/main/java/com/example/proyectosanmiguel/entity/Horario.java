package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@Table(name = "horario")
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHorario")
    private Integer idHorario;

    @Column(name = "idHorarioSemanal")
    private Integer idHorarioSemanal;

    @Column(name = "idComplejoDeportivo")
    private Integer idComplejoDeportivo;

    // Relaciones
    @ManyToOne
    @JoinColumn(name = "idHorarioSemanal", referencedColumnName = "idHorarioSemanal", insertable = false, updatable = false)
    private HorarioSemanal horarioSemanal;

    @ManyToOne
    @JoinColumn(name = "idComplejoDeportivo", insertable = false, updatable = false)
    private ComplejoDeportivo complejoDeportivo;

    // Otros campos
    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "horaIngreso")
    private LocalTime horaIngreso;

    @Column(name = "horaSalida")
    private LocalTime horaSalida;
}