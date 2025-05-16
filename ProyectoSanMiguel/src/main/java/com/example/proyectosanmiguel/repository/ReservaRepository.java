package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    List<Reserva> findByUsuario_IdUsuario(Integer idUsuario);
    List<Reserva> findByInstanciaServicio_IdInstanciaServicioAndFecha(Integer idInstanciaServicio, LocalDate fecha);

    @Query("SELECT r.fecha FROM Reserva r WHERE r.instanciaServicio.idInstanciaServicio = :idInstancia GROUP BY r.fecha HAVING COUNT(r.idReserva) >= 15")
    List<LocalDate> findFechasLlenasByInstancia(@Param("idInstancia") Integer idInstancia);

    @Query("SELECT r FROM Reserva r WHERE r.instanciaServicio.idInstanciaServicio = :id AND r.fecha BETWEEN :inicio AND :fin")
    List<Reserva> findByInstanciaServicio_IdInstanciaServicioBetweenDates(@Param("id") Integer id, @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);



}