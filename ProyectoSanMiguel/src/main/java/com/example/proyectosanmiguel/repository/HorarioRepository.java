package com.example.proyectosanmiguel.repository;

import com.example.proyectosanmiguel.dto.EventoHorarioDto;
import com.example.proyectosanmiguel.dto.HorarioTurnoDto;
import com.example.proyectosanmiguel.entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Integer> {
    // Aquí puedes agregar métodos personalizados si es necesario
   /**
    @Query("""
    SELECT h
    FROM Horario h
    JOIN FETCH h.horarioSemanal hs
    JOIN FETCH h.complejoDeportivo c
    WHERE (:idComplejo IS NULL OR c.idComplejoDeportivo = :idComplejo)
      AND (:idCoordinador IS NULL OR hs.coordinador.idUsuario = :idCoordinador)
""")
    List<Horario> obtenerHorariosPorFiltro(
            @Param("idComplejo") Integer idComplejo,
            @Param("idCoordinador") Integer idCoordinador
    );


    **/

   @Query("""
        SELECT h FROM Horario h
        WHERE h.horarioSemanal.coordinador.idUsuario = :idCoordinador
    """)
   List<Horario> findByCoordinador(@Param("idCoordinador") Integer idCoordinador);

    @Query("""
        SELECT h FROM Horario h
        WHERE h.complejoDeportivo.idComplejoDeportivo = :idComplejo
    """)
    List<Horario> findByComplejo(@Param("idComplejo") Integer idComplejo);

    @Query("""
    SELECT 
        h.idHorario AS idHorario,
        h.fecha AS fecha,
        h.horaIngreso AS horaIngreso,
        h.horaSalida AS horaSalida,
        c.nombre AS nombreComplejo,
        u.nombre AS nombreCoordinador
    FROM Horario h
    JOIN h.complejoDeportivo c
    JOIN h.horarioSemanal hs
    JOIN hs.coordinador u
    WHERE (:idComplejo IS NULL OR c.idComplejoDeportivo = :idComplejo)
      AND (:idCoordinador IS NULL OR u.idUsuario = :idCoordinador)
""")
    List<EventoHorarioDto> listarHorariosPorFiltro(
            @Param("idComplejo") Integer idComplejo,
            @Param("idCoordinador") Integer idCoordinador
    );

    @Query("""
SELECT 
    FUNCTION('DAYNAME', h.fecha) as diaSemana,
    h.idHorario AS id,
    h.idHorarioSemanal as idHorarioSemanal,
    h.horaIngreso as horaInicio,
    h.horaSalida as horaFin,
    c.nombre as nombreComplejo
FROM Horario h
JOIN h.complejoDeportivo c
WHERE h.idHorarioSemanal = :idHorarioSemanal
""")
    List<HorarioTurnoDto> obtenerTurnosPorHorarioSemanal(@Param("idHorarioSemanal") Integer idHorarioSemanal);

    @Query("""
  SELECT h FROM Horario h
  JOIN h.horarioSemanal hs
  WHERE hs.idCoordinador = :idCoordinador
    AND h.fecha = :fecha
    AND h.idComplejoDeportivo = :idComplejo
    AND (:horaInicio < h.horaSalida AND :horaFin > h.horaIngreso)
""")
    List<Horario> buscarSolapamientos(@Param("idCoordinador") int idCoordinador,
                                      @Param("fecha") LocalDate fecha,
                                      @Param("idComplejo") int idComplejo,
                                      @Param("horaInicio") LocalTime horaInicio,
                                      @Param("horaFin") LocalTime horaFin);


    List<Horario> findAllByIdHorarioIn(List<Integer> ids);

    @Query("""
  SELECT COUNT(h) > 0
  FROM Horario h
  JOIN h.horarioSemanal hs
  WHERE hs.idCoordinador = :idCoordinador
    AND h.fecha = :fecha
    AND (:horaInicio < h.horaSalida AND :horaFin > h.horaIngreso)
    AND h.idHorario NOT IN :idsAExcluir
""")
    boolean existeSolapamiento(@Param("idCoordinador") int idCoordinador,
                               @Param("fecha") LocalDate fecha,
                               @Param("horaInicio") LocalTime horaInicio,
                               @Param("horaFin") LocalTime horaFin,
                               @Param("idsAExcluir") List<Integer> idsAExcluir);
    //usamos ids a excluir porque si te pones a pensar en el gudar horarios tambien habran eliminados y luego en la parctica no afectaran.








}
