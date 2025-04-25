-- FULL REFACTORED SQL SCRIPT: GESTIONDEPORTIVA (Simplified for Spring Boot JPA)

-- Drop schema if exists
DROP SCHEMA IF EXISTS `GestionDeportiva`;

-- Create schema
CREATE SCHEMA IF NOT EXISTS `GestionDeportiva` DEFAULT CHARACTER SET utf8mb4;
USE `GestionDeportiva`;

-- Sector
CREATE TABLE `Sector` (
  `idSector` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45),
  PRIMARY KEY (`idSector`)
);

-- ComplejoDeportivo
CREATE TABLE `ComplejoDeportivo` (
  `idComplejoDeportivo` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45),
  `direccion` VARCHAR(45),
  `idSector` INT NOT NULL,
  `numeroSoporte` VARCHAR(45),
  `latitud` DECIMAL(10,8),
  `longitud` DECIMAL(11,8),
  PRIMARY KEY (`idComplejoDeportivo`),
  FOREIGN KEY (`idSector`) REFERENCES `Sector`(`idSector`)
);

-- Foto
CREATE TABLE `Foto` (
  `idFoto` INT NOT NULL AUTO_INCREMENT,
  `nombreFoto` VARCHAR(80),
  `foto` LONGBLOB,
  `urlFoto` VARCHAR(100),
  PRIMARY KEY (`idFoto`)
);

-- Tercerizado
CREATE TABLE `Tercerizado` (
  `idTercerizado` INT NOT NULL AUTO_INCREMENT,
  `ruc` VARCHAR(11),
  `direccionFiscal` VARCHAR(45),
  PRIMARY KEY (`idTercerizado`)
);

-- Rol
CREATE TABLE `Rol` (
  `idRol` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45),
  PRIMARY KEY (`idRol`)
);

-- Usuario
CREATE TABLE `Usuario` (
  `idUsuario` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45),
  `apellido` VARCHAR(45),
  `dni` VARCHAR(45),
  `direccion` VARCHAR(45),
  `distrito` VARCHAR(45),
  `provincia` VARCHAR(45),
  `departamento` VARCHAR(45),
  `telefono` VARCHAR(9),
  `idSector` INT,
  `idTercerizado` INT,
  `idFoto` INT NOT NULL,
  `idRol` INT NOT NULL,
  PRIMARY KEY (`idUsuario`),
  FOREIGN KEY (`idSector`) REFERENCES `Sector`(`idSector`),
  FOREIGN KEY (`idTercerizado`) REFERENCES `Tercerizado`(`idTercerizado`),
  FOREIGN KEY (`idFoto`) REFERENCES `Foto`(`idFoto`),
  FOREIGN KEY (`idRol`) REFERENCES `Rol`(`idRol`)
);

-- HorarioSemanal
CREATE TABLE `HorarioSemanal` (
  `idHorarioSemanal` INT NOT NULL AUTO_INCREMENT,
  `idAdministrador` INT NOT NULL,
  `idCoordinador` INT NOT NULL,
  `fechaInicio` DATE,
  `fechaFin` DATE,
  `fechaCreacion` DATE,
  PRIMARY KEY (`idHorarioSemanal`),
  FOREIGN KEY (`idAdministrador`) REFERENCES `Usuario`(`idUsuario`),
  FOREIGN KEY (`idCoordinador`) REFERENCES `Usuario`(`idUsuario`)
);

-- Horario
CREATE TABLE `Horario` (
  `idHorario` INT NOT NULL AUTO_INCREMENT,
  `idHorarioSemanal` INT NOT NULL,
  `idComplejoDeportivo` INT NOT NULL,
  `fecha` DATE,
  `horaIngreso` TIME,
  `horaSalida` TIME,
  PRIMARY KEY (`idHorario`),
  FOREIGN KEY (`idHorarioSemanal`) REFERENCES `HorarioSemanal`(`idHorarioSemanal`),
  FOREIGN KEY (`idComplejoDeportivo`) REFERENCES `ComplejoDeportivo`(`idComplejoDeportivo`)
);

-- Servicio
CREATE TABLE `Servicio` (
  `idServicio` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(60),
  PRIMARY KEY (`idServicio`)
);

-- InstanciaServicio
CREATE TABLE `InstanciaServicio` (
  `idInstanciaServicio` INT NOT NULL AUTO_INCREMENT,
  `idServicio` INT NOT NULL,
  `idComplejoDeportivo` INT NOT NULL,
  `nombre` VARCHAR(45),
  `capacidadMaxima` VARCHAR(45),
  `modoAcceso` VARCHAR(45),
  PRIMARY KEY (`idInstanciaServicio`),
  FOREIGN KEY (`idServicio`) REFERENCES `Servicio`(`idServicio`),
  FOREIGN KEY (`idComplejoDeportivo`) REFERENCES `ComplejoDeportivo`(`idComplejoDeportivo`)
);

