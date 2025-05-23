package com.example.proyectosanmiguel.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "reporte")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idReporte", nullable = false)
    private Integer idReporte;

    @Column(name = "tipoReporte", nullable = false, length = 45)
    private String tipoReporte;

    @Column(name = "fechaRecepcion", nullable = false)
    private LocalDate fechaRecepcion;

    @Column(name = "estado", nullable = false, length = 45)
    private String estado;

    @Column(name = "asunto", nullable = false, length = 100)
    private String asunto;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "respuesta", nullable = false, length = 500)
    private String respuesta;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name ="idFoto")
    private Foto foto;

    @ManyToOne
    @JoinColumn(name = "idReserva")
    private Reserva reserva;

    @ManyToOne
    @JoinColumn(name = "idHorario")
    private Horario horario;

}
