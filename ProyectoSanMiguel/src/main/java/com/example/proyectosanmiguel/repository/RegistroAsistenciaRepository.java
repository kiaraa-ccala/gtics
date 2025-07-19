package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.RegistroAsistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroAsistenciaRepository extends JpaRepository<RegistroAsistencia, Integer> {    @Query(value = "SELECT * FROM registro_asistencia ra " +
           "WHERE ra.id_coordinador = :idCoordinador " +
           "AND ra.tipo_registro = :tipo " +
           "AND DATE(ra.fecha_hora) = :fecha " +
           "ORDER BY ra.fecha_hora DESC LIMIT 1", 
           nativeQuery = true)
    Optional<RegistroAsistencia> findUltimoRegistroPorTipoYFecha(@Param("idCoordinador") Integer idCoordinador,
                                                                 @Param("tipo") String tipo,
                                                                 @Param("fecha") LocalDate fecha);    @Query("SELECT r FROM RegistroAsistencia r " +
           "JOIN FETCH r.coordinador " +
           "JOIN FETCH r.complejo " +
           "WHERE r.coordinador.idUsuario = :idCoordinador " +
           "ORDER BY r.fechaHora DESC")
    List<RegistroAsistencia> findRegistrosPorCoordinador(@Param("idCoordinador") Integer idCoordinador);
    
    @Query(value = "SELECT COUNT(*) FROM registro_asistencia ra " +
           "WHERE ra.id_coordinador = :idCoordinador " +
           "AND ra.tipo_registro = :tipo " +
           "AND DATE(ra.fecha_hora) = :fecha", 
           nativeQuery = true)
    long countRegistrosPorTipoYFecha(@Param("idCoordinador") Integer idCoordinador,
                                     @Param("tipo") String tipo,
                                     @Param("fecha") LocalDate fecha);
    
    @Query(value = "SELECT * FROM registro_asistencia ra " +
           "WHERE ra.id_coordinador = :idCoordinador " +
           "AND ra.tipo_registro = :tipo " +
           "AND DATE(ra.fecha_hora) = :fecha " +
           "AND ra.id_horario = :idHorario " +
           "ORDER BY ra.fecha_hora DESC LIMIT 1", 
           nativeQuery = true)
    Optional<RegistroAsistencia> findUltimoRegistroPorTipoYFechaYHorario(@Param("idCoordinador") Integer idCoordinador,
                                                                        @Param("tipo") String tipo,
                                                                        @Param("fecha") LocalDate fecha,
                                                                        @Param("idHorario") Integer idHorario);
}