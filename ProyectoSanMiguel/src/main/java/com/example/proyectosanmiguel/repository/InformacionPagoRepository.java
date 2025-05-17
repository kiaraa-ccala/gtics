package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.InformacionPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InformacionPagoRepository extends JpaRepository<InformacionPago, Integer> {
}
