-- Script para verificar el estado de las reservas
-- y crear una reserva de prueba para probar el cupón DEPORTE10

-- 1. Verificar todas las reservas existentes
SELECT 
    r.idReserva,
    r.fecha,
    r.horaInicio,
    r.horaFin,
    r.estado as estadoReserva,
    s.nombre as nombreServicio,
    s.idServicio,
    ip.estado as estadoPago,
    ip.total,
    u.nombre as nombreUsuario
FROM reserva r
JOIN instanciaservicio ins ON r.idInstanciaServicio = ins.idInstanciaServicio
JOIN servicio s ON ins.idServicio = s.idServicio
LEFT JOIN informacionpago ip ON r.idInformacionPago = ip.idInformacionPago
LEFT JOIN usuario u ON r.idUsuario = u.idUsuario
ORDER BY r.idReserva DESC;

-- 2. Verificar usuarios disponibles
SELECT idUsuario, nombre, apellido FROM usuario WHERE activo = 1 LIMIT 5;

-- 3. Verificar instancias de servicio para "Cancha de Grass"
SELECT 
    ins.idInstanciaServicio,
    s.nombre as nombreServicio,
    cd.nombre as nombreComplejo
FROM instanciaservicio ins
JOIN servicio s ON ins.idServicio = s.idServicio
JOIN complejodeportivo cd ON ins.idComplejoDeportivo = cd.idComplejoDeportivo
WHERE s.idServicio = 1;

-- 4. Verificar tarifas para el servicio
SELECT 
    t.idTarifa,
    t.diaSemana,
    t.horaInicio,
    t.horaFin,
    t.monto,
    s.nombre as nombreServicio
FROM tarifa t
JOIN servicio s ON t.idServicio = s.idServicio
WHERE s.idServicio = 1;

-- 5. CREAR RESERVA DE PRUEBA PARA PROBAR CUPÓN DEPORTE10
-- Crear información de pago primero
INSERT INTO informacionpago (
    total,
    estado,
    fechaCreacion
) VALUES (
    30.00,               -- Precio ejemplo para una hora de cancha
    'Pendiente',         -- Estado pendiente para poder aplicar cupón
    NOW()
);

-- Obtener el ID de la información de pago recién creada
SET @idInfoPago = LAST_INSERT_ID();

-- Crear la reserva con estado PENDIENTE (estado = 0)
INSERT INTO reserva (
    fecha, 
    horaInicio, 
    horaFin, 
    estado, 
    idUsuario, 
    idInstanciaServicio, 
    idInformacionPago
) VALUES (
    CURDATE(),           -- Fecha de hoy
    '10:00:00',         -- Hora inicio
    '11:00:00',         -- Hora fin
    0,                  -- Estado PENDIENTE
    1,                  -- Usar el primer usuario (cambiar si es necesario)
    1,                  -- Usar la primera instancia de servicio (cambiar si es necesario)
    @idInfoPago         -- Usar el ID de información de pago creado arriba
);

-- 6. Verificar que la reserva se creó correctamente con estado PENDIENTE
SELECT 
    r.idReserva,
    r.fecha,
    r.horaInicio,
    r.horaFin,
    r.estado as estadoReserva,
    CASE 
        WHEN r.estado = 0 THEN 'PENDIENTE'
        WHEN r.estado = 1 THEN 'CONFIRMADA'
        WHEN r.estado = 2 THEN 'CANCELADA'
        ELSE 'DESCONOCIDO'
    END as estadoTexto,
    s.nombre as nombreServicio,
    s.idServicio,
    u.nombre as nombreUsuario,
    ip.total as totalPago,
    ip.estado as estadoPago
FROM reserva r
JOIN instanciaservicio ins ON r.idInstanciaServicio = ins.idInstanciaServicio
JOIN servicio s ON ins.idServicio = s.idServicio
LEFT JOIN usuario u ON r.idUsuario = u.idUsuario
LEFT JOIN informacionpago ip ON r.idInformacionPago = ip.idInformacionPago
WHERE r.estado = 0  -- Solo reservas pendientes
ORDER BY r.idReserva DESC
LIMIT 5;

-- 7. Verificar que el cupón DEPORTE10 existe y está configurado correctamente
SELECT 
    d.idDescuento,
    d.codigo,
    d.tipoDescuento,
    d.valor,
    d.fechaInicio,
    d.fechaFinal,
    s.idServicio,
    s.nombre as nombreServicio,
    CASE 
        WHEN CURDATE() >= d.fechaInicio AND CURDATE() <= d.fechaFinal THEN 'VIGENTE'
        WHEN CURDATE() < d.fechaInicio THEN 'AÚN NO ACTIVO'
        WHEN CURDATE() > d.fechaFinal THEN 'EXPIRADO'
        ELSE 'ESTADO DESCONOCIDO'
    END as estadoCupon
FROM descuento d
JOIN servicio s ON d.idServicio = s.idServicio
WHERE d.codigo = 'DEPORTE10';

-- 8. INSTRUCCIONES PARA PROBAR EL CUPÓN:
-- 1. Ejecuta todo este script en MySQL Workbench
-- 2. Verifica que se creó una reserva con estado PENDIENTE
-- 3. Verifica que el cupón DEPORTE10 está VIGENTE
-- 4. Inicia sesión en la aplicación como vecino (usuario ID 1)
-- 5. Ve a /vecino/pagos
-- 6. Aplica el cupón DEPORTE10 a la reserva pendiente
-- 7. Deberías ver un descuento del 10% aplicado
