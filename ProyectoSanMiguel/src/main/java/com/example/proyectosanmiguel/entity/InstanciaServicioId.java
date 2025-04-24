package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class InstanciaServicioId implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idInstanciaServicio", nullable = false)
    private Integer idInstanciaServicio;
    @Column(name = "idServicio", nullable = false)
    private Integer idServicio;
    @Column(name = "idComplejoDeportivo", nullable = false)
    private Integer idComplejoDeportivo;

}
