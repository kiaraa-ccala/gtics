package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "horariosemanal")
public class HorarioSemanal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHorarioSemanal", nullable = false)
    private Integer idHorarioSemanal;


    @Column(name = "idAdministrador", nullable = false)
    private Integer idAdministrador;


    @Column(name = "idCoordinador", nullable = false)
    private Integer idCoordinador;

    // Relaciones
    @ManyToOne
    @JoinColumn(name = "idAdministrador", insertable = false, updatable = false)
    private Usuario administrador;

    @ManyToOne
    @JoinColumn(name = "idCoordinador", insertable = false, updatable = false)
    private Usuario coordinador;

    @Column(name = "fechaInicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fechaFin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "fechaCreacion", nullable = false)
    private LocalDate fechaCreacion;
}