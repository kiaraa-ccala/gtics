package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Integer> {
    Servicio findByNombre(String nombre);
}
