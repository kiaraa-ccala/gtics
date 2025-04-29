package com.example.proyectosanmiguel.dto;

public interface ReporteFinancieroDto {

    String getNombreComplejoDeportivo();
    String getNombreServicio();
    Double getIngresoTotal();
    Double getIngresoTarjeta();
    Double getIngresoTransferencia();
    Double getPorcentajeIngresoTarjetaSobreTransferencia();
}
