package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
;

@Entity
@Getter
@Setter

@Table(name = "ComplejoDeportivo")
public class ComplejoDeportivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idComplejoDeportivo")
    private Long id;

    @Column(name = "nombre")
    private String nombre;
    @Column(name = "direccion")
    private String direccion;

    @ManyToOne
    @JoinColumn(name = "idSector")
    private Sector sector;

    @Column(name = "numeroSoporte")
    private String numeroSoporte;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;
}
