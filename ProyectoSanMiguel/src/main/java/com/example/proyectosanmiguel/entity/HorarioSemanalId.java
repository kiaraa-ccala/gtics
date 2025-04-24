package com.example.proyectosanmiguel.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class HorarioSemanalId implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHorarioSemanal", nullable = false)
    private Integer idHorarioSemanal;
    @Column(name = "idAdministrador", nullable = false)
    private Integer idAdministrador;
    @Column(name = "idCoordinador", nullable = false)
    private Integer idCoordinador;
}
