INSERT INTO Sector (idSector, nombre) VALUES
(1, 'Zona A'),
(2, 'Zona B'),
(3, 'Zona C');

-- Complejos deportivos
INSERT INTO ComplejoDeportivo (idComplejoDeportivo, nombre, direccion, idSector, numeroSoporte, latitud, longitud) VALUES
(1, 'Complejo Central', 'Av Universitaria 1818', 1, '999111', -12.0464, -77.0428),
(2, 'Complejo Norte', 'Calle Norte 456', 2, '999222', -11.9825, -77.0018),
(3, 'Complejo Sur', 'Calle Sur 789', 3, '999333', -12.1806, -76.9993);

select * from ComplejoDeportivo;

-- Tercerizado (proveedor)
INSERT INTO Tercerizado (idTercerizado, ruc, direccionFiscal) VALUES
(1, '12345678901', 'Av. Proveedores 321');

select * from tercerizado;


INSERT INTO Foto (idFoto, nombreFoto, urlFoto) VALUES
(1, 'ana.jpg', 'http://fotos.com/ana.jpg'),
(2, 'bruno.jpg', 'http://fotos.com/bruno.jpg'),
(3, 'carla.jpg', 'http://fotos.com/carla.jpg'),
(4, 'diego.jpg', 'http://fotos.com/diego.jpg');

select * from foto;

-- Usuarios (revisar idSector si puede ser null)!!!!!!!!!!!!!!!!!!!!!!
INSERT INTO Usuario (idUsuario, nombre, apellido, dni, direccion, distrito, provincia, departamento, idSector, idTercerizado, idFoto) VALUES
(1, 'Tony', 'Flores',null, null, null, null, null, null, null, 1),
(2, 'Kiara', 'Ccala', null, null, null, null, null, null, null, 2),
(3, 'Daniel', 'Vargas', '34567890', 'Calle Parque San Martin 241', 'Pueblo Libre', 'Lima', 'Lima', 1, 1, 3),
(4, 'Mathias', 'Tirado', '45678901', 'Av. La Mar 400', 'San Miguel', 'Lima', 'Lima', null, null, 4);


select * from usuario;


-- Roles
INSERT INTO Roles (idRol, nombre) VALUES
(1, 'superadmin'),
(2, 'admin'),
(3, 'coordinador'),
(4, 'usuario');

select * from roles;

-- Rol por usuario
INSERT INTO RolUsuario (idRolUsuario, idUsuario, idRol) VALUES
(1, 1, 1), -- Tony - superadmin
(2, 2, 2), -- Kiara - admin
(3, 3, 3), -- Daniel - coordinador
(4, 4, 4); -- Mathias - vecino


INSERT INTO Credencial (idCredencial, correo, password, idUsuario) VALUES
(1, 'tony@mail.com', 'tony', 1),
(2, 'kiara@mail.com', 'kiara', 2),
(3, 'daniel.vargas@gmail.com', 'daniel', 3),
(4, 'mathias.tirado@hotmail.com', 'matisaurio', 4);

select * from credencial;

SELECT 
  CONCAT(u.nombre, ' ', u.apellido) AS usuario,
  r.nombre AS rol,
  c.correo,
  c.password
FROM Usuario u
JOIN Credencial c ON u.idUsuario = c.idUsuario
JOIN RolUsuario ru ON u.idUsuario = ru.idUsuario
JOIN Roles r ON ru.idRol = r.idRol;

SELECT 
  s.nombre AS sector,
  cd.nombre AS complejo_deportivo,
  cd.direccion,
  cd.numeroSoporte,
  cd.latitud,
  cd.longitud
FROM ComplejoDeportivo cd
JOIN Sector s ON cd.idSector = s.idSector
ORDER BY s.nombre, cd.nombre;


INSERT INTO Servicio (idServicio, nombre) VALUES
(1, 'Loza'),
(2, 'Grass'),
(3, 'Gimnasio');

select * from instanciaservicio;

-- Instancias para Complejo Central (id = 1)
INSERT INTO InstanciaServicio (idInstanciaServicio, idServicio, idComplejoDeportivo, nombre, capacidadMaxima, modoAcceso) VALUES
(1, 1, 1, 'Cancha Loza A', null, 'libre'),
(2, 1, 1, 'Cancha Loza B', null, 'libre');

-- Instancias para Complejo Norte (id = 2)
INSERT INTO InstanciaServicio (idInstanciaServicio, idServicio, idComplejoDeportivo, nombre, capacidadMaxima, modoAcceso) VALUES
(3, 1, 2, 'Cancha Grass A', null, 'libre'),
(4, 2, 2, 'Cancha Grass B', null, 'libre');

-- Instancias para Complejo Sur (id = 3)
INSERT INTO InstanciaServicio (idInstanciaServicio, idServicio, idComplejoDeportivo, nombre, capacidadMaxima, modoAcceso) VALUES
(5, 3, 3, 'Guimnasio San Miguel 1', '50', 'libre');


INSERT INTO InformacionPago (idInformacionPago, fecha, hora, tipo, total, estado) VALUES
(1, '2025-04-10', '10:00:00', 'tarjeta', 50.00, 'pagado'),
(2, '2025-04-10', '11:00:00', 'efectivo', 30.00, 'pagado'),
(3, '2025-04-11', '08:00:00', 'tarjeta', 60.00, 'pagado'),
(4, '2025-04-11', '09:00:00', 'tarjeta', 45.00, 'pendiente');
select * from informacionpago;


SELECT idServicio, idComplejoDeportivo, idInstanciaServicio FROM InstanciaServicio;

INSERT INTO Reserva (
  idReserva, idUsuario, idInformacionPago, fecha, horaInicio, horaFin,
  estado, fechaHoraRegistro, idServicio, idComplejoDeportivo, idInstanciaServicio
) VALUES
-- Mathias  reserva Cancha Loza B pero está pendiente
(4, 4, 4, '2025-04-15', '16:00:00', '17:00:00', 0, '2025-04-11 09:20:00', 1, 1, 2);


SELECT 
  r.idReserva,
  CONCAT(u.nombre, ' ', u.apellido) AS usuario,
  s.nombre AS servicio,
  iserv.nombre AS instancia_servicio,
  cd.nombre AS complejo_deportivo,
  r.fecha AS fecha_reserva,
  r.horaInicio,
  r.horaFin,
  ip.tipo AS tipo_pago,
  ip.total AS monto_pago,
  CASE r.estado
    WHEN 1 THEN 'Confirmada'
    WHEN 0 THEN 'Pendiente'
    ELSE 'Desconocido'
  END AS estado_reserva,
  r.fechaHoraRegistro AS registro_reserva
FROM Reserva r
JOIN Usuario u ON r.idUsuario = u.idUsuario
JOIN InformacionPago ip ON r.idInformacionPago = ip.idInformacionPago
JOIN InstanciaServicio iserv ON r.idServicio = iserv.idServicio 
  AND r.idComplejoDeportivo = iserv.idComplejoDeportivo
  AND r.idInstanciaServicio = iserv.idInstanciaServicio
JOIN Servicio s ON iserv.idServicio = s.idServicio
JOIN ComplejoDeportivo cd ON iserv.idComplejoDeportivo = cd.idComplejoDeportivo
ORDER BY r.fecha DESC, r.horaInicio ASC;
