package com.example.proyectosanmiguel.repository;
import com.example.proyectosanmiguel.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = 'COORDINADOR'")
    List<Usuario> findAllCoordinadores();




}
