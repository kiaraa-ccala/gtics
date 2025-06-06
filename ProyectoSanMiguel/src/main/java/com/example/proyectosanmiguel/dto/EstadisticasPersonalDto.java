package com.example.proyectosanmiguel.dto;

public class EstadisticasPersonalDto {
    private Integer totalPersonal;
    private Integer personalATiempo;
    private Integer nuevoPersonal;
    private Integer personalAusente;
    private Double porcentajeATiempo;
    private Double porcentajeNuevo;
    private Double porcentajeAusente;

    public EstadisticasPersonalDto() {}

    public EstadisticasPersonalDto(Integer totalPersonal, Integer personalATiempo, Integer nuevoPersonal, 
                                 Integer personalAusente, Double porcentajeATiempo, Double porcentajeNuevo, 
                                 Double porcentajeAusente) {
        this.totalPersonal = totalPersonal;
        this.personalATiempo = personalATiempo;
        this.nuevoPersonal = nuevoPersonal;
        this.personalAusente = personalAusente;
        this.porcentajeATiempo = porcentajeATiempo;
        this.porcentajeNuevo = porcentajeNuevo;
        this.porcentajeAusente = porcentajeAusente;
    }

    public Integer getTotalPersonal() { return totalPersonal; }
    public void setTotalPersonal(Integer totalPersonal) { this.totalPersonal = totalPersonal; }

    public Integer getPersonalATiempo() { return personalATiempo; }
    public void setPersonalATiempo(Integer personalATiempo) { this.personalATiempo = personalATiempo; }

    public Integer getNuevoPersonal() { return nuevoPersonal; }
    public void setNuevoPersonal(Integer nuevoPersonal) { this.nuevoPersonal = nuevoPersonal; }

    public Integer getPersonalAusente() { return personalAusente; }
    public void setPersonalAusente(Integer personalAusente) { this.personalAusente = personalAusente; }

    public Double getPorcentajeATiempo() { return porcentajeATiempo; }
    public void setPorcentajeATiempo(Double porcentajeATiempo) { this.porcentajeATiempo = porcentajeATiempo; }

    public Double getPorcentajeNuevo() { return porcentajeNuevo; }
    public void setPorcentajeNuevo(Double porcentajeNuevo) { this.porcentajeNuevo = porcentajeNuevo; }

    public Double getPorcentajeAusente() { return porcentajeAusente; }
    public void setPorcentajeAusente(Double porcentajeAusente) { this.porcentajeAusente = porcentajeAusente; }
}
