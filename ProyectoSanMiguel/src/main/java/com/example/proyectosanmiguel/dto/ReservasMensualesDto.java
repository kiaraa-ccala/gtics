package com.example.proyectosanmiguel.dto;

public class ReservasMensualesDto {
    private int mes;
    private long reservasTotales;

    public ReservasMensualesDto(int mes, long reservasTotales) {
        this.mes = mes;
        this.reservasTotales = reservasTotales;
    }

    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }
    public long getReservasTotales() { return reservasTotales; }
    public void setReservasTotales(long reservasTotales) { this.reservasTotales = reservasTotales; }
}
