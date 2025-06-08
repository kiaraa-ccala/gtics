package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.time.LocalDate;


public interface DescuentoRepository extends JpaRepository<Descuento, Integer> {
    Optional<Descuento> findByCodigoAndFechaInicioLessThanEqualAndFechaFinalGreaterThanEqual(String codigo, LocalDate fechaInicio, LocalDate fechaFinal);
    Optional<Descuento> findByCodigo(String codigo);
}
