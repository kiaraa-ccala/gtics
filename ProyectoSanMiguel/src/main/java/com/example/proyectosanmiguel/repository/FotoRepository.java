package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.Foto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FotoRepository extends JpaRepository<Foto, Integer> {
}
