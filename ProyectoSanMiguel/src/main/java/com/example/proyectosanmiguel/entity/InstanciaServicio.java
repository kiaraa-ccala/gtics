package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "InstanciaServicio")
public class InstanciaServicio {

    @EmbeddedId
    private InstanciaServicioId id;

    @MapsId("idServicio")
    @ManyToOne
    @JoinColumn(name = "idServicio")
    private Servicio servicio;

    @MapsId("idComplejoDeportivo")
    @ManyToOne
    @JoinColumn(name = "idComplejoDeportivo")
    private ComplejoDeportivo complejoDeportivo;

    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;

    @Column(name = "capacidadMaxima", nullable = false, length = 45)
    private String capacidadMaxima;

    @Column(name = "modoAcceso", nullable = false, length = 45)
    private String modoAcceso;

}
