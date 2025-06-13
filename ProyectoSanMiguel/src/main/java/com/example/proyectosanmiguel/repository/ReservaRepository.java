package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.dto.ReporteReservasDto;
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
    List<Reserva> findReservasPendientesConPagoPendiente(@Param("idUsuario") Integer idUsuario);    List<Reserva> findByUsuario_IdUsuarioAndEstado(Integer idUsuario, Integer estado);

    // Metodo para encontrar reservas por estado y estado de información de pago
    List<Reserva> findByEstadoAndInformacionPago_Estado(Integer estado, String estadoPago);

    // Metodo para obtener reporte de reservas con filtros
    @Query(value = "SELECT " +
            "CAST(r.idReserva AS CHAR) as idReserva, " +
            "CONCAT(u.nombre, ' ', u.apellido) as nombreUsuario, " +
            "cd.nombre as nombreComplejo, " +
            "s.nombre as nombreServicio, " +
            "DATE_FORMAT(r.fecha, '%d/%m/%Y') as fechaReserva, " +
            "CASE " +
            "  WHEN ip.total IS NOT NULL THEN CONCAT('S/. ', FORMAT(ip.total, 2)) " +
            "  ELSE 'S/. 0.00' " +
            "END as montoTotal, " +
            "CASE " +
            "  WHEN r.estado = 0 THEN 'Pendiente' " +
            "  WHEN r.estado = 1 THEN 'Pagada' " +
            "  WHEN r.estado = 2 THEN 'Cancelada' " +
            "  ELSE 'Desconocido' " +
            "END as estadoReserva " +
            "FROM Reserva r " +
            "INNER JOIN Usuario u ON r.idUsuario = u.idUsuario " +
            "INNER JOIN InstanciaServicio isv ON r.idInstanciaServicio = isv.idInstanciaServicio " +
            "INNER JOIN Servicio s ON isv.idServicio = s.idServicio " +
            "INNER JOIN ComplejoDeportivo cd ON isv.idComplejoDeportivo = cd.idComplejoDeportivo " +
            "LEFT JOIN InformacionPago ip ON r.idInformacionPago = ip.idInformacionPago " +
            "WHERE (:fechaInicio IS NULL OR r.fecha >= :fechaInicio) " +
            "AND (:fechaFin IS NULL OR r.fecha <= :fechaFin) " +
            "AND (:idComplejo IS NULL OR cd.idComplejoDeportivo = :idComplejo) " +
            "AND (:idServicio IS NULL OR s.idServicio = :idServicio) " +
            "ORDER BY r.fecha DESC, r.idReserva DESC",
            nativeQuery = true)
    List<ReporteReservasDto> getReporteReservas(@Param("fechaInicio") LocalDate fechaInicio,
                                                @Param("fechaFin") LocalDate fechaFin,
                                                @Param("idComplejo") Integer idComplejo,
                                                @Param("idServicio") Integer idServicio);

}