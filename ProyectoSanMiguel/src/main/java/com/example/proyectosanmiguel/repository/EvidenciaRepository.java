package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.Comentario;
import com.example.proyectosanmiguel.entity.Evidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvidenciaRepository extends JpaRepository<Evidencia, Integer> {
}
