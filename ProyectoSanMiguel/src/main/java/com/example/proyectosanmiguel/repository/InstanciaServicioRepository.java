package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.InstanciaServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstanciaServicioRepository extends JpaRepository<InstanciaServicio, Integer> {
}
