package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "tercerizado")
public class Tercerizado implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTercerizado", nullable = false)
    private Integer idTercerizado;

    @Column(name = "ruc", nullable = false, length = 11)
    private String ruc;

    @Column(name = "direccionFiscal", nullable = false, length = 45)
    private String direccionFiscal;
}
