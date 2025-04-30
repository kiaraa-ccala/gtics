package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {
    List<Comentario> findByReporteIdReporteOrderByFechaHoraAsc(Integer idReporte);
}
