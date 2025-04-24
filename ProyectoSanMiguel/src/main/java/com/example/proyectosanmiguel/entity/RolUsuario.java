package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;

public class RolUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRolUsuario", nullable = false)
    private Integer idRolUsuario;

    @ManyToOne
    @JoinColumn(name = "idUsuario")
    private Usuario idUsuario;

    @ManyToOne
    @JoinColumn(name = "idRol")
    private Roles rol;

}
