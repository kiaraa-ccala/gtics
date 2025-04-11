package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Sector")
public class Sector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSector", nullable = false)
    private Integer idSector;
    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;
}
