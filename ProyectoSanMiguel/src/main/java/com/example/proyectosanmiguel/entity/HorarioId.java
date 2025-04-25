package com.example.proyectosanmiguel.entity;

import java.io.Serializable;
import java.util.Objects;

public class HorarioId implements Serializable {
    private Integer idHorario;
    private Integer idHorarioSemanal;
    private Integer idAdministrador;
    private Integer idCoordinador;
    private Integer idComplejoDeportivo;

    // equals() y hashCode() obligatorios para JPA
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HorarioId)) return false;
        HorarioId that = (HorarioId) o;
        return Objects.equals(idHorario, that.idHorario) &&
                Objects.equals(idHorarioSemanal, that.idHorarioSemanal) &&
                Objects.equals(idAdministrador, that.idAdministrador) &&
                Objects.equals(idCoordinador, that.idCoordinador) &&
                Objects.equals(idComplejoDeportivo, that.idComplejoDeportivo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idHorario, idHorarioSemanal, idAdministrador, idCoordinador, idComplejoDeportivo);
    }
}