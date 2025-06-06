package com.example.proyectosanmiguel.dto;

import java.util.List;

public class ComparativaAsistenciaDto {
    private List<String> meses;
    private List<Double> porcentajesAsistencia;
    private Double cambioPercentual;

    public ComparativaAsistenciaDto() {}

    public ComparativaAsistenciaDto(List<String> meses, List<Double> porcentajesAsistencia, Double cambioPercentual) {
        this.meses = meses;
        this.porcentajesAsistencia = porcentajesAsistencia;
        this.cambioPercentual = cambioPercentual;
    }

    public List<String> getMeses() { return meses; }
    public void setMeses(List<String> meses) { this.meses = meses; }

    public List<Double> getPorcentajesAsistencia() { return porcentajesAsistencia; }
    public void setPorcentajesAsistencia(List<Double> porcentajesAsistencia) { this.porcentajesAsistencia = porcentajesAsistencia; }

    public Double getCambioPercentual() { return cambioPercentual; }
    public void setCambioPercentual(Double cambioPercentual) { this.cambioPercentual = cambioPercentual; }
}
