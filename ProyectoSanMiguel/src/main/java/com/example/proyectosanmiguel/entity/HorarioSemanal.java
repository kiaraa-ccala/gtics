package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "HorarioSemanal")
public class HorarioSemanal {

    @EmbeddedId
    private HorarioSemanalId id;

    @MapsId("idAdministrador")
    @ManyToOne
    @JoinColumn(name = "idAdministrador")
    private Usuario administrador;

    @MapsId("idCoordinador")
    @ManyToOne
    @JoinColumn(name = "idCoordinador")
    private Usuario coordinador;

    @Column(name = "fechaInicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fechaFin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "fechaCreacion", nullable = false)
    private LocalDate fechaCreacion;
}
