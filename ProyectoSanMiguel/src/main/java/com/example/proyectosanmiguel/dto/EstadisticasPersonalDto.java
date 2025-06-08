package com.example.proyectosanmiguel.dto;

public class EstadisticasPersonalDto {
    private Integer totalPersonal;
    private Integer horariosATiempo;
    private Integer horariosAusentes;
    private Double porcentajeATiempo;
    private Double porcentajeAusente;

    public EstadisticasPersonalDto() {}

    public EstadisticasPersonalDto(Integer totalPersonal, Integer horariosATiempo, 
                                 Integer horariosAusentes, Double porcentajeATiempo, 
                                 Double porcentajeAusente) {
        this.totalPersonal = totalPersonal;
        this.horariosATiempo = horariosATiempo;
        this.horariosAusentes = horariosAusentes;
        this.porcentajeATiempo = porcentajeATiempo;
        this.porcentajeAusente = porcentajeAusente;
    }    public Integer getTotalPersonal() { return totalPersonal; }
    public void setTotalPersonal(Integer totalPersonal) { this.totalPersonal = totalPersonal; }

    public Integer getHorariosATiempo() { return horariosATiempo; }
    public void setHorariosATiempo(Integer horariosATiempo) { this.horariosATiempo = horariosATiempo; }

    public Integer getHorariosAusentes() { return horariosAusentes; }
    public void setHorariosAusentes(Integer horariosAusentes) { this.horariosAusentes = horariosAusentes; }

    public Double getPorcentajeATiempo() { return porcentajeATiempo; }
    public void setPorcentajeATiempo(Double porcentajeATiempo) { this.porcentajeATiempo = porcentajeATiempo; }

    public Double getPorcentajeAusente() { return porcentajeAusente; }
    public void setPorcentajeAusente(Double porcentajeAusente) { this.porcentajeAusente = porcentajeAusente; }
}
