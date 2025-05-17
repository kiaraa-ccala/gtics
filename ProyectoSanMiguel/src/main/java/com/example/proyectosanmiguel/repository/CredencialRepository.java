package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.Credencial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface CredencialRepository extends JpaRepository<Credencial, Integer> {

    Credencial findByCorreo(String correo);

}
