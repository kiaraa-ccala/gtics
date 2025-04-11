package com.example.proyectosanmiguel.repository;
import com.example.proyectosanmiguel.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectorRepository extends JpaRepository<Sector, Integer> {
}