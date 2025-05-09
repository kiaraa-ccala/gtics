package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.InstanciaServicio;
import com.example.proyectosanmiguel.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstanciaServicioRepository extends JpaRepository<InstanciaServicio, Integer> {

    List<Servicio> findInstanciaServicioByComplejoDeportivo_IdComplejoDeportivo(Integer id);

}
