-- =====================================================
-- CONSULTAS CORREGIDAS PARA ANÁLISIS DE RESERVAS E INGRESOS
-- =====================================================

-- 1. Calcular los ingresos mensuales para enero (mes 1)
SELECT 
    SUM(ip.total) as ingresosMensuales
FROM informacionpago ip
WHERE MONTH(ip.fecha) = 4 
  AND YEAR(ip.fecha) = YEAR(CURDATE())
  AND ip.estado = 'pagado';

-- =====================================================

-- 2. Calcular las reservas creadas y reservas totales en febrero (mes 02)
SELECT 
	COUNT(*) as reservasMensuales
FROM reserva r
WHERE MONTH(r.fechaHoraRegistro) = 2 
AND YEAR(r.fechaHoraRegistro) = YEAR(CURDATE());

-- =====================================================

-- 3. Calcular los ingresos totales para el mes actual y los 5 meses anteriores
SELECT 
    año,
    mes,
    CASE mes
        WHEN 1 THEN 'Enero'
        WHEN 2 THEN 'Febrero'
        WHEN 3 THEN 'Marzo'
        WHEN 4 THEN 'Abril'
        WHEN 5 THEN 'Mayo'
        WHEN 6 THEN 'Junio'
        WHEN 7 THEN 'Julio'
        WHEN 8 THEN 'Agosto'
        WHEN 9 THEN 'Septiembre'
        WHEN 10 THEN 'Octubre'
        WHEN 11 THEN 'Noviembre'
        WHEN 12 THEN 'Diciembre'
    END as nombre_mes,
    ingresos_tarjeta_credito,
    ingresos_transferencia,
    ingresos_totales,
    total_transacciones
FROM (
    SELECT 
        YEAR(ip.fecha) as año,
        MONTH(ip.fecha) as mes,
        SUM(CASE WHEN ip.tipo = 'Tarjeta' THEN ip.total ELSE 0 END) as ingresos_tarjeta_credito,
        SUM(CASE WHEN ip.tipo = 'Transferencia' THEN ip.total ELSE 0 END) as ingresos_transferencia,
        SUM(ip.total) as ingresos_totales,
        COUNT(*) as total_transacciones
    FROM informacionpago ip
    WHERE ip.fecha >= DATE_SUB(DATE_FORMAT(CURDATE(), '%Y-%m-01'), INTERVAL 5 MONTH)
      AND ip.fecha <= LAST_DAY(CURDATE())
      AND ip.estado = 'pagado'
    GROUP BY YEAR(ip.fecha), MONTH(ip.fecha)
) AS datos_mensuales
ORDER BY año DESC, mes DESC;


