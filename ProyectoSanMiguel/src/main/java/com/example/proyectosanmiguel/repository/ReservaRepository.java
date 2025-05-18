package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalTime;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    List<Reserva> findByUsuario_IdUsuario(Integer idUsuario);
    List<Reserva> findByInstanciaServicio_IdInstanciaServicioAndFecha(Integer idInstanciaServicio, LocalDate fecha);

    @Query("SELECT r.fecha FROM Reserva r WHERE r.instanciaServicio.idInstanciaServicio = :idInstancia GROUP BY r.fecha HAVING COUNT(r.idReserva) >= 15")
    List<LocalDate> findFechasLlenasByInstancia(@Param("idInstancia") Integer idInstancia);

    @Query("SELECT r FROM Reserva r WHERE r.instanciaServicio.idInstanciaServicio = :id AND r.fecha BETWEEN :inicio AND :fin")
    List<Reserva> findByInstanciaServicio_IdInstanciaServicioBetweenDates(@Param("id") Integer id, @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    @Query("SELECT COUNT(r) > 0 FROM Reserva r " +
            "WHERE r.instanciaServicio.idInstanciaServicio = :idInstancia " +
            "AND r.fecha = :fecha " +
            "AND r.estado IN (0, 1) " + // Pendiente o pagada
            "AND (" +
            "  (:horaInicio < r.horaFin AND :horaFin > r.horaInicio)" +
            ")")
    boolean existeCruceReserva(@Param("idInstancia") Integer idInstancia,
                               @Param("fecha") LocalDate fecha,
                               @Param("horaInicio") LocalTime horaInicio,
                               @Param("horaFin") LocalTime horaFin);

    @Query("SELECT r FROM Reserva r " +
            "WHERE r.usuario.idUsuario = :idUsuario " +
            "AND r.estado = 0 " +
            "AND r.informacionPago.estado = 'Pendiente'")
    List<Reserva> findReservasPendientesConPagoPendiente(@Param("idUsuario") Integer idUsuario);


}