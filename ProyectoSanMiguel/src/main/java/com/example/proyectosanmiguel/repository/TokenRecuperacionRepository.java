package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRecuperacionRepository  extends JpaRepository<TokenRecuperacion, Integer> {

    Optional<TokenRecuperacion> findByToken(String token);

    boolean existsByToken(String token);
}
