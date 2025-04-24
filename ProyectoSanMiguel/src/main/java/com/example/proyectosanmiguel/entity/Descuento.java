package com.example.proyectosanmiguel.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "Descuento")
public class Descuento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDescuento", nullable = false)
    private Integer idDescuento;

    @Column(name = "codigo", nullable = false, length = 45)
    private String codigo;

    @Column(name = "tipoDescuento", nullable = false, length = 45)
    private String tipoDescuento;

    @Column(name = "valor", nullable = false)
    private Double valor;

    @Column(name = "fechaInicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fechaFinal", nullable = false)
    private LocalDate fechaFinal;

    @ManyToOne
    @JoinColumn(name = "idServicio")
    private Servicio servicio;

}
