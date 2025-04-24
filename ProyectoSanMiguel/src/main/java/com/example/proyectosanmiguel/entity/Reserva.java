package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "Reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idReserva", nullable = false)
    private Integer idReserva;

    @ManyToOne
    @JoinColumn(name = "idUsuario")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "idInformacionPago")
    private InformacionPago informacionPago;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "horaInicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "horaFin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "estado", nullable = false)
    private Integer estado;

    @Column(name = "fechaHoraRegistro", nullable = false)
    private LocalDateTime fechaHoraRegistro;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "idServicio"),
            @JoinColumn(name = "idComplejoDeportivo"),
            @JoinColumn(name = "idInstanciaServicio")
    })
    private InstanciaServicio instanciaServicio;



}
