package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.time.LocalDate;
import java.util.List;


public interface DescuentoRepository extends JpaRepository<Descuento, Integer> {
    Optional<Descuento> findByCodigo(String codigo);
    
    @Query("SELECT d FROM Descuento d WHERE d.codigo = :codigo AND :fecha >= d.fechaInicio AND :fecha <= d.fechaFinal")
    Optional<Descuento> findValidDescuentoByCodigoAndFecha(@Param("codigo") String codigo, @Param("fecha") LocalDate fecha);
}
