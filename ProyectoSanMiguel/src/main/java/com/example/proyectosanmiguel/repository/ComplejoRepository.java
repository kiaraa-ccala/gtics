package com.example.proyectosanmiguel.repository;
import com.example.proyectosanmiguel.dto.ComplejoAgendaAdminDto;
import com.example.proyectosanmiguel.dto.ReporteFinancieroDto;
import com.example.proyectosanmiguel.dto.ReporteHorarioDto;
import com.example.proyectosanmiguel.dto.ReporteServiciosDto;
import com.example.proyectosanmiguel.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplejoRepository extends JpaRepository<ComplejoDeportivo, Integer> {

    @Query(value = "SELECT cd.nombre AS nombreComplejoDeportivo, " +
            "s.nombre AS nombreServicio, " +
            "isv.capacidadMaxima AS capacidad, " +
            "isv.modoAcceso AS modoAcceso, " +
            "COUNT(r.idReserva) AS reservasTotales, " +
            "ROUND(COUNT(r.idReserva) / SUM(COUNT(r.idReserva)) OVER (PARTITION BY cd.idComplejoDeportivo) * 100, 2) AS porcentajeReservas " +
            "FROM instanciaservicio isv " +
            "INNER JOIN servicio s ON isv.idServicio = s.idServicio " +
            "INNER JOIN complejodeportivo cd ON isv.idComplejoDeportivo = cd.idComplejoDeportivo " +
            "LEFT JOIN reserva r ON isv.idInstanciaServicio = r.idInstanciaServicio " +
            "GROUP BY isv.idInstanciaServicio, cd.idComplejoDeportivo, cd.nombre, s.nombre, isv.capacidadMaxima, isv.modoAcceso",
            nativeQuery = true)
    List<ReporteServiciosDto> getReporteServiciosSuperAdmin();

    @Query(value = "SELECT cd.nombre AS nombreComplejoDeportivo, " +
            "s.nombre AS nombreServicio, " +
            "COALESCE(SUM(CASE WHEN ip.estado = 'Pagado' THEN ip.total ELSE 0 END), 0) AS ingresoTotal, " +
            "COALESCE(SUM(CASE WHEN ip.estado = 'Pagado' AND ip.tipo = 'Tarjeta' THEN ip.total ELSE 0 END), 0) AS ingresoTarjeta, " +
            "COALESCE(SUM(CASE WHEN ip.estado = 'Pagado' AND ip.tipo = 'Transferencia' THEN ip.total ELSE 0 END), 0) AS ingresoTransferencia, " +
            "ROUND(CASE WHEN SUM(CASE WHEN ip.estado = 'Pagado' AND ip.tipo = 'Transferencia' THEN ip.total ELSE 0 END) > 0 " +
            "THEN SUM(CASE WHEN ip.estado = 'Pagado' AND ip.tipo = 'Tarjeta' THEN ip.total ELSE 0 END) / SUM(CASE WHEN ip.estado = 'Pagado' AND ip.tipo = 'Transferencia' THEN ip.total ELSE 0 END) * 100 " +
            "ELSE 0 END, 2) AS porcentajeIngresoTarjetaSobreTransferencia " +
            "FROM instanciaservicio isv " +
            "INNER JOIN servicio s ON isv.idServicio = s.idServicio " +
            "INNER JOIN complejodeportivo cd ON isv.idComplejoDeportivo = cd.idComplejoDeportivo " +
            "LEFT JOIN reserva r ON isv.idInstanciaServicio = r.idInstanciaServicio " +
            "LEFT JOIN informacionpago ip ON r.idInformacionPago = ip.idInformacionPago " +
            "GROUP BY cd.idComplejoDeportivo, cd.nombre, s.idServicio, s.nombre, isv.idInstanciaServicio " +
            "ORDER BY ingresoTotal",
            nativeQuery = true)
    List<ReporteFinancieroDto> getReporteServiciosFinancierosSuperAdmin();

    @Query(value = "SELECT " +
            "CONCAT(u.nombre, ' ', u.apellido) AS nombreCoordinador, " +
            "cd.nombre AS nombreComplejo, " +
            "h.fecha AS fecha, " +
            "CASE " +
            "WHEN SUM(CASE WHEN v.resultado = 'Exitoso' THEN 1 ELSE 0 END) = 0 THEN 'No registrado' " +
            "WHEN MIN(CASE WHEN v.resultado = 'Exitoso' AND TIME(v.timeStampValidacion) < h.horaIngreso THEN v.timeStampValidacion ELSE NULL END) IS NOT NULL THEN 'A tiempo' " +
            "ELSE 'Tardanza' " +
            "END AS estadoFinal " +
            "FROM horario h " +
            "JOIN horariosemanal hs ON h.idHorarioSemanal = hs.idHorarioSemanal " +
            "JOIN complejodeportivo cd ON h.idComplejoDeportivo = cd.idComplejoDeportivo " +
            "JOIN usuario u ON hs.idCoordinador = u.idUsuario " +
            "LEFT JOIN validacion v ON v.idHorario = h.idHorario " +
            "GROUP BY h.idHorario",
            nativeQuery = true)
    List<ReporteHorarioDto> getReporteHorarios();

    ComplejoDeportivo findFirstBySector(Sector sector);

    @Query("SELECT cd.idComplejoDeportivo AS idComplejoDeportivo, " +
            "cd.nombre AS nombreComplejo, cd.direccion AS direccionComplejo, " +
            "s.nombre AS nombreSector, cd.numeroSoporte AS numeroSoporte, " +
            "cd.latitud AS latitud, cd.longitud AS longitud "+
            "FROM ComplejoDeportivo cd " +
            "JOIN cd.sector s " +
            "WHERE cd.idComplejoDeportivo = :id")
    ComplejoAgendaAdminDto obtenerComplejoDto(@Param("id") Integer id);

}
