package com.example.proyectosanmiguel.repository;
import com.example.proyectosanmiguel.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplejoRepository extends JpaRepository<ComplejoDeportivo, Long> {
}
