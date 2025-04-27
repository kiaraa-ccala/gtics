package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "InstanciaServicio")
public class InstanciaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idInstanciaServicio", nullable = false)
    private Integer idInstanciaServicio;

    @ManyToOne
    @JoinColumn(name = "idServicio", nullable = false)
    private Servicio servicio;

    @ManyToOne
    @JoinColumn(name = "idComplejoDeportivo", nullable = false)
    private ComplejoDeportivo complejoDeportivo;

    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;

    @Column(name = "capacidadMaxima", nullable = false, length = 45)
    private String capacidadMaxima;

    @Column(name = "modoAcceso", nullable = false, length = 45)
    private String modoAcceso;
}
