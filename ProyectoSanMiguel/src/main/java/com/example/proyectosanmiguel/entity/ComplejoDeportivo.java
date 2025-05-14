package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
;

@Entity
@Getter
@Setter
@Table(name = "complejodeportivo")
public class ComplejoDeportivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idComplejoDeportivo", nullable = false)
    private Integer idComplejoDeportivo;
    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;
    @Column(name = "direccion", nullable = false, length = 45)
    private String direccion;
    @ManyToOne
    @JoinColumn(name = "idSector")
    private Sector sector;
    @Column(name = "numeroSoporte", nullable = false, length = 45)
    private String numeroSoporte;
    @Column(name = "latitud", nullable = false)
    private Double latitud;
    @Column(name = "longitud", nullable = false)
    private Double longitud;
}
