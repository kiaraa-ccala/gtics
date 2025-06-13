package com.example.proyectosanmiguel.dto;

public class IngresosDetalleMesDto {
    private int anio;
    private int mes;
    private String nombreMes;
    private double ingresosTarjetaCredito;
    private double ingresosTransferencia;
    private double ingresosTotales;
    private long totalTransacciones;

    public IngresosDetalleMesDto(int anio, int mes, String nombreMes, double ingresosTarjetaCredito, double ingresosTransferencia, double ingresosTotales, long totalTransacciones) {
        this.anio = anio;
        this.mes = mes;
        this.nombreMes = nombreMes;
        this.ingresosTarjetaCredito = ingresosTarjetaCredito;
        this.ingresosTransferencia = ingresosTransferencia;
        this.ingresosTotales = ingresosTotales;
        this.totalTransacciones = totalTransacciones;
    }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }
    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }
    public String getNombreMes() { return nombreMes; }
    public void setNombreMes(String nombreMes) { this.nombreMes = nombreMes; }
    public double getIngresosTarjetaCredito() { return ingresosTarjetaCredito; }
    public void setIngresosTarjetaCredito(double ingresosTarjetaCredito) { this.ingresosTarjetaCredito = ingresosTarjetaCredito; }
    public double getIngresosTransferencia() { return ingresosTransferencia; }
    public void setIngresosTransferencia(double ingresosTransferencia) { this.ingresosTransferencia = ingresosTransferencia; }
    public double getIngresosTotales() { return ingresosTotales; }
    public void setIngresosTotales(double ingresosTotales) { this.ingresosTotales = ingresosTotales; }
    public long getTotalTransacciones() { return totalTransacciones; }
    public void setTotalTransacciones(long totalTransacciones) { this.totalTransacciones = totalTransacciones; }
}
