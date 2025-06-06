package com.example.proyectosanmiguel.dto;

import java.util.List;

public class AsistenciaSemanalDto {
    private List<String> dias;
    private List<Integer> asistencias;
    private List<Integer> tardanzas;
    private List<Integer> ausencias;

    public AsistenciaSemanalDto() {}

    public AsistenciaSemanalDto(List<String> dias, List<Integer> asistencias, List<Integer> tardanzas, List<Integer> ausencias) {
        this.dias = dias;
        this.asistencias = asistencias;
        this.tardanzas = tardanzas;
        this.ausencias = ausencias;
    }

    public List<String> getDias() { return dias; }
    public void setDias(List<String> dias) { this.dias = dias; }

    public List<Integer> getAsistencias() { return asistencias; }
    public void setAsistencias(List<Integer> asistencias) { this.asistencias = asistencias; }

    public List<Integer> getTardanzas() { return tardanzas; }
    public void setTardanzas(List<Integer> tardanzas) { this.tardanzas = tardanzas; }

    public List<Integer> getAusencias() { return ausencias; }
    public void setAusencias(List<Integer> ausencias) { this.ausencias = ausencias; }
}
