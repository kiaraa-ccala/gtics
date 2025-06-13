package com.example.proyectosanmiguel.dto;

public class IngresosMensualesDto {
    private int mes;
    private double ingresos;

    public IngresosMensualesDto(int mes, double ingresos) {
        this.mes = mes;
        this.ingresos = ingresos;
    }

    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }
    public double getIngresos() { return ingresos; }
    public void setIngresos(double ingresos) { this.ingresos = ingresos; }
}
