package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class HorarioId implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHorario", nullable = false)
    private Integer idHorario;
    @Column(name = "idHorarioSemanal", nullable = false)
    private Integer idHorarioSemanal;
    @Column(name = "idAdministrador", nullable = false)
    private Integer idAdministrador;
    @Column(name = "idCoordinador", nullable = false)
    private Integer idCoordinador;
    @Column(name = "idComplejoDeportivo", nullable = false)
    private Integer idComplejoDeportivo;
}
