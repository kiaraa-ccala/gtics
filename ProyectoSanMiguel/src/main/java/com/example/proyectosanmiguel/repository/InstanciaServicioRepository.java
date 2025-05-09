package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.InstanciaServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstanciaServicioRepository extends JpaRepository<InstanciaServicio, Integer> {
    @Query("SELECT i FROM InstanciaServicio i WHERE i.complejoDeportivo.idComplejoDeportivo = :idComplejoDeportivo")
    List<InstanciaServicio> findInstanciaServicioByComplejoDeportivo(@Param("idComplejoDeportivo") int idComplejoDeportivo);
}

