package com.example.proyectosanmiguel.repository;
import com.example.proyectosanmiguel.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {




}
