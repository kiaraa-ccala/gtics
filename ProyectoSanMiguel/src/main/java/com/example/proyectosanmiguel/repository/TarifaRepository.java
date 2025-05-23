package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Integer> {

    @Query("SELECT t FROM Tarifa t WHERE t.servicio.idServicio = :idServicio " +
            "AND t.diaSemana = :diaSemana " +
            "AND t.horaInicio <= :horaInicio AND t.horaFin >= :horaFin")
    Optional<Tarifa> findTarifaAplicable(@Param("idServicio") Integer idServicio,
                                         @Param("diaSemana") String diaSemana,
                                         @Param("horaInicio") LocalTime horaInicio,
                                         @Param("horaFin") LocalTime horaFin);

    @Query("""
    SELECT t FROM Tarifa t 
    JOIN t.servicio s 
    JOIN InstanciaServicio i ON i.servicio.idServicio = s.idServicio
    WHERE i.complejoDeportivo.idComplejoDeportivo = :idComplejo
    """)
    List<Tarifa> findTarifasByIdComplejo(@Param("idComplejo") Integer idComplejo);

}
