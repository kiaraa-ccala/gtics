package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.Reporte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Integer> {
    Page<Reporte> findAll(Pageable pageable);
    List<Reporte> findTop7ByOrderByFechaRecepcionDesc();
    List<Reporte> findTop4ByOrderByFechaRecepcionDesc();
    long countByEstado(String estado);
}
