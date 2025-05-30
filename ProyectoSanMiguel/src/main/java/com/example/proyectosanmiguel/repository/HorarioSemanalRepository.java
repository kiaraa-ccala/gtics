package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.HorarioSemanal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HorarioSemanalRepository extends JpaRepository<HorarioSemanal, Integer> {
    Optional<HorarioSemanal> findByCoordinadorIdUsuarioAndFechaInicioAndFechaFin(
            Integer idCoordinador, LocalDate fechaInicio, LocalDate fechaFin
    );

    List<HorarioSemanal> findByIdCoordinador(Integer idCoordinador);
}
