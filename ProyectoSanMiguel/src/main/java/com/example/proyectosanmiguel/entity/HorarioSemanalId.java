package com.example.proyectosanmiguel.entity;

import java.io.Serializable;
import java.util.Objects;

public class HorarioSemanalId implements Serializable {
    private Integer idHorarioSemanal;
    private Integer idAdministrador;
    private Integer idCoordinador;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HorarioSemanalId)) return false;
        HorarioSemanalId that = (HorarioSemanalId) o;
        return Objects.equals(idHorarioSemanal, that.idHorarioSemanal) &&
                Objects.equals(idAdministrador, that.idAdministrador) &&
                Objects.equals(idCoordinador, that.idCoordinador);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idHorarioSemanal, idAdministrador, idCoordinador);
    }
}