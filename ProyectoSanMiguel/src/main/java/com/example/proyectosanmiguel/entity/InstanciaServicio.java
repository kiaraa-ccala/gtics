package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@IdClass(InstanciaServicioId.class)
@Table(name = "InstanciaServicio")
public class InstanciaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idInstanciaServicio", nullable = false)
    private Integer idInstanciaServicio;

    @Id
    @Column(name = "idServicio", nullable = false)
    private Integer idServicio;

    @Id
    @Column(name = "idComplejoDeportivo", nullable = false)
    private Integer idComplejoDeportivo;

    @ManyToOne
    @JoinColumn(name = "idServicio", insertable = false, updatable = false)
    private Servicio servicio;

    @ManyToOne
    @JoinColumn(name = "idComplejoDeportivo", insertable = false, updatable = false)
    private ComplejoDeportivo complejoDeportivo;

    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;

    @Column(name = "capacidadMaxima", nullable = false, length = 45)
    private String capacidadMaxima;

    @Column(name = "modoAcceso", nullable = false, length = 45)
    private String modoAcceso;
}