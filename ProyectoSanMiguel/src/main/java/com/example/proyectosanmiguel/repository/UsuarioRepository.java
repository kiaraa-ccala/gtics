package com.example.proyectosanmiguel.repository;
import com.example.proyectosanmiguel.dto.CoordinadorAgendaAdminDto;
import com.example.proyectosanmiguel.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Consultas originales del sistema
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = 'COORDINADOR'")
    List<Usuario> findAllCoordinadores();
    
    Usuario findFirstByRol_IdRol(int idRol);
    
    Optional<Usuario> findByCredencial_Correo(String correo);

    @Query("SELECT u.idUsuario AS idCoordinador, u.nombre AS nombreCoordinador, " +
            "u.apellido AS apellidoCoordinador, u.direccion AS direccionCoordinador, " +
            "u.telefono AS telefonoCoordinador, u.departamento AS departamentoCoordinador, " +
            "u.provincia AS provinciaCoordinador, u.distrito AS distritoCoordinador, " +
            "c.correo AS emailCoordinador " +
            "FROM Usuario u " +
            "JOIN Credencial c ON u.idUsuario = c.usuario.idUsuario " +
            "WHERE u.idUsuario = :id")
    CoordinadorAgendaAdminDto obtenerCoordinadorDto(@Param("id") Integer id);    // Consultas para estadísticas del dashboard personal (usando consultas nativas)
    @Query(value = "SELECT COUNT(u.idUsuario) FROM usuario u " +
           "JOIN rol r ON u.idRol = r.idRol " +
           "WHERE u.activo = 1 AND r.nombre != 'Superadministrador'", nativeQuery = true)
    Long countPersonalActivo();

    @Query(value = "SELECT COUNT(u.idUsuario) FROM usuario u " +
           "JOIN rol r ON u.idRol = r.idRol " +
           "WHERE u.activo = 1 AND r.nombre != 'Superadministrador'", nativeQuery = true)
    Long countNuevoPersonal();

    @Query(value = "SELECT COUNT(DISTINCT hs.idCoordinador) FROM horario h " +
           "JOIN horariosemanal hs ON h.idHorarioSemanal = hs.idHorarioSemanal " +
           "JOIN usuario u ON hs.idCoordinador = u.idUsuario " +
           "JOIN rol r ON u.idRol = r.idRol " +
           "WHERE h.fecha = :fecha AND u.activo = 1 " +
           "AND r.nombre != 'Superadministrador'", nativeQuery = true)
    Long countPersonalPresenteHoy(@Param("fecha") LocalDate fecha);

    @Query(value = "SELECT COUNT(DISTINCT hs.idCoordinador) FROM horario h " +
           "JOIN horariosemanal hs ON h.idHorarioSemanal = hs.idHorarioSemanal " +
           "JOIN usuario u ON hs.idCoordinador = u.idUsuario " +
           "JOIN rol r ON u.idRol = r.idRol " +
           "WHERE h.fecha = :fecha AND u.activo = 1 " +
           "AND r.nombre != 'Superadministrador'", nativeQuery = true)
    Long countPersonalConHorarioHoy(@Param("fecha") LocalDate fecha);

    @Query(value = "SELECT DAYOFWEEK(h.fecha) as dia, " +
           "COUNT(CASE WHEN v.resultado = 'VALIDO' THEN 1 END) as presente, " +
           "COUNT(CASE WHEN v.idValidacion IS NULL THEN 1 END) as ausente " +
           "FROM horario h " +
           "LEFT JOIN validacion v ON h.idHorario = v.idHorario " +
           "JOIN horariosemanal hs ON h.idHorarioSemanal = hs.idHorarioSemanal " +
           "JOIN usuario u ON hs.idCoordinador = u.idUsuario " +
           "JOIN rol r ON u.idRol = r.idRol " +
           "WHERE h.fecha >= :fechaInicio AND h.fecha <= :fechaFin " +
           "AND u.activo = 1 AND r.nombre != 'Superadministrador' " +
           "GROUP BY DAYOFWEEK(h.fecha) ORDER BY DAYOFWEEK(h.fecha)", nativeQuery = true)
    List<Object[]> getAsistenciaSemanal(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Query(value = "SELECT MONTH(h.fecha) as mes, YEAR(h.fecha) as año, " +
           "(COUNT(CASE WHEN v.resultado = 'VALIDO' THEN 1 END) * 100.0 / COUNT(h.idHorario)) as porcentaje " +
           "FROM horario h " +
           "LEFT JOIN validacion v ON h.idHorario = v.idHorario " +
           "JOIN horariosemanal hs ON h.idHorarioSemanal = hs.idHorarioSemanal " +
           "JOIN usuario u ON hs.idCoordinador = u.idUsuario " +
           "JOIN rol r ON u.idRol = r.idRol " +
           "WHERE h.fecha >= :fechaInicio AND u.activo = 1 " +
           "AND r.nombre != 'Superadministrador' " +
           "GROUP BY YEAR(h.fecha), MONTH(h.fecha) " +
           "ORDER BY YEAR(h.fecha), MONTH(h.fecha)", nativeQuery = true)
    List<Object[]> getPorcentajeAsistenciaMensual(@Param("fechaInicio") LocalDate fechaInicio);
}
