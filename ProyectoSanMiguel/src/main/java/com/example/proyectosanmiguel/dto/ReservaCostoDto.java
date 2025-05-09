package com.example.proyectosanmiguel.dto;
import java.time.LocalDateTime;

import com.example.proyectosanmiguel.entity.Reserva;

public class ReservaCostoDto {
    private Reserva reserva;
    private Double costo;
    private boolean puedeCancelar;

    public ReservaCostoDto(Reserva reserva, Double costo) {
        this.reserva = reserva;
        this.costo = costo;
        // Lógica para saber si puede cancelar
        LocalDateTime fechaHoraInicio = LocalDateTime.of(reserva.getFecha(), reserva.getHoraInicio());
        this.puedeCancelar = LocalDateTime.now().isBefore(fechaHoraInicio.minusHours(48));
    }

    public Reserva getReserva() {
        return reserva;
    }

    public Double getCosto() {
        return costo;
    }

    public boolean isPuedeCancelar() { return puedeCancelar; }
}
