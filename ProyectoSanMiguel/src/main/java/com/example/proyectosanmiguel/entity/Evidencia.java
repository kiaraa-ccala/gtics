package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "Evidencia")
public class Evidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEvidencia", nullable = false)
    private Integer idEvidencia;

    @Column(name = "nombreArchivo", nullable = false, length = 100)
    private String nombreArchivo;

    @Column(name = "urlArchivo", nullable = false, length = 100)
    private String urlArchivo;

    @Lob
    @Column(name = "archivo")
    private byte[] archivo;

    @ManyToOne
    @JoinColumn(name = "idReporte")
    private Reporte reporte;
    
}
