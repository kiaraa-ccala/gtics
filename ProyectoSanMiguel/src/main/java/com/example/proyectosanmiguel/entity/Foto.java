package com.example.proyectosanmiguel.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "foto")
public class Foto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFoto", nullable = false)
    private Integer idFoto;

    @Column(name = "nombreFoto", nullable = false, length = 80)
    private String nombreFoto;

    @Lob
    @Column(name = "foto")
    private byte[] foto;

    @Column(name = "urlFoto", nullable = false, length = 100)
    private String urlFoto;

}
