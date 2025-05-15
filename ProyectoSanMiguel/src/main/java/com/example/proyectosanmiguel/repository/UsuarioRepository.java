package com.example.proyectosanmiguel.repository;
import com.example.proyectosanmiguel.dto.CoordinadorAgendaAdminDto;
import com.example.proyectosanmiguel.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = 'COORDINADOR'")
    List<Usuario> findAllCoordinadores();
    Usuario findFirstByRol_IdRol(int idRol);



    @Query("SELECT u.idUsuario AS idCoordinador, u.nombre AS nombreCoordinador, " +
            "u.apellido AS apellidoCoordinador, u.direccion AS direccionCoordinador, " +
            "u.telefono AS telefonoCoordinador, u.departamento AS departamentoCoordinador, " +
            "u.provincia AS provinciaCoordinador, u.distrito AS distritoCoordinador, " +
            "c.correo AS emailCoordinador " +
            "FROM Usuario u " +
            "JOIN Credencial c ON u.idUsuario = c.usuario.idUsuario " +
            "WHERE u.idUsuario = :id")
    CoordinadorAgendaAdminDto obtenerCoordinadorDto(@Param("id") Integer id);


}
