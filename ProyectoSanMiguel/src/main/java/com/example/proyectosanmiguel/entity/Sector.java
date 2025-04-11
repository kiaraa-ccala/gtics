package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Sector")
public class Sector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSector")
    private Integer id;

    private String nombre;
}
