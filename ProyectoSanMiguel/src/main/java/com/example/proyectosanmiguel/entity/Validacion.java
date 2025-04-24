package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "Validacion")
public class Validacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idValidacion", nullable = false)
    private Integer idValidacion;
    @Column(name="timeStampValidacion", nullable = false)
    private LocalDateTime timeStampValidacion;
    @Column(name = "latitudCoordinador", nullable = false)
    private Double latitudCoordinador;
    @Column(name = "longitudCoordinador", nullable = false)
    private Double longitudCoordinador;
    @Column(name = "distanciaError")
    private Double distanciaError;
    @Column(name = "resultado", length = 45)
    private String resultado;
    @ManyToOne
    @JoinColumn(name = "idHorario")
    private Horario horario;
}
