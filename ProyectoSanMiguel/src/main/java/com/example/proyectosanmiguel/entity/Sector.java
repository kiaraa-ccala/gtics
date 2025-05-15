package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "sector")
public class Sector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSector", nullable = false)
    private Integer idSector;
    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;
}
