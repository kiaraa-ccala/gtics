package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.Mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Integer> {

    List<Mantenimiento> findByComplejoDeportivoIdComplejoDeportivo(Integer complejoId);
    // Aquí puedes agregar métodos personalizados si es necesario
}