-- InformacionPago
CREATE TABLE `InformacionPago` (
  `idInformacionPago` INT NOT NULL AUTO_INCREMENT,
  `fecha` DATE,
  `hora` TIME,
  `tipo` VARCHAR(45),
  `total` DECIMAL(10,2),
  `estado` VARCHAR(45),
  PRIMARY KEY (`idInformacionPago`)
);

-- Reserva
CREATE TABLE `Reserva` (
  `idReserva` INT NOT NULL AUTO_INCREMENT,
  `idUsuario` INT NOT NULL,
  `idInformacionPago` INT NOT NULL,
  `fecha` DATE,
  `horaInicio` TIME,
  `horaFin` TIME,
  `estado` TINYINT,
  `fechaHoraRegistro` DATETIME,
  `idInstanciaServicio` INT NOT NULL,
  PRIMARY KEY (`idReserva`),
  FOREIGN KEY (`idUsuario`) REFERENCES `Usuario`(`idUsuario`),
  FOREIGN KEY (`idInformacionPago`) REFERENCES `InformacionPago`(`idInformacionPago`),
  FOREIGN KEY (`idInstanciaServicio`) REFERENCES `InstanciaServicio`(`idInstanciaServicio`)
);

-- Reporte
CREATE TABLE `Reporte` (
  `idReporte` INT NOT NULL AUTO_INCREMENT,
  `tipoReporte` VARCHAR(45),
  `fechaRecepcion` DATE,
  `estado` VARCHAR(45),
  `asunto` VARCHAR(100),
  `descripcion` VARCHAR(500),
  `respuesta` VARCHAR(500),
  `idReserva` INT,
  `idHorario` INT NOT NULL,
  PRIMARY KEY (`idReporte`),
  FOREIGN KEY (`idReserva`) REFERENCES `Reserva`(`idReserva`),
  FOREIGN KEY (`idHorario`) REFERENCES `Horario`(`idHorario`)
);

-- Comentario
CREATE TABLE `Comentario` (
  `idComentario` INT NOT NULL AUTO_INCREMENT,
  `fechaHora` DATETIME,
  `contenido` VARCHAR(300),
  `idReporte` INT NOT NULL,
  PRIMARY KEY (`idComentario`),
  FOREIGN KEY (`idReporte`) REFERENCES `Reporte`(`idReporte`)
);

-- Credencial
CREATE TABLE `Credencial` (
  `idCredencial` INT NOT NULL AUTO_INCREMENT,
  `correo` VARCHAR(45),
  `password` VARCHAR(45),
  `idUsuario` INT NOT NULL,
  PRIMARY KEY (`idCredencial`),
  FOREIGN KEY (`idUsuario`) REFERENCES `Usuario`(`idUsuario`)
);

-- Descuento
CREATE TABLE `Descuento` (
  `idDescuento` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(45),
  `tipoDescuento` VARCHAR(45),
  `valor` DECIMAL(10,2),
  `fechaInicio` DATE,
  `fechaFinal` DATE,
  `idServicio` INT NOT NULL,
  PRIMARY KEY (`idDescuento`),
  FOREIGN KEY (`idServicio`) REFERENCES `Servicio`(`idServicio`)
);

-- Evidencia
CREATE TABLE `Evidencia` (
  `idEvidencia` INT NOT NULL AUTO_INCREMENT,
  `nombreArchivo` VARCHAR(100),
  `urlArchivo` VARCHAR(100),
  `archivo` LONGBLOB,
  `idReporte` INT NOT NULL,
  PRIMARY KEY (`idEvidencia`),
  FOREIGN KEY (`idReporte`) REFERENCES `Reporte`(`idReporte`)
);

-- Mantenimiento
CREATE TABLE `Mantenimiento` (
  `idMantenimiento` INT NOT NULL AUTO_INCREMENT,
  `idServicio` INT NOT NULL,
  `fechaInicio` DATE,
  `fechaFin` DATE,
  `horaInicio` TIME,
  `horaFin` TIME,
  PRIMARY KEY (`idMantenimiento`),
  FOREIGN KEY (`idServicio`) REFERENCES `Servicio`(`idServicio`)
);

-- Tarifa
CREATE TABLE `Tarifa` (
  `idTarifa` INT NOT NULL AUTO_INCREMENT,
  `idServicio` INT NOT NULL,
  `tipoServicio` VARCHAR(45),
  `diaSemana` VARCHAR(45),
  `horaInicio` TIME,
  `horaFin` TIME,
  `monto` DECIMAL(10,2),
  PRIMARY KEY (`idTarifa`),
  FOREIGN KEY (`idServicio`) REFERENCES `Servicio`(`idServicio`)
);

-- Validacion
CREATE TABLE `Validacion` (
  `idValidacion` INT NOT NULL AUTO_INCREMENT,
  `timeStampValidacion` DATETIME,
  `latitudCoordinador` DECIMAL(10,8),
  `longitudCoordinador` DECIMAL(11,8),
  `distanciaError` DECIMAL(10,2),
  `resultado` VARCHAR(45),
  `idHorario` INT NOT NULL,
  PRIMARY KEY (`idValidacion`),
  FOREIGN KEY (`idHorario`) REFERENCES `Horario`(`idHorario`)
);
