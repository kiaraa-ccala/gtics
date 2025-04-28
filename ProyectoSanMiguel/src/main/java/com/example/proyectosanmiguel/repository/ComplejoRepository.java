package com.example.proyectosanmiguel.repository;
import com.example.proyectosanmiguel.dto.ReporteServiciosDto;
import com.example.proyectosanmiguel.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplejoRepository extends JpaRepository<ComplejoDeportivo, Integer> {

    @Query(value = "SELECT cd.nombre AS nombreComplejoDeportivo, " +
            "s.nombre AS nombreServicio, " +
            "isv.capacidadMaxima AS capacidad, " +
            "isv.modoAcceso AS modoAcceso, " +
            "COUNT(r.idReserva) AS reservasTotales, " +
            "ROUND(COUNT(r.idReserva) / SUM(COUNT(r.idReserva)) OVER (PARTITION BY cd.idComplejoDeportivo) * 100, 2) AS porcentajeReservas " +
            "FROM InstanciaServicio isv " +
            "INNER JOIN Servicio s ON isv.idServicio = s.idServicio " +
            "INNER JOIN ComplejoDeportivo cd ON isv.idComplejoDeportivo = cd.idComplejoDeportivo " +
            "LEFT JOIN Reserva r ON isv.idInstanciaServicio = r.idInstanciaServicio " +
            "GROUP BY isv.idInstanciaServicio, cd.idComplejoDeportivo, cd.nombre, s.nombre, isv.capacidadMaxima, isv.modoAcceso",
            nativeQuery = true)
    List<ReporteServiciosDto> getReporteServiciosSuperAdmin();

}
