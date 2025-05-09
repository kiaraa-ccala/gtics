package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarifaRepository extends JpaRepository<Servicio, Integer> {
    Servicio findByNombre(String nombre);
}
