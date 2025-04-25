package com.example.proyectosanmiguel.entity;

import java.io.Serializable;
import java.util.Objects;

public class InstanciaServicioId implements Serializable {

    private Integer idInstanciaServicio;
    private Integer idServicio;
    private Integer idComplejoDeportivo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InstanciaServicioId)) return false;
        InstanciaServicioId that = (InstanciaServicioId) o;
        return Objects.equals(idInstanciaServicio, that.idInstanciaServicio) &&
                Objects.equals(idServicio, that.idServicio) &&
                Objects.equals(idComplejoDeportivo, that.idComplejoDeportivo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idInstanciaServicio, idServicio, idComplejoDeportivo);
    }
}