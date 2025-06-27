-- ----------------------------------------------------------------------------------------------------------
--                               1° Creacion de tablas de la base de datos 
-- ----------------------------------------------------------------------------------------------------------

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema gestiondeportiva
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `gestiondeportiva` ;

-- -----------------------------------------------------
-- Schema gestiondeportiva
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `gestiondeportiva` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `gestiondeportiva` ;

-- -----------------------------------------------------
-- Table `gestiondeportiva`.`foto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`foto` (
  `idFoto` INT NOT NULL AUTO_INCREMENT,
  `nombreFoto` VARCHAR(80) NULL DEFAULT NULL,
  `foto` LONGBLOB NULL DEFAULT NULL,
  `urlFoto` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`idFoto`))
ENGINE = InnoDB
AUTO_INCREMENT = 6
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`sector`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`sector` (
  `idSector` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`idSector`))
ENGINE = InnoDB
AUTO_INCREMENT = 6
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`tercerizado`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`tercerizado` (
  `idTercerizado` INT NOT NULL AUTO_INCREMENT,
  `ruc` VARCHAR(11) NULL DEFAULT NULL,
  `direccionFiscal` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`idTercerizado`))
ENGINE = InnoDB
AUTO_INCREMENT = 4
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`rol`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`rol` (
  `idRol` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`idRol`))
ENGINE = InnoDB
AUTO_INCREMENT = 5
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`usuario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`usuario` (
  `idUsuario` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NULL DEFAULT NULL,
  `apellido` VARCHAR(45) NULL DEFAULT NULL,
  `dni` VARCHAR(45) NULL DEFAULT NULL,
  `direccion` VARCHAR(45) NULL DEFAULT NULL,
  `distrito` VARCHAR(45) NULL DEFAULT NULL,
  `provincia` VARCHAR(45) NULL DEFAULT NULL,
  `departamento` VARCHAR(45) NULL DEFAULT NULL,
  `telefono` VARCHAR(9) NULL DEFAULT NULL,
  `idSector` INT NULL DEFAULT NULL,
  `idTercerizado` INT NULL DEFAULT NULL,
  `idFoto` INT NULL DEFAULT NULL,
  `idRol` INT NOT NULL,
  `activo` TINYINT NULL DEFAULT NULL,
  PRIMARY KEY (`idUsuario`),
  INDEX `idSector` (`idSector` ASC) VISIBLE,
  INDEX `idTercerizado` (`idTercerizado` ASC) VISIBLE,
  INDEX `idFoto` (`idFoto` ASC) VISIBLE,
  INDEX `idRol` (`idRol` ASC) VISIBLE,
  CONSTRAINT `usuario_ibfk_1`
    FOREIGN KEY (`idSector`)
    REFERENCES `gestiondeportiva`.`sector` (`idSector`),
  CONSTRAINT `usuario_ibfk_2`
    FOREIGN KEY (`idTercerizado`)
    REFERENCES `gestiondeportiva`.`tercerizado` (`idTercerizado`),
  CONSTRAINT `usuario_ibfk_3`
    FOREIGN KEY (`idFoto`)
    REFERENCES `gestiondeportiva`.`foto` (`idFoto`),
  CONSTRAINT `usuario_ibfk_4`
    FOREIGN KEY (`idRol`)
    REFERENCES `gestiondeportiva`.`rol` (`idRol`))
ENGINE = InnoDB
AUTO_INCREMENT = 34
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`informacionpago`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`informacionpago` (
  `idInformacionPago` INT NOT NULL AUTO_INCREMENT,
  `fecha` DATE NULL DEFAULT NULL,
  `hora` TIME NULL DEFAULT NULL,
  `tipo` VARCHAR(45) NULL DEFAULT NULL,
  `total` DECIMAL(10,2) NULL DEFAULT NULL,
  `estado` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`idInformacionPago`))
ENGINE = InnoDB
AUTO_INCREMENT = 22
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`servicio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`servicio` (
  `idServicio` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(60) NULL DEFAULT NULL,
  PRIMARY KEY (`idServicio`))
ENGINE = InnoDB
AUTO_INCREMENT = 6
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`complejodeportivo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`complejodeportivo` (
  `idComplejoDeportivo` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NULL DEFAULT NULL,
  `direccion` VARCHAR(45) NULL DEFAULT NULL,
  `idSector` INT NOT NULL,
  `numeroSoporte` VARCHAR(45) NULL DEFAULT NULL,
  `latitud` DECIMAL(10,0) NULL DEFAULT NULL,
  `longitud` DECIMAL(10,0) NULL DEFAULT NULL,
  PRIMARY KEY (`idComplejoDeportivo`),
  INDEX `idSector` (`idSector` ASC) VISIBLE,
  CONSTRAINT `complejodeportivo_ibfk_1`
    FOREIGN KEY (`idSector`)
    REFERENCES `gestiondeportiva`.`sector` (`idSector`))
ENGINE = InnoDB
AUTO_INCREMENT = 11
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`instanciaservicio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`instanciaservicio` (
  `idInstanciaServicio` INT NOT NULL AUTO_INCREMENT,
  `idServicio` INT NOT NULL,
  `idComplejoDeportivo` INT NOT NULL,
  `nombre` VARCHAR(45) NULL DEFAULT NULL,
  `capacidadMaxima` VARCHAR(45) NULL DEFAULT NULL,
  `modoAcceso` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`idInstanciaServicio`),
  INDEX `idServicio` (`idServicio` ASC) VISIBLE,
  INDEX `idComplejoDeportivo` (`idComplejoDeportivo` ASC) VISIBLE,
  CONSTRAINT `instanciaservicio_ibfk_1`
    FOREIGN KEY (`idServicio`)
    REFERENCES `gestiondeportiva`.`servicio` (`idServicio`),
  CONSTRAINT `instanciaservicio_ibfk_2`
    FOREIGN KEY (`idComplejoDeportivo`)
    REFERENCES `gestiondeportiva`.`complejodeportivo` (`idComplejoDeportivo`))
ENGINE = InnoDB
AUTO_INCREMENT = 29
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`reserva`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`reserva` (
  `idReserva` INT NOT NULL AUTO_INCREMENT,
  `idUsuario` INT NOT NULL,
  `idInformacionPago` INT NOT NULL,
  `fecha` DATETIME NULL DEFAULT NULL,
  `horaInicio` TIME NULL DEFAULT NULL,
  `horaFin` TIME NULL DEFAULT NULL,
  `estado` TINYINT NULL DEFAULT NULL,
  `fechaHoraRegistro` DATETIME NULL DEFAULT NULL,
  `idInstanciaServicio` INT NOT NULL,
  PRIMARY KEY (`idReserva`),
  INDEX `idUsuario` (`idUsuario` ASC) VISIBLE,
  INDEX `idInformacionPago` (`idInformacionPago` ASC) VISIBLE,
  INDEX `idInstanciaServicio` (`idInstanciaServicio` ASC) VISIBLE,
  CONSTRAINT `reserva_ibfk_1`
    FOREIGN KEY (`idUsuario`)
    REFERENCES `gestiondeportiva`.`usuario` (`idUsuario`),
  CONSTRAINT `reserva_ibfk_2`
    FOREIGN KEY (`idInformacionPago`)
    REFERENCES `gestiondeportiva`.`informacionpago` (`idInformacionPago`),
  CONSTRAINT `reserva_ibfk_3`
    FOREIGN KEY (`idInstanciaServicio`)
    REFERENCES `gestiondeportiva`.`instanciaservicio` (`idInstanciaServicio`))
ENGINE = InnoDB
AUTO_INCREMENT = 22
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`horariosemanal`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`horariosemanal` (
  `idHorarioSemanal` INT NOT NULL AUTO_INCREMENT,
  `idAdministrador` INT NOT NULL,
  `idCoordinador` INT NOT NULL,
  `fechaInicio` DATE NULL DEFAULT NULL,
  `fechaFin` DATE NULL DEFAULT NULL,
  `fechaCreacion` DATE NULL DEFAULT NULL,
  PRIMARY KEY (`idHorarioSemanal`),
  INDEX `idAdministrador` (`idAdministrador` ASC) VISIBLE,
  INDEX `idCoordinador` (`idCoordinador` ASC) VISIBLE,
  CONSTRAINT `horariosemanal_ibfk_1`
    FOREIGN KEY (`idAdministrador`)
    REFERENCES `gestiondeportiva`.`usuario` (`idUsuario`),
  CONSTRAINT `horariosemanal_ibfk_2`
    FOREIGN KEY (`idCoordinador`)
    REFERENCES `gestiondeportiva`.`usuario` (`idUsuario`))
ENGINE = InnoDB
AUTO_INCREMENT = 101
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`horario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`horario` (
  `idHorario` INT NOT NULL AUTO_INCREMENT,
  `idHorarioSemanal` INT NOT NULL,
  `idComplejoDeportivo` INT NOT NULL,
  `fecha` DATE NULL DEFAULT NULL,
  `horaIngreso` TIME NULL DEFAULT NULL,
  `horaSalida` TIME NULL DEFAULT NULL,
  PRIMARY KEY (`idHorario`),
  INDEX `idHorarioSemanal` (`idHorarioSemanal` ASC) VISIBLE,
  INDEX `idComplejoDeportivo` (`idComplejoDeportivo` ASC) VISIBLE,
  CONSTRAINT `horario_ibfk_1`
    FOREIGN KEY (`idHorarioSemanal`)
    REFERENCES `gestiondeportiva`.`horariosemanal` (`idHorarioSemanal`),
  CONSTRAINT `horario_ibfk_2`
    FOREIGN KEY (`idComplejoDeportivo`)
    REFERENCES `gestiondeportiva`.`complejodeportivo` (`idComplejoDeportivo`))
ENGINE = InnoDB
AUTO_INCREMENT = 701
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`reporte`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`reporte` (
  `idReporte` INT NOT NULL AUTO_INCREMENT,
  `tipoReporte` VARCHAR(45) NULL DEFAULT NULL,
  `fechaRecepcion` DATE NULL DEFAULT NULL,
  `estado` VARCHAR(45) NULL DEFAULT NULL,
  `asunto` VARCHAR(100) NULL DEFAULT NULL,
  `descripcion` VARCHAR(500) NULL DEFAULT NULL,
  `respuesta` VARCHAR(500) NULL DEFAULT NULL,
  `idReserva` INT NULL DEFAULT NULL,
  `idHorario` INT NOT NULL,
  `idFoto` INT NULL DEFAULT NULL,
  PRIMARY KEY (`idReporte`),
  INDEX `idReserva` (`idReserva` ASC) VISIBLE,
  INDEX `idHorario` (`idHorario` ASC) VISIBLE,
  INDEX `fk_reporte_foto1_idx` (`idFoto` ASC) VISIBLE,
  CONSTRAINT `fk_reporte_foto1`
    FOREIGN KEY (`idFoto`)
    REFERENCES `gestiondeportiva`.`foto` (`idFoto`),
  CONSTRAINT `reporte_ibfk_1`
    FOREIGN KEY (`idReserva`)
    REFERENCES `gestiondeportiva`.`reserva` (`idReserva`),
  CONSTRAINT `reporte_ibfk_2`
    FOREIGN KEY (`idHorario`)
    REFERENCES `gestiondeportiva`.`horario` (`idHorario`))
ENGINE = InnoDB
AUTO_INCREMENT = 16
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`comentario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`comentario` (
  `idComentario` INT NOT NULL AUTO_INCREMENT,
  `fechaHora` DATETIME NULL DEFAULT NULL,
  `contenido` VARCHAR(300) NULL DEFAULT NULL,
  `idReporte` INT NOT NULL,
  PRIMARY KEY (`idComentario`),
  INDEX `idReporte` (`idReporte` ASC) VISIBLE,
  CONSTRAINT `comentario_ibfk_1`
    FOREIGN KEY (`idReporte`)
    REFERENCES `gestiondeportiva`.`reporte` (`idReporte`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`credencial`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`credencial` (
  `idCredencial` INT NOT NULL AUTO_INCREMENT,
  `correo` VARCHAR(45) NULL DEFAULT NULL,
  `password` VARCHAR(60) NULL DEFAULT NULL,
  `idUsuario` INT NOT NULL,
  PRIMARY KEY (`idCredencial`),
  INDEX `idUsuario` (`idUsuario` ASC) VISIBLE,
  CONSTRAINT `credencial_ibfk_1`
    FOREIGN KEY (`idUsuario`)
    REFERENCES `gestiondeportiva`.`usuario` (`idUsuario`))
ENGINE = InnoDB
AUTO_INCREMENT = 34
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`descuento`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`descuento` (
  `idDescuento` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(45) NULL DEFAULT NULL,
  `tipoDescuento` VARCHAR(45) NULL DEFAULT NULL,
  `valor` DECIMAL(10,2) NULL DEFAULT NULL,
  `fechaInicio` DATE NULL DEFAULT NULL,
  `fechaFinal` DATE NULL DEFAULT NULL,
  `idServicio` INT NOT NULL,
  PRIMARY KEY (`idDescuento`),
  INDEX `idServicio` (`idServicio` ASC) VISIBLE,
  CONSTRAINT `descuento_ibfk_1`
    FOREIGN KEY (`idServicio`)
    REFERENCES `gestiondeportiva`.`servicio` (`idServicio`))
ENGINE = InnoDB
AUTO_INCREMENT = 6
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`evidencia`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`evidencia` (
  `idEvidencia` INT NOT NULL AUTO_INCREMENT,
  `nombreArchivo` VARCHAR(100) NULL DEFAULT NULL,
  `urlArchivo` VARCHAR(100) NULL DEFAULT NULL,
  `archivo` LONGBLOB NULL DEFAULT NULL,
  `idComentario` INT NULL DEFAULT NULL,
  PRIMARY KEY (`idEvidencia`),
  INDEX `fk_evidencia_comentario1_idx` (`idComentario` ASC) VISIBLE,
  CONSTRAINT `fk_evidencia_comentario1`
    FOREIGN KEY (`idComentario`)
    REFERENCES `gestiondeportiva`.`comentario` (`idComentario`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`mantenimiento`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`mantenimiento` (
  `idMantenimiento` INT NOT NULL AUTO_INCREMENT,
  `fechaInicio` DATE NULL DEFAULT NULL,
  `fechaFin` DATE NULL DEFAULT NULL,
  `horaInicio` TIME NULL DEFAULT NULL,
  `horaFin` TIME NULL DEFAULT NULL,
  `idComplejoDeportivo` INT NOT NULL,
  PRIMARY KEY (`idMantenimiento`),
  INDEX `fk_mantenimiento_complejodeportivo1_idx` (`idComplejoDeportivo` ASC) VISIBLE,
  CONSTRAINT `fk_mantenimiento_complejodeportivo1`
    FOREIGN KEY (`idComplejoDeportivo`)
    REFERENCES `gestiondeportiva`.`complejodeportivo` (`idComplejoDeportivo`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `gestiondeportiva`.`tarifa`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`tarifa` (
  `idTarifa` INT NOT NULL AUTO_INCREMENT,
  `idServicio` INT NOT NULL,
  `tipoServicio` VARCHAR(45) NULL DEFAULT NULL,
  `diaSemana` VARCHAR(45) NULL DEFAULT NULL,
  `horaInicio` TIME NULL DEFAULT NULL,
  `horaFin` TIME NULL DEFAULT NULL,
  `monto` DECIMAL(10,2) NULL DEFAULT NULL,
  PRIMARY KEY (`idTarifa`),
  INDEX `idServicio` (`idServicio` ASC) VISIBLE,
  CONSTRAINT `tarifa_ibfk_1`
    FOREIGN KEY (`idServicio`)
    REFERENCES `gestiondeportiva`.`servicio` (`idServicio`))
ENGINE = InnoDB
AUTO_INCREMENT = 71
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`validacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`validacion` (
  `idValidacion` INT NOT NULL AUTO_INCREMENT,
  `timeStampValidacion` DATETIME NULL DEFAULT NULL,
  `latitudCoordinador` DECIMAL(10,8) NULL DEFAULT NULL,
  `longitudCoordinador` DECIMAL(11,8) NULL DEFAULT NULL,
  `distanciaError` DECIMAL(10,2) NULL DEFAULT NULL,
  `resultado` VARCHAR(45) NULL DEFAULT NULL,
  `idHorario` INT NOT NULL,
  PRIMARY KEY (`idValidacion`),
  INDEX `idHorario` (`idHorario` ASC) VISIBLE,
  CONSTRAINT `validacion_ibfk_1`
    FOREIGN KEY (`idHorario`)
    REFERENCES `gestiondeportiva`.`horario` (`idHorario`))
ENGINE = InnoDB
AUTO_INCREMENT = 13
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `gestiondeportiva`.`tokenRecuperacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gestiondeportiva`.`tokenRecuperacion` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `token` VARCHAR(255) NULL,
  `idUsuario` INT NOT NULL,
  `expiracion` DATETIME NULL,
  `usado` TINYINT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idtokenPassword_UNIQUE` (`id` ASC) VISIBLE,
  INDEX `fk_tokenRecuperacion_usuario1_idx` (`idUsuario` ASC) VISIBLE,
  CONSTRAINT `fk_tokenRecuperacion_usuario1`
    FOREIGN KEY (`idUsuario`)
    REFERENCES `gestiondeportiva`.`usuario` (`idUsuario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- ----------------------------------------------------------------------------------------------------------
--                                2° Creacion de registros en la base de datos
-- ----------------------------------------------------------------------------------------------------------

USE gestiondeportiva;

-- Insert sectores
INSERT INTO sector (idSector, nombre) VALUES
(1, 'Costanera'),
(2, 'La Paz'),
(3, 'Pando'),
(4, 'Maranga'),
(5, 'La Arboleda'),
(6, 'Otro distrito');

INSERT INTO complejodeportivo (idComplejoDeportivo, nombre, direccion, idSector, numeroSoporte, latitud, longitud) VALUES
(1, 'Polideportivo Maranga', 'Av. La Marina 2200', 4, '999999999', -1234567, -7654321),
(2, 'Parque Juan Pablo II', 'Av. Costanera 450', 1, '999999999', -1234568, -7654322),
(3, 'Complejo Deportivo Pando', 'Av. Universitaria 1800', 3, '999999999', -1234569, -7654323),
(4, 'Parque Media Luna', 'Calle Media Luna 150', 2, '999999999', -1234570, -7654324),
(5, 'Complejo Deportivo Arboleda', 'Av. Brígida Silva 600', 5, '999999999', -1234571, -7654325),
(6, 'Cancha Deportiva Costanera', 'Av. Costanera 600', 1, '999999999', -1234572, -7654326),
(7, 'Gimnasio Municipal', 'Jr. Federico Blume 150', 2, '999999999', -1234573, -7654327),
(8, 'Polideportivo La Paz', 'Jr. La Paz 400', 2, '999999999', -1234574, -7654328),
(9, 'Campo Deportivo Pando Norte', 'Calle Pando Norte 300', 3, '999999999', -1234575, -7654329),
(10, 'Skatepark San Miguel', 'Av. Bertolotto 1200', 1, '999999999', -1234576, -7654330);

INSERT INTO servicio (idServicio, nombre) VALUES
(1, 'Cancha de Grass'),
(2, 'Cancha de Losa'),
(3, 'Gimnasio'),
(4, 'Piscina'),
(5, 'Pista de Atletismo');


INSERT INTO instanciaservicio (idInstanciaServicio, idServicio, idComplejoDeportivo, nombre, capacidadMaxima, modoAcceso) VALUES
(1, 3, 1, 'Gimnasio Principal', '30', 'Libre'),
(2, 4, 1, 'Piscina Principal', '30', 'Libre'),
(3, 2, 1, 'Cancha de Losa A', '20', 'Libre'),
(4, 2, 1, 'Cancha de Losa B', '20', 'Libre'),
(5, 5, 2, 'Pista de Atletismo Principal', '30', 'Libre'),
(6, 1, 2, 'Cancha de Grass A', '20', 'Libre'),
(7, 1, 2, 'Cancha de Grass B', '20', 'Libre'),
(8, 1, 3, 'Cancha de Grass A', '20', 'Libre'),
(9, 1, 3, 'Cancha de Grass B', '20', 'Libre'),
(10, 2, 3, 'Cancha de Losa A', '20', 'Libre'),
(11, 2, 3, 'Cancha de Losa B', '20', 'Libre'),
(12, 2, 4, 'Cancha de Losa A', '20', 'Libre'),
(13, 2, 4, 'Cancha de Losa B', '20', 'Libre'),
(14, 5, 4, 'Pista de Atletismo Principal', '30', 'Libre'),
(15, 1, 5, 'Cancha de Grass A', '20', 'Libre'),
(16, 1, 5, 'Cancha de Grass B', '20', 'Libre'),
(17, 1, 6, 'Cancha de Grass A', '20', 'Libre'),
(18, 1, 6, 'Cancha de Grass B', '20', 'Libre'),
(19, 3, 7, 'Gimnasio Principal', '30', 'Libre'),
(20, 3, 8, 'Gimnasio Principal', '30', 'Libre'),
(21, 4, 8, 'Piscina Principal', '30', 'Libre'),
(22, 2, 8, 'Cancha de Losa A', '20', 'Libre'),
(23, 2, 8, 'Cancha de Losa B', '20', 'Libre'),
(24, 1, 9, 'Cancha de Grass A', '20', 'Libre'),
(25, 1, 9, 'Cancha de Grass B', '20', 'Libre'),
(26, 2, 9, 'Cancha de Losa A', '20', 'Libre'),
(27, 2, 9, 'Cancha de Losa B', '20', 'Libre'),
(28, 5, 10, 'Pista de Atletismo Principal', '30', 'Libre');


INSERT INTO rol (idRol, nombre) VALUES
(1, 'Superadministrador'),
(2, 'Administrador'),
(3, 'Coordinador'),
(4, 'Vecino');

INSERT INTO tercerizado (idTercerizado, ruc, direccionFiscal) VALUES
(1, '20123456789', 'Av. La Marina 1200, San Miguel'),
(2, '20198765432', 'Av. Universitaria 300, San Miguel'),
(3, '20567891234', 'Calle Mantaro 250, San Miguel');

INSERT INTO foto (idFoto, nombreFoto, urlFoto) VALUES
(1, 'Foto Perfil Usuario 1', 'https://tse4.mm.bing.net/th/id/OIP.cgNMVEvuDEun1YTljSQU-QAAAA?rs=1&pid=ImgDetMain'),
(2, 'Foto Perfil Usuario 2', 'https://tse4.mm.bing.net/th/id/OIP.cgNMVEvuDEun1YTljSQU-QAAAA?rs=1&pid=ImgDetMain'),
(3, 'Foto Perfil Usuario 3', 'https://tse4.mm.bing.net/th/id/OIP.cgNMVEvuDEun1YTljSQU-QAAAA?rs=1&pid=ImgDetMain'),
(4, 'Foto Perfil Usuario 4', 'https://tse4.mm.bing.net/th/id/OIP.cgNMVEvuDEun1YTljSQU-QAAAA?rs=1&pid=ImgDetMain'),
(5, 'Foto Perfil Usuario 5', 'https://tse4.mm.bing.net/th/id/OIP.cgNMVEvuDEun1YTljSQU-QAAAA?rs=1&pid=ImgDetMain');

-- Usuarios Principales por asi decirlo--
INSERT INTO usuario (idUsuario, nombre, apellido, dni, direccion, distrito, provincia, departamento, idSector, idTercerizado, idFoto, idRol) VALUES
(1, 'Tony', 'Flores', NULL, NULL, NULL, NULL, NULL, 3, NULL, 1, 1),
(2, 'Kiara', 'Ccala', NULL, NULL, NULL, NULL, NULL, 5, NULL, 2, 2),
(3, 'Daniel', 'Vargas', '74211799', 'Parque San Martin 241', 'Pueblo Libre', 'Lima', 'Lima', 2, 3, 3, 3),
(4, 'Mathias', 'Tirado', '44332211', 'Calle La Paz 101', 'San Miguel', 'Lima', 'Lima', 1, NULL, 4, 4);

-- insert vecinos adicionales para mas data--
INSERT INTO usuario (idUsuario, nombre, apellido, dni, direccion, distrito, provincia, departamento, idSector, idTercerizado, idFoto, idRol) VALUES
(5, 'Luciana', 'Salazar', '40000005', 'Av. Los Próceres 456', 'Pueblo Libre', 'Lima', 'Lima', 4, NULL, 5, 4),
(6, 'Jorge', 'Ramírez', '40000006', 'Jr. Amazonas 850', 'Jesús María', 'Lima', 'Lima', 2, NULL, 5, 4),
(7, 'Paula', 'Pérez', '40000007', 'Calle Bolívar 120', 'Magdalena del Mar', 'Lima', 'Lima', 5, NULL, 5, 4),
(8, 'Santiago', 'Cáceres', '40000008', 'Av. Sucre 1400', 'Pueblo Libre', 'Lima', 'Lima', 3, NULL, 5, 4),
(9, 'Valeria', 'Lopez', '40000009', 'Av. Salaverry 320', 'Jesús María', 'Lima', 'Lima', 1, NULL, 5, 4),
(10, 'Adrián', 'Quispe', '40000010', 'Jr. Tarapacá 760', 'Breña', 'Lima', 'Lima', 2, NULL, 5, 4),
(11, 'Mariana', 'Guzmán', '40000011', 'Av. Venezuela 500', 'Breña', 'Lima', 'Lima', 5, NULL, 5, 4),
(12, 'Diego', 'Ortega', '40000012', 'Av. Brasil 2200', 'Jesús María', 'Lima', 'Lima', 1, NULL, 5, 4),
(13, 'Natalia', 'Fernández', '40000013', 'Av. Cuba 350', 'Jesús María', 'Lima', 'Lima', 3, NULL, 5, 4),
(14, 'Sebastián', 'Morales', '40000014', 'Calle Ayacucho 200', 'Magdalena del Mar', 'Lima', 'Lima', 5, NULL, 5, 4),
(15, 'Gabriela', 'Campos', '40000015', 'Av. Brasil 1400', 'Breña', 'Lima', 'Lima', 1, NULL, 5, 4),
(16, 'Mateo', 'Valverde', '40000016', 'Jr. Coronel Zegarra 300', 'Lince', 'Lima', 'Lima', 4, NULL, 5, 4),
(17, 'Camila', 'Chávez', '40000017', 'Av. República de Panamá 560', 'La Victoria', 'Lima', 'Lima', 2, NULL, 5, 4),
(18, 'Andrés', 'Delgado', '40000018', 'Av. Canadá 1340', 'La Victoria', 'Lima', 'Lima', 1, NULL, 5, 4),
(19, 'Isabella', 'Mendoza', '40000019', 'Calle Los Halcones 520', 'Magdalena del Mar', 'Lima', 'Lima', 4, NULL, 5, 4),
(20, 'Samuel', 'Vásquez', '40000020', 'Av. Parque de las Leyendas 234', 'San Miguel', 'Lima', 'Lima', 5, NULL, 5, 4),
(21, 'Ana', 'López', '40000021', 'Av. Húsares de Junín 410', 'Pueblo Libre', 'Lima', 'Lima', 3, NULL, 5, 4),
(22, 'Marcos', 'Torres', '40000022', 'Jr. Junín 790', 'Breña', 'Lima', 'Lima', 2, NULL, 5, 4),
(23, 'Daniela', 'Sánchez', '40000023', 'Calle Conde de la Vega 120', 'Lince', 'Lima', 'Lima', 5, NULL, 5, 4),
(24, 'Ricardo', 'Paredes', '40000024', 'Av. Javier Prado Oeste 1300', 'San Isidro', 'Lima', 'Lima', 4, NULL, 5, 4);


-- Por si Faltan coordinadores--
INSERT INTO usuario (idUsuario, nombre, apellido, dni, direccion, distrito, provincia, departamento, idSector, idTercerizado, idFoto, idRol)
VALUES (25, 'Julieta', 'Ramos', '40000025', 'Av. Riva Agüero 800', 'Pueblo Libre', 'Lima', 'Lima', 2, 1, 5, 3);

INSERT INTO usuario (idUsuario, nombre, apellido, dni, direccion, distrito, provincia, departamento, idSector, idTercerizado, idFoto, idRol)
VALUES (26, 'Cristian', 'Mejía', '40000026', 'Jr. Trujillo 240', 'Breña', 'Lima', 'Lima', 4, 2, 5, 3);

INSERT INTO usuario (idUsuario, nombre, apellido, dni, direccion, distrito, provincia, departamento, idSector, idTercerizado, idFoto, idRol) VALUES
(27, 'Valeria', 'Gómez', '40000027', 'Av. Los Álamos 450', 'San Miguel', 'Lima', 'Lima', 1, 1, 5, 3),
(28, 'Sebastián', 'Torres', '40000028', 'Jr. Arequipa 123', 'Pueblo Libre', 'Lima', 'Lima', 2, 1, 5, 3),
(29, 'Luciana', 'Medina', '40000029', 'Av. Brasil 890', 'Jesús María', 'Lima', 'Lima', 3, 1, 5, 3),
(30, 'Jorge', 'Salazar', '40000030', 'Calle Libertad 320', 'Magdalena del Mar', 'Lima', 'Lima', 4, 1, 5, 3),
(31, 'Camila', 'Rojas', '40000031', 'Av. Faucett 720', 'San Miguel', 'Lima', 'Lima', 5, 1, 5, 3),
(32, 'Alejandro', 'Vargas', '40000032', 'Jr. Amazonas 400', 'Breña', 'Lima', 'Lima', 2, 1, 5, 3),
(33, 'Daniela', 'Castillo', '40000033', 'Av. La Marina 2300', 'San Miguel', 'Lima', 'Lima', 1, 1, 5, 3);

INSERT INTO credencial (correo, password, idUsuario) VALUES
('tony.flores@gmail.com', '$2a$10$DiJX5WJut04XVwXDT6YRjODcfXepPhZ9tPUCsfYRVKio0lY6aG5Qi', 1), #Password uncrypted = Tonylee1 | Rol = Superadministrador
('kiara.ccala@gmail.com', '$2a$10$ApVyPIIV5Zi8RLJyLGnXhO0ZAbzMMviCUu1pUbvLXelUklnwDQtBa', 2),  #Password uncrypted = asd123 | Rol = Administrador
('daniel.vargas@gmail.com', '$2a$10$ApVyPIIV5Zi8RLJyLGnXhO0ZAbzMMviCUu1pUbvLXelUklnwDQtBa', 3), #Password uncrypted = asd123 | Rol = Coordinador
('mathias.tirado@gmail.com', '$2a$10$ApVyPIIV5Zi8RLJyLGnXhO0ZAbzMMviCUu1pUbvLXelUklnwDQtBa', 4), #Password uncrypted = asd123 | Rol = Vecino
('luciana.salazar@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 5), #Password uncrypted = Tonylee1 | Rol = Vecino
('jorge.ramirez@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 6), 
('paula.perez@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 7),
('santiago.caceres@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 8), #Password uncrypted = Tonylee1 | Rol = Vecino
('valeria.lopez@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 9),
('adrian.quispe@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 10),
('mariana.guzman@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 11),
('diego.ortega@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 12),
('natalia.fernandez@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 13),
('sebastian.morales@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 14),
('gabriela.campos@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 15),
('mateo.valverde@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 16),
('camila.chavez@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 17),
('andres.delgado@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 18),
('isabella.mendoza@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 19),
('samuel.vasquez@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 20),
('ana.lopez@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 21),
('marcos.torres@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 22),
('daniela.sanchez@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 23),
('ricardo.paredes@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 24),
('julieta.ramos@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 25),
('cristian.mejia@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 26),
('valeria.gomez@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 27),
('sebastian.torres@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 28),
('luciana.medina@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 29),
('jorge.salazar@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 30),
('camila.rojas@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 31),
('alejandro.vargas@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 32),
('daniela.castillo@gmail.com', '$2a$10$Vd52riJwl4I8TjQTc2SDP.4GYDTkwcppYqdWFy4W90dG2ltnlx2Ca', 33);

Update usuario Set Activo = 1 where idUsuario >= 0;  

-- Cancha de Grass (idServicio = 1)
INSERT INTO tarifa (idTarifa, idServicio, tipoServicio, diaSemana, horaInicio, horaFin, monto) VALUES
(1, 1, 'Uso libre', 'Lunes', '08:00:00', '14:00:00', 20),
(2, 1, 'Uso libre', 'Lunes', '14:00:00', '20:00:00', 30),
(3, 1, 'Uso libre', 'Martes', '08:00:00', '14:00:00', 20),
(4, 1, 'Uso libre', 'Martes', '14:00:00', '20:00:00', 30),
(5, 1, 'Uso libre', 'Miércoles', '08:00:00', '14:00:00', 20),
(6, 1, 'Uso libre', 'Miércoles', '14:00:00', '20:00:00', 30),
(7, 1, 'Uso libre', 'Jueves', '08:00:00', '14:00:00', 20),
(8, 1, 'Uso libre', 'Jueves', '14:00:00', '20:00:00', 30),
(9, 1, 'Uso libre', 'Viernes', '08:00:00', '14:00:00', 20),
(10, 1, 'Uso libre', 'Viernes', '14:00:00', '20:00:00', 30),
(11, 1, 'Uso libre', 'Sábado', '08:00:00', '14:00:00', 20),
(12, 1, 'Uso libre', 'Sábado', '14:00:00', '20:00:00', 30),
(13, 1, 'Uso libre', 'Domingo', '08:00:00', '14:00:00', 20),
(14, 1, 'Uso libre', 'Domingo', '14:00:00', '20:00:00', 30);

-- Cancha de Losa (idServicio = 2)
INSERT INTO tarifa (idTarifa, idServicio, tipoServicio, diaSemana, horaInicio, horaFin, monto) VALUES
(15, 2, 'Uso libre', 'Lunes', '08:00:00', '14:00:00', 15),
(16, 2, 'Uso libre', 'Lunes', '14:00:00', '20:00:00', 25),
(17, 2, 'Uso libre', 'Martes', '08:00:00', '14:00:00', 15),
(18, 2, 'Uso libre', 'Martes', '14:00:00', '20:00:00', 25),
(19, 2, 'Uso libre', 'Miércoles', '08:00:00', '14:00:00', 15),
(20, 2, 'Uso libre', 'Miércoles', '14:00:00', '20:00:00', 25),
(21, 2, 'Uso libre', 'Jueves', '08:00:00', '14:00:00', 15),
(22, 2, 'Uso libre', 'Jueves', '14:00:00', '20:00:00', 25),
(23, 2, 'Uso libre', 'Viernes', '08:00:00', '14:00:00', 15),
(24, 2, 'Uso libre', 'Viernes', '14:00:00', '20:00:00', 25),
(25, 2, 'Uso libre', 'Sábado', '08:00:00', '14:00:00', 15),
(26, 2, 'Uso libre', 'Sábado', '14:00:00', '20:00:00', 25),
(27, 2, 'Uso libre', 'Domingo', '08:00:00', '14:00:00', 15),
(28, 2, 'Uso libre', 'Domingo', '14:00:00', '20:00:00', 25);

-- Gimnasio (idServicio = 3)
INSERT INTO tarifa (idTarifa, idServicio, tipoServicio, diaSemana, horaInicio, horaFin, monto) VALUES
(29, 3, 'Uso libre', 'Lunes', '08:00:00', '14:00:00', 10),
(30, 3, 'Uso libre', 'Lunes', '14:00:00', '20:00:00', 15),
(31, 3, 'Uso libre', 'Martes', '08:00:00', '14:00:00', 10),
(32, 3, 'Uso libre', 'Martes', '14:00:00', '20:00:00', 15),
(33, 3, 'Uso libre', 'Miércoles', '08:00:00', '14:00:00', 10),
(34, 3, 'Uso libre', 'Miércoles', '14:00:00', '20:00:00', 15),
(35, 3, 'Uso libre', 'Jueves', '08:00:00', '14:00:00', 10),
(36, 3, 'Uso libre', 'Jueves', '14:00:00', '20:00:00', 15),
(37, 3, 'Uso libre', 'Viernes', '08:00:00', '14:00:00', 10),
(38, 3, 'Uso libre', 'Viernes', '14:00:00', '20:00:00', 15),
(39, 3, 'Uso libre', 'Sábado', '08:00:00', '14:00:00', 10),
(40, 3, 'Uso libre', 'Sábado', '14:00:00', '20:00:00', 15),
(41, 3, 'Uso libre', 'Domingo', '08:00:00', '14:00:00', 10),
(42, 3, 'Uso libre', 'Domingo', '14:00:00', '20:00:00', 15);

-- Piscina (idServicio = 4)
INSERT INTO tarifa (idTarifa, idServicio, tipoServicio, diaSemana, horaInicio, horaFin, monto) VALUES
(43, 4, 'Uso libre', 'Lunes', '08:00:00', '14:00:00', 8),
(44, 4, 'Uso libre', 'Lunes', '14:00:00', '20:00:00', 12),
(45, 4, 'Uso libre', 'Martes', '08:00:00', '14:00:00', 8),
(46, 4, 'Uso libre', 'Martes', '14:00:00', '20:00:00', 12),
(47, 4, 'Uso libre', 'Miércoles', '08:00:00', '14:00:00', 8),
(48, 4, 'Uso libre', 'Miércoles', '14:00:00', '20:00:00', 12),
(49, 4, 'Uso libre', 'Jueves', '08:00:00', '14:00:00', 8),
(50, 4, 'Uso libre', 'Jueves', '14:00:00', '20:00:00', 12),
(51, 4, 'Uso libre', 'Viernes', '08:00:00', '14:00:00', 8),
(52, 4, 'Uso libre', 'Viernes', '14:00:00', '20:00:00', 12),
(53, 4, 'Uso libre', 'Sábado', '08:00:00', '14:00:00', 8),
(54, 4, 'Uso libre', 'Sábado', '14:00:00', '20:00:00', 12),
(55, 4, 'Uso libre', 'Domingo', '08:00:00', '14:00:00', 8),
(56, 4, 'Uso libre', 'Domingo', '14:00:00', '20:00:00', 12);


-- Pista de Atletismo (idServicio = 5)
INSERT INTO tarifa (idTarifa, idServicio, tipoServicio, diaSemana, horaInicio, horaFin, monto) VALUES
(57, 5, 'Uso libre', 'Lunes', '08:00:00', '14:00:00', 5),
(58, 5, 'Uso libre', 'Lunes', '14:00:00', '20:00:00', 8),
(59, 5, 'Uso libre', 'Martes', '08:00:00', '14:00:00', 5),
(60, 5, 'Uso libre', 'Martes', '14:00:00', '20:00:00', 8),
(61, 5, 'Uso libre', 'Miércoles', '08:00:00', '14:00:00', 5),
(62, 5, 'Uso libre', 'Miércoles', '14:00:00', '20:00:00', 8),
(63, 5, 'Uso libre', 'Jueves', '08:00:00', '14:00:00', 5),
(64, 5, 'Uso libre', 'Jueves', '14:00:00', '20:00:00', 8),
(65, 5, 'Uso libre', 'Viernes', '08:00:00', '14:00:00', 5),
(66, 5, 'Uso libre', 'Viernes', '14:00:00', '20:00:00', 8),
(67, 5, 'Uso libre', 'Sábado', '08:00:00', '14:00:00', 5),
(68, 5, 'Uso libre', 'Sábado', '14:00:00', '20:00:00', 8),
(69, 5, 'Uso libre', 'Domingo', '08:00:00', '14:00:00', 5),
(70, 5, 'Uso libre', 'Domingo', '14:00:00', '20:00:00', 8);

INSERT INTO descuento (idDescuento, codigo, tipoDescuento, valor, fechaInicio, fechaFinal, idServicio) VALUES
(1, 'VERANO2025', 'Porcentaje', 10, '2025-01-01', '2025-03-31', 4),
(2, 'NUEVOUSUARIO', 'Porcentaje', 5, '2025-01-01', '2025-12-31', 3),
(3, 'DEPORTE10', 'Porcentaje', 10, '2025-01-01', '2025-12-31', 1),
(4, 'PISTALIBRE', 'Porcentaje', 15, '2025-01-01', '2025-06-30', 5),
(5, 'FINSEMANA', 'Porcentaje', 20, '2025-01-01', '2025-12-31', 2);

INSERT INTO informacionpago (idInformacionPago, fecha, hora, tipo, total, estado) VALUES
(1, '2025-01-05', '10:15:00', 'Tarjeta', 30, 'Pagado'),
(2, '2025-01-06', '11:30:00', 'Transferencia', 40, 'En proceso'),
(3, '2025-01-07', '14:00:00', 'Tarjeta', 20, 'Pagado'),
(4, '2025-01-08', '15:45:00', 'Transferencia', 25, 'En espera'),
(5, '2025-01-09', '09:50:00', 'Tarjeta', 30, 'Cancelado'),
(6, '2025-01-10', '12:20:00', 'Transferencia', 15, 'Pagado'),
(7, '2025-01-11', '16:10:00', 'Tarjeta', 40, 'Pagado'),
(8, '2025-01-12', '13:30:00', 'Transferencia', 18, 'En espera'),
(9, '2025-01-13', '11:10:00', 'Tarjeta', 22, 'En proceso'),
(10, '2025-01-14', '14:50:00', 'Transferencia', 35, 'Pagado'),
(11, '2025-01-15', '10:05:00', 'Tarjeta', 12, 'En espera'),
(12, '2025-01-16', '17:20:00', 'Transferencia', 28, 'Cancelado'),
(13, '2025-01-17', '09:30:00', 'Tarjeta', 25, 'Pagado'),
(14, '2025-01-18', '15:40:00', 'Transferencia', 32, 'Pagado'),
(15, '2025-01-19', '13:15:00', 'Tarjeta', 20, 'En proceso'),
(16, '2025-01-20', '12:40:00', 'Transferencia', 18, 'Pagado'),
(17, '2025-01-21', '16:50:00', 'Tarjeta', 35, 'Cancelado'),
(18, '2025-01-22', '11:25:00', 'Transferencia', 22, 'Pagado'),
(19, '2025-01-23', '10:30:00', 'Tarjeta', 15, 'En espera'),
(20, '2025-01-24', '17:10:00', 'Transferencia', 40, 'Pagado'),
(21, '2025-01-25', '17:12:00', 'Transferencia', 40, 'Pagado'),
(22, '2025-02-05', '10:15:00', 'Tarjeta', 30, 'Pagado'),
(23, '2025-02-06', '11:30:00', 'Transferencia', 40, 'En proceso'),
(24, '2025-02-07', '14:00:00', 'Tarjeta', 20, 'Pagado'),
(25, '2025-02-08', '15:45:00', 'Transferencia', 25, 'En espera'),
(26, '2025-02-09', '09:50:00', 'Tarjeta', 30, 'Cancelado'),
(27, '2025-03-10', '12:20:00', 'Transferencia', 15, 'Pagado'),
(28, '2025-03-12', '13:30:00', 'Transferencia', 18, 'En espera'),
(29, '2025-04-13', '11:10:00', 'Tarjeta', 22, 'En proceso'),
(30, '2025-04-14', '14:50:00', 'Transferencia', 35, 'Pagado'),
(31, '2025-04-15', '10:05:00', 'Tarjeta', 12, 'En espera'),
(32, '2025-05-16', '17:20:00', 'Transferencia', 28, 'Cancelado'),
(33, '2025-05-17', '09:30:00', 'Tarjeta', 25, 'Pagado'),
(34, '2025-05-18', '15:40:00', 'Transferencia', 32, 'Pagado'),
(35, '2025-05-19', '13:15:00', 'Tarjeta', 20, 'En proceso'),
(36, '2025-05-20', '12:40:00', 'Transferencia', 18, 'Pagado'),
(37, '2025-06-21', '16:50:00', 'Tarjeta', 35, 'Cancelado'),
(38, '2025-06-22', '11:25:00', 'Transferencia', 22, 'Pagado'),
(39, '2025-06-23', '10:30:00', 'Tarjeta', 15, 'En espera'),
(40, '2025-06-24', '17:10:00', 'Transferencia', 40, 'Pagado'),
(41, '2025-06-25', '17:12:00', 'Transferencia', 40, 'Pagado');

INSERT INTO reserva (idReserva, idUsuario, idInformacionPago, fecha, horaInicio, horaFin, estado, fechaHoraRegistro, idInstanciaServicio) VALUES
(1, 5, 1, '2025-02-05', '08:00:00', '10:00:00', 1, '2025-02-04 10:00:00', 1),
(2, 6, 2, '2025-02-06', '14:00:00', '16:00:00', 1, '2025-02-05 11:00:00', 3),
(3, 7, 3, '2025-02-07', '08:00:00', '10:00:00', 1, '2025-02-06 13:00:00', 5),
(4, 8, 4, '2025-02-08', '14:00:00', '16:00:00', 1, '2025-02-07 15:00:00', 7),
(5, 9, 5, '2025-02-09', '08:00:00', '10:00:00', 1, '2025-02-08 09:30:00', 9),
(6, 10, 6, '2025-02-10', '14:00:00', '16:00:00', 1, '2025-02-09 12:00:00', 11),
(7, 11, 7, '2025-02-11', '08:00:00', '10:00:00', 1, '2025-02-10 14:00:00', 13),
(8, 12, 8, '2025-02-12', '14:00:00', '16:00:00', 1, '2025-02-11 16:00:00', 15),
(9, 13, 9, '2025-02-13', '08:00:00', '10:00:00', 1, '2025-02-12 09:00:00', 17),
(10, 14, 10, '2025-02-14', '14:00:00', '16:00:00', 1, '2025-02-13 11:00:00', 19),
(11, 15, 11, '2025-02-15', '08:00:00', '10:00:00', 1, '2025-02-14 13:00:00', 2),
(12, 16, 12, '2025-02-16', '14:00:00', '16:00:00', 1, '2025-02-15 15:00:00', 4),
(13, 17, 13, '2025-02-17', '08:00:00', '10:00:00', 1, '2025-02-16 08:00:00', 6),
(14, 18, 14, '2025-02-18', '14:00:00', '16:00:00', 1, '2025-02-17 10:00:00', 8),
(15, 19, 15, '2025-02-19', '08:00:00', '10:00:00', 1, '2025-02-18 12:00:00', 10),
(16, 20, 16, '2025-02-20', '14:00:00', '16:00:00', 1, '2025-02-19 14:00:00', 12),
(17, 21, 17, '2025-02-21', '08:00:00', '10:00:00', 1, '2025-02-20 16:00:00', 14),
(18, 22, 18, '2025-02-22', '14:00:00', '16:00:00', 1, '2025-02-21 09:00:00', 16),
(19, 23, 19, '2025-02-23', '08:00:00', '10:00:00', 1, '2025-02-22 11:00:00', 18),
(20, 24, 20, '2025-02-24', '14:00:00', '16:00:00', 1, '2025-02-23 13:00:00', 20),
(21, 5, 21, '2025-02-07', '08:00:00', '10:00:00', 1, '2025-02-05 10:00:00', 1),
(22, 5, 22, '2025-02-05', '08:00:00', '10:00:00', 1, '2025-02-04 10:00:00', 1),
(23, 6, 23, '2025-02-06', '14:00:00', '16:00:00', 1, '2025-02-05 11:00:00', 3),
(24, 7, 24, '2025-02-07', '08:00:00', '10:00:00', 1, '2025-02-06 13:00:00', 5),
(25, 8, 25, '2025-02-08', '14:00:00', '16:00:00', 1, '2025-02-07 15:00:00', 7),
(26, 9, 26, '2025-02-09', '08:00:00', '10:00:00', 1, '2025-02-08 09:30:00', 9),
(27, 10, 27, '2025-02-10', '14:00:00', '16:00:00', 1, '2025-02-09 12:00:00', 11),
(28, 11, 28, '2025-02-11', '08:00:00', '10:00:00', 1, '2025-02-10 14:00:00', 13),
(29, 12, 29, '2025-02-12', '14:00:00', '16:00:00', 1, '2025-02-11 16:00:00', 15),
(30, 13, 30, '2025-02-13', '08:00:00', '10:00:00', 1, '2025-02-12 09:00:00', 17),
(31, 14, 31, '2025-02-14', '14:00:00', '16:00:00', 1, '2025-02-13 11:00:00', 19),
(32, 15, 32, '2025-02-15', '08:00:00', '10:00:00', 1, '2025-02-14 13:00:00', 2),
(33, 16, 33, '2025-02-16', '14:00:00', '16:00:00', 1, '2025-02-15 15:00:00', 4),
(34, 17, 34, '2025-02-17', '08:00:00', '10:00:00', 1, '2025-02-16 08:00:00', 6),
(35, 18, 35, '2025-02-18', '14:00:00', '16:00:00', 1, '2025-02-17 10:00:00', 8),
(36, 19, 36, '2025-02-19', '08:00:00', '10:00:00', 1, '2025-02-18 12:00:00', 10),
(37, 20, 37, '2025-02-20', '14:00:00', '16:00:00', 1, '2025-02-19 14:00:00', 12),
(38, 21, 38, '2025-02-21', '08:00:00', '10:00:00', 1, '2025-02-20 16:00:00', 14),
(39, 22, 39, '2025-02-22', '14:00:00', '16:00:00', 1, '2025-02-21 09:00:00', 16),
(40, 23, 40, '2025-02-23', '08:00:00', '10:00:00', 1, '2025-02-22 11:00:00', 18),
(41, 24, 41, '2025-02-24', '14:00:00', '16:00:00', 1, '2025-02-23 13:00:00', 20);

-- inserts de hoario -- marzo abril 2025
INSERT INTO horariosemanal (idHorarioSemanal, idAdministrador, idCoordinador, fechaInicio, fechaFin, fechaCreacion) VALUES
(1, 1, 3, '2025-03-03', '2025-03-09', '2025-03-02'),
(2, 1, 25, '2025-03-03', '2025-03-09', '2025-03-02'),
(3, 1, 26, '2025-03-03', '2025-03-09', '2025-03-02'),
(4, 1, 27, '2025-03-03', '2025-03-09', '2025-03-02'),
(5, 1, 28, '2025-03-03', '2025-03-09', '2025-03-02'),
(6, 1, 29, '2025-03-03', '2025-03-09', '2025-03-02'),
(7, 1, 30, '2025-03-03', '2025-03-09', '2025-03-02'),
(8, 1, 31, '2025-03-03', '2025-03-09', '2025-03-02'),
(9, 1, 32, '2025-03-03', '2025-03-09', '2025-03-02'),
(10, 1, 33, '2025-03-03', '2025-03-09', '2025-03-02'),
(11, 1, 3, '2025-03-10', '2025-03-16', '2025-03-09'),
(12, 1, 25, '2025-03-10', '2025-03-16', '2025-03-09'),
(13, 1, 26, '2025-03-10', '2025-03-16', '2025-03-09'),
(14, 1, 27, '2025-03-10', '2025-03-16', '2025-03-09'),
(15, 1, 28, '2025-03-10', '2025-03-16', '2025-03-09'),
(16, 1, 29, '2025-03-10', '2025-03-16', '2025-03-09'),
(17, 1, 30, '2025-03-10', '2025-03-16', '2025-03-09'),
(18, 1, 31, '2025-03-10', '2025-03-16', '2025-03-09'),
(19, 1, 32, '2025-03-10', '2025-03-16', '2025-03-09'),
(20, 1, 33, '2025-03-10', '2025-03-16', '2025-03-09'),
(21, 1, 3, '2025-03-17', '2025-03-23', '2025-03-16'),
(22, 1, 25, '2025-03-17', '2025-03-23', '2025-03-16'),
(23, 1, 26, '2025-03-17', '2025-03-23', '2025-03-16'),
(24, 1, 27, '2025-03-17', '2025-03-23', '2025-03-16'),
(25, 1, 28, '2025-03-17', '2025-03-23', '2025-03-16'),
(26, 1, 29, '2025-03-17', '2025-03-23', '2025-03-16'),
(27, 1, 30, '2025-03-17', '2025-03-23', '2025-03-16'),
(28, 1, 31, '2025-03-17', '2025-03-23', '2025-03-16'),
(29, 1, 32, '2025-03-17', '2025-03-23', '2025-03-16'),
(30, 1, 33, '2025-03-17', '2025-03-23', '2025-03-16'),
(31, 1, 3, '2025-03-24', '2025-03-30', '2025-03-23'),
(32, 1, 25, '2025-03-24', '2025-03-30', '2025-03-23'),
(33, 1, 26, '2025-03-24', '2025-03-30', '2025-03-23'),
(34, 1, 27, '2025-03-24', '2025-03-30', '2025-03-23'),
(35, 1, 28, '2025-03-24', '2025-03-30', '2025-03-23'),
(36, 1, 29, '2025-03-24', '2025-03-30', '2025-03-23'),
(37, 1, 30, '2025-03-24', '2025-03-30', '2025-03-23'),
(38, 1, 31, '2025-03-24', '2025-03-30', '2025-03-23'),
(39, 1, 32, '2025-03-24', '2025-03-30', '2025-03-23'),
(40, 1, 33, '2025-03-24', '2025-03-30', '2025-03-23'),
(41, 1, 3, '2025-03-31', '2025-04-06', '2025-03-30'),
(42, 1, 25, '2025-03-31', '2025-04-06', '2025-03-30'),
(43, 1, 26, '2025-03-31', '2025-04-06', '2025-03-30'),
(44, 1, 27, '2025-03-31', '2025-04-06', '2025-03-30'),
(45, 1, 28, '2025-03-31', '2025-04-06', '2025-03-30'),
(46, 1, 29, '2025-03-31', '2025-04-06', '2025-03-30'),
(47, 1, 30, '2025-03-31', '2025-04-06', '2025-03-30'),
(48, 1, 31, '2025-03-31', '2025-04-06', '2025-03-30'),
(49, 1, 32, '2025-03-31', '2025-04-06', '2025-03-30'),
(50, 1, 33, '2025-03-31', '2025-04-06', '2025-03-30'),
(51, 1, 3, '2025-04-07', '2025-04-13', '2025-04-06'),
(52, 1, 25, '2025-04-07', '2025-04-13', '2025-04-06'),
(53, 1, 26, '2025-04-07', '2025-04-13', '2025-04-06'),
(54, 1, 27, '2025-04-07', '2025-04-13', '2025-04-06'),
(55, 1, 28, '2025-04-07', '2025-04-13', '2025-04-06'),
(56, 1, 29, '2025-04-07', '2025-04-13', '2025-04-06'),
(57, 1, 30, '2025-04-07', '2025-04-13', '2025-04-06'),
(58, 1, 31, '2025-04-07', '2025-04-13', '2025-04-06'),
(59, 1, 32, '2025-04-07', '2025-04-13', '2025-04-06'),
(60, 1, 33, '2025-04-07', '2025-04-13', '2025-04-06'),
(61, 1, 3, '2025-04-14', '2025-04-20', '2025-04-13'),
(62, 1, 25, '2025-04-14', '2025-04-20', '2025-04-13'),
(63, 1, 26, '2025-04-14', '2025-04-20', '2025-04-13'),
(64, 1, 27, '2025-04-14', '2025-04-20', '2025-04-13'),
(65, 1, 28, '2025-04-14', '2025-04-20', '2025-04-13'),
(66, 1, 29, '2025-04-14', '2025-04-20', '2025-04-13'),
(67, 1, 30, '2025-04-14', '2025-04-20', '2025-04-13'),
(68, 1, 31, '2025-04-14', '2025-04-20', '2025-04-13'),
(69, 1, 32, '2025-04-14', '2025-04-20', '2025-04-13'),
(70, 1, 33, '2025-04-14', '2025-04-20', '2025-04-13'),
(71, 1, 3, '2025-04-21', '2025-04-27', '2025-04-20'),
(72, 1, 25, '2025-04-21', '2025-04-27', '2025-04-20'),
(73, 1, 26, '2025-04-21', '2025-04-27', '2025-04-20'),
(74, 1, 27, '2025-04-21', '2025-04-27', '2025-04-20'),
(75, 1, 28, '2025-04-21', '2025-04-27', '2025-04-20'),
(76, 1, 29, '2025-04-21', '2025-04-27', '2025-04-20'),
(77, 1, 30, '2025-04-21', '2025-04-27', '2025-04-20'),
(78, 1, 31, '2025-04-21', '2025-04-27', '2025-04-20'),
(79, 1, 32, '2025-04-21', '2025-04-27', '2025-04-20'),
(80, 1, 33, '2025-04-21', '2025-04-27', '2025-04-20'),
(81, 1, 3, '2025-04-28', '2025-05-04', '2025-04-27'),
(82, 1, 25, '2025-04-28', '2025-05-04', '2025-04-27'),
(83, 1, 26, '2025-04-28', '2025-05-04', '2025-04-27'),
(84, 1, 27, '2025-04-28', '2025-05-04', '2025-04-27'),
(85, 1, 28, '2025-04-28', '2025-05-04', '2025-04-27'),
(86, 1, 29, '2025-04-28', '2025-05-04', '2025-04-27'),
(87, 1, 30, '2025-04-28', '2025-05-04', '2025-04-27'),
(88, 1, 31, '2025-04-28', '2025-05-04', '2025-04-27'),
(89, 1, 32, '2025-04-28', '2025-05-04', '2025-04-27'),
(90, 1, 33, '2025-04-28', '2025-05-04', '2025-04-27'),
(91, 1, 3, '2025-05-05', '2025-05-11', '2025-05-04'),
(92, 1, 25, '2025-05-05', '2025-05-11', '2025-05-04'),
(93, 1, 26, '2025-05-05', '2025-05-11', '2025-05-04'),
(94, 1, 27, '2025-05-05', '2025-05-11', '2025-05-04'),
(95, 1, 28, '2025-05-05', '2025-05-11', '2025-05-04'),
(96, 1, 29, '2025-05-05', '2025-05-11', '2025-05-04'),
(97, 1, 30, '2025-05-05', '2025-05-11', '2025-05-04'),
(98, 1, 31, '2025-05-05', '2025-05-11', '2025-05-04'),
(99, 1, 32, '2025-05-05', '2025-05-11', '2025-05-04'),
(100, 1, 33, '2025-05-05', '2025-05-11', '2025-05-04');

INSERT INTO horario (idHorario, idHorarioSemanal, idComplejoDeportivo, fecha, horaIngreso, horaSalida) VALUES
(1, 1, 1, '2025-03-03', '08:00:00', '16:00:00'),
(2, 1, 1, '2025-03-04', '08:00:00', '16:00:00'),
(3, 1, 1, '2025-03-05', '08:00:00', '16:00:00'),
(4, 1, 1, '2025-03-06', '08:00:00', '16:00:00'),
(5, 1, 1, '2025-03-07', '08:00:00', '16:00:00'),
(6, 1, 1, '2025-03-08', '08:00:00', '16:00:00'),
(7, 1, 1, '2025-03-09', '08:00:00', '16:00:00'),
(8, 2, 2, '2025-03-03', '08:00:00', '16:00:00'),
(9, 2, 2, '2025-03-04', '08:00:00', '16:00:00'),
(10, 2, 2, '2025-03-05', '08:00:00', '16:00:00'),
(11, 2, 2, '2025-03-06', '08:00:00', '16:00:00'),
(12, 2, 2, '2025-03-07', '08:00:00', '16:00:00'),
(13, 2, 2, '2025-03-08', '08:00:00', '16:00:00'),
(14, 2, 2, '2025-03-09', '08:00:00', '16:00:00'),
(15, 3, 3, '2025-03-03', '08:00:00', '16:00:00'),
(16, 3, 3, '2025-03-04', '08:00:00', '16:00:00'),
(17, 3, 3, '2025-03-05', '08:00:00', '16:00:00'),
(18, 3, 3, '2025-03-06', '08:00:00', '16:00:00'),
(19, 3, 3, '2025-03-07', '08:00:00', '16:00:00'),
(20, 3, 3, '2025-03-08', '08:00:00', '16:00:00'),
(21, 3, 3, '2025-03-09', '08:00:00', '16:00:00'),
(22, 4, 4, '2025-03-03', '08:00:00', '16:00:00'),
(23, 4, 4, '2025-03-04', '08:00:00', '16:00:00'),
(24, 4, 4, '2025-03-05', '08:00:00', '16:00:00'),
(25, 4, 4, '2025-03-06', '08:00:00', '16:00:00'),
(26, 4, 4, '2025-03-07', '08:00:00', '16:00:00'),
(27, 4, 4, '2025-03-08', '08:00:00', '16:00:00'),
(28, 4, 4, '2025-03-09', '08:00:00', '16:00:00'),
(29, 5, 5, '2025-03-03', '08:00:00', '16:00:00'),
(30, 5, 5, '2025-03-04', '08:00:00', '16:00:00'),
(31, 5, 5, '2025-03-05', '08:00:00', '16:00:00'),
(32, 5, 5, '2025-03-06', '08:00:00', '16:00:00'),
(33, 5, 5, '2025-03-07', '08:00:00', '16:00:00'),
(34, 5, 5, '2025-03-08', '08:00:00', '16:00:00'),
(35, 5, 5, '2025-03-09', '08:00:00', '16:00:00'),
(36, 6, 6, '2025-03-03', '08:00:00', '16:00:00'),
(37, 6, 6, '2025-03-04', '08:00:00', '16:00:00'),
(38, 6, 6, '2025-03-05', '08:00:00', '16:00:00'),
(39, 6, 6, '2025-03-06', '08:00:00', '16:00:00'),
(40, 6, 6, '2025-03-07', '08:00:00', '16:00:00'),
(41, 6, 6, '2025-03-08', '08:00:00', '16:00:00'),
(42, 6, 6, '2025-03-09', '08:00:00', '16:00:00'),
(43, 7, 7, '2025-03-03', '08:00:00', '16:00:00'),
(44, 7, 7, '2025-03-04', '08:00:00', '16:00:00'),
(45, 7, 7, '2025-03-05', '08:00:00', '16:00:00'),
(46, 7, 7, '2025-03-06', '08:00:00', '16:00:00'),
(47, 7, 7, '2025-03-07', '08:00:00', '16:00:00'),
(48, 7, 7, '2025-03-08', '08:00:00', '16:00:00'),
(49, 7, 7, '2025-03-09', '08:00:00', '16:00:00'),
(50, 8, 8, '2025-03-03', '08:00:00', '16:00:00'),
(51, 8, 8, '2025-03-04', '08:00:00', '16:00:00'),
(52, 8, 8, '2025-03-05', '08:00:00', '16:00:00'),
(53, 8, 8, '2025-03-06', '08:00:00', '16:00:00'),
(54, 8, 8, '2025-03-07', '08:00:00', '16:00:00'),
(55, 8, 8, '2025-03-08', '08:00:00', '16:00:00'),
(56, 8, 8, '2025-03-09', '08:00:00', '16:00:00'),
(57, 9, 9, '2025-03-03', '08:00:00', '16:00:00'),
(58, 9, 9, '2025-03-04', '08:00:00', '16:00:00'),
(59, 9, 9, '2025-03-05', '08:00:00', '16:00:00'),
(60, 9, 9, '2025-03-06', '08:00:00', '16:00:00'),
(61, 9, 9, '2025-03-07', '08:00:00', '16:00:00'),
(62, 9, 9, '2025-03-08', '08:00:00', '16:00:00'),
(63, 9, 9, '2025-03-09', '08:00:00', '16:00:00'),
(64, 10, 10, '2025-03-03', '08:00:00', '16:00:00'),
(65, 10, 10, '2025-03-04', '08:00:00', '16:00:00'),
(66, 10, 10, '2025-03-05', '08:00:00', '16:00:00'),
(67, 10, 10, '2025-03-06', '08:00:00', '16:00:00'),
(68, 10, 10, '2025-03-07', '08:00:00', '16:00:00'),
(69, 10, 10, '2025-03-08', '08:00:00', '16:00:00'),
(70, 10, 10, '2025-03-09', '08:00:00', '16:00:00'),
(71, 11, 1, '2025-03-10', '08:00:00', '16:00:00'),
(72, 11, 1, '2025-03-11', '08:00:00', '16:00:00'),
(73, 11, 1, '2025-03-12', '08:00:00', '16:00:00'),
(74, 11, 1, '2025-03-13', '08:00:00', '16:00:00'),
(75, 11, 1, '2025-03-14', '08:00:00', '16:00:00'),
(76, 11, 1, '2025-03-15', '08:00:00', '16:00:00'),
(77, 11, 1, '2025-03-16', '08:00:00', '16:00:00'),
(78, 12, 2, '2025-03-10', '08:00:00', '16:00:00'),
(79, 12, 2, '2025-03-11', '08:00:00', '16:00:00'),
(80, 12, 2, '2025-03-12', '08:00:00', '16:00:00'),
(81, 12, 2, '2025-03-13', '08:00:00', '16:00:00'),
(82, 12, 2, '2025-03-14', '08:00:00', '16:00:00'),
(83, 12, 2, '2025-03-15', '08:00:00', '16:00:00'),
(84, 12, 2, '2025-03-16', '08:00:00', '16:00:00'),
(85, 13, 3, '2025-03-10', '08:00:00', '16:00:00'),
(86, 13, 3, '2025-03-11', '08:00:00', '16:00:00'),
(87, 13, 3, '2025-03-12', '08:00:00', '16:00:00'),
(88, 13, 3, '2025-03-13', '08:00:00', '16:00:00'),
(89, 13, 3, '2025-03-14', '08:00:00', '16:00:00'),
(90, 13, 3, '2025-03-15', '08:00:00', '16:00:00'),
(91, 13, 3, '2025-03-16', '08:00:00', '16:00:00'),
(92, 14, 4, '2025-03-10', '08:00:00', '16:00:00'),
(93, 14, 4, '2025-03-11', '08:00:00', '16:00:00'),
(94, 14, 4, '2025-03-12', '08:00:00', '16:00:00'),
(95, 14, 4, '2025-03-13', '08:00:00', '16:00:00'),
(96, 14, 4, '2025-03-14', '08:00:00', '16:00:00'),
(97, 14, 4, '2025-03-15', '08:00:00', '16:00:00'),
(98, 14, 4, '2025-03-16', '08:00:00', '16:00:00'),
(99, 15, 5, '2025-03-10', '08:00:00', '16:00:00'),
(100, 15, 5, '2025-03-11', '08:00:00', '16:00:00'),
(101, 15, 5, '2025-03-12', '08:00:00', '16:00:00'),
(102, 15, 5, '2025-03-13', '08:00:00', '16:00:00'),
(103, 15, 5, '2025-03-14', '08:00:00', '16:00:00'),
(104, 15, 5, '2025-03-15', '08:00:00', '16:00:00'),
(105, 15, 5, '2025-03-16', '08:00:00', '16:00:00'),
(106, 16, 6, '2025-03-10', '08:00:00', '16:00:00'),
(107, 16, 6, '2025-03-11', '08:00:00', '16:00:00'),
(108, 16, 6, '2025-03-12', '08:00:00', '16:00:00'),
(109, 16, 6, '2025-03-13', '08:00:00', '16:00:00'),
(110, 16, 6, '2025-03-14', '08:00:00', '16:00:00'),
(111, 16, 6, '2025-03-15', '08:00:00', '16:00:00'),
(112, 16, 6, '2025-03-16', '08:00:00', '16:00:00'),
(113, 17, 7, '2025-03-10', '08:00:00', '16:00:00'),
(114, 17, 7, '2025-03-11', '08:00:00', '16:00:00'),
(115, 17, 7, '2025-03-12', '08:00:00', '16:00:00'),
(116, 17, 7, '2025-03-13', '08:00:00', '16:00:00'),
(117, 17, 7, '2025-03-14', '08:00:00', '16:00:00'),
(118, 17, 7, '2025-03-15', '08:00:00', '16:00:00'),
(119, 17, 7, '2025-03-16', '08:00:00', '16:00:00'),
(120, 18, 8, '2025-03-10', '08:00:00', '16:00:00'),
(121, 18, 8, '2025-03-11', '08:00:00', '16:00:00'),
(122, 18, 8, '2025-03-12', '08:00:00', '16:00:00'),
(123, 18, 8, '2025-03-13', '08:00:00', '16:00:00'),
(124, 18, 8, '2025-03-14', '08:00:00', '16:00:00'),
(125, 18, 8, '2025-03-15', '08:00:00', '16:00:00'),
(126, 18, 8, '2025-03-16', '08:00:00', '16:00:00'),
(127, 19, 9, '2025-03-10', '08:00:00', '16:00:00'),
(128, 19, 9, '2025-03-11', '08:00:00', '16:00:00'),
(129, 19, 9, '2025-03-12', '08:00:00', '16:00:00'),
(130, 19, 9, '2025-03-13', '08:00:00', '16:00:00'),
(131, 19, 9, '2025-03-14', '08:00:00', '16:00:00'),
(132, 19, 9, '2025-03-15', '08:00:00', '16:00:00'),
(133, 19, 9, '2025-03-16', '08:00:00', '16:00:00'),
(134, 20, 10, '2025-03-10', '08:00:00', '16:00:00'),
(135, 20, 10, '2025-03-11', '08:00:00', '16:00:00'),
(136, 20, 10, '2025-03-12', '08:00:00', '16:00:00'),
(137, 20, 10, '2025-03-13', '08:00:00', '16:00:00'),
(138, 20, 10, '2025-03-14', '08:00:00', '16:00:00'),
(139, 20, 10, '2025-03-15', '08:00:00', '16:00:00'),
(140, 20, 10, '2025-03-16', '08:00:00', '16:00:00'),
(141, 21, 1, '2025-03-17', '08:00:00', '16:00:00'),
(142, 21, 1, '2025-03-18', '08:00:00', '16:00:00'),
(143, 21, 1, '2025-03-19', '08:00:00', '16:00:00'),
(144, 21, 1, '2025-03-20', '08:00:00', '16:00:00'),
(145, 21, 1, '2025-03-21', '08:00:00', '16:00:00'),
(146, 21, 1, '2025-03-22', '08:00:00', '16:00:00'),
(147, 21, 1, '2025-03-23', '08:00:00', '16:00:00'),
(148, 22, 2, '2025-03-17', '08:00:00', '16:00:00'),
(149, 22, 2, '2025-03-18', '08:00:00', '16:00:00'),
(150, 22, 2, '2025-03-19', '08:00:00', '16:00:00'),
(151, 22, 2, '2025-03-20', '08:00:00', '16:00:00'),
(152, 22, 2, '2025-03-21', '08:00:00', '16:00:00'),
(153, 22, 2, '2025-03-22', '08:00:00', '16:00:00'),
(154, 22, 2, '2025-03-23', '08:00:00', '16:00:00'),
(155, 23, 3, '2025-03-17', '08:00:00', '16:00:00'),
(156, 23, 3, '2025-03-18', '08:00:00', '16:00:00'),
(157, 23, 3, '2025-03-19', '08:00:00', '16:00:00'),
(158, 23, 3, '2025-03-20', '08:00:00', '16:00:00'),
(159, 23, 3, '2025-03-21', '08:00:00', '16:00:00'),
(160, 23, 3, '2025-03-22', '08:00:00', '16:00:00'),
(161, 23, 3, '2025-03-23', '08:00:00', '16:00:00'),
(162, 24, 4, '2025-03-17', '08:00:00', '16:00:00'),
(163, 24, 4, '2025-03-18', '08:00:00', '16:00:00'),
(164, 24, 4, '2025-03-19', '08:00:00', '16:00:00'),
(165, 24, 4, '2025-03-20', '08:00:00', '16:00:00'),
(166, 24, 4, '2025-03-21', '08:00:00', '16:00:00'),
(167, 24, 4, '2025-03-22', '08:00:00', '16:00:00'),
(168, 24, 4, '2025-03-23', '08:00:00', '16:00:00'),
(169, 25, 5, '2025-03-17', '08:00:00', '16:00:00'),
(170, 25, 5, '2025-03-18', '08:00:00', '16:00:00'),
(171, 25, 5, '2025-03-19', '08:00:00', '16:00:00'),
(172, 25, 5, '2025-03-20', '08:00:00', '16:00:00'),
(173, 25, 5, '2025-03-21', '08:00:00', '16:00:00'),
(174, 25, 5, '2025-03-22', '08:00:00', '16:00:00'),
(175, 25, 5, '2025-03-23', '08:00:00', '16:00:00'),
(176, 26, 6, '2025-03-17', '08:00:00', '16:00:00'),
(177, 26, 6, '2025-03-18', '08:00:00', '16:00:00'),
(178, 26, 6, '2025-03-19', '08:00:00', '16:00:00'),
(179, 26, 6, '2025-03-20', '08:00:00', '16:00:00'),
(180, 26, 6, '2025-03-21', '08:00:00', '16:00:00'),
(181, 26, 6, '2025-03-22', '08:00:00', '16:00:00'),
(182, 26, 6, '2025-03-23', '08:00:00', '16:00:00'),
(183, 27, 7, '2025-03-17', '08:00:00', '16:00:00'),
(184, 27, 7, '2025-03-18', '08:00:00', '16:00:00'),
(185, 27, 7, '2025-03-19', '08:00:00', '16:00:00'),
(186, 27, 7, '2025-03-20', '08:00:00', '16:00:00'),
(187, 27, 7, '2025-03-21', '08:00:00', '16:00:00'),
(188, 27, 7, '2025-03-22', '08:00:00', '16:00:00'),
(189, 27, 7, '2025-03-23', '08:00:00', '16:00:00'),
(190, 28, 8, '2025-03-17', '08:00:00', '16:00:00'),
(191, 28, 8, '2025-03-18', '08:00:00', '16:00:00'),
(192, 28, 8, '2025-03-19', '08:00:00', '16:00:00'),
(193, 28, 8, '2025-03-20', '08:00:00', '16:00:00'),
(194, 28, 8, '2025-03-21', '08:00:00', '16:00:00'),
(195, 28, 8, '2025-03-22', '08:00:00', '16:00:00'),
(196, 28, 8, '2025-03-23', '08:00:00', '16:00:00'),
(197, 29, 9, '2025-03-17', '08:00:00', '16:00:00'),
(198, 29, 9, '2025-03-18', '08:00:00', '16:00:00'),
(199, 29, 9, '2025-03-19', '08:00:00', '16:00:00'),
(200, 29, 9, '2025-03-20', '08:00:00', '16:00:00'),
(201, 29, 9, '2025-03-21', '08:00:00', '16:00:00'),
(202, 29, 9, '2025-03-22', '08:00:00', '16:00:00'),
(203, 29, 9, '2025-03-23', '08:00:00', '16:00:00'),
(204, 30, 10, '2025-03-17', '08:00:00', '16:00:00'),
(205, 30, 10, '2025-03-18', '08:00:00', '16:00:00'),
(206, 30, 10, '2025-03-19', '08:00:00', '16:00:00'),
(207, 30, 10, '2025-03-20', '08:00:00', '16:00:00'),
(208, 30, 10, '2025-03-21', '08:00:00', '16:00:00'),
(209, 30, 10, '2025-03-22', '08:00:00', '16:00:00'),
(210, 30, 10, '2025-03-23', '08:00:00', '16:00:00'),
(211, 31, 1, '2025-03-24', '08:00:00', '16:00:00'),
(212, 31, 1, '2025-03-25', '08:00:00', '16:00:00'),
(213, 31, 1, '2025-03-26', '08:00:00', '16:00:00'),
(214, 31, 1, '2025-03-27', '08:00:00', '16:00:00'),
(215, 31, 1, '2025-03-28', '08:00:00', '16:00:00'),
(216, 31, 1, '2025-03-29', '08:00:00', '16:00:00'),
(217, 31, 1, '2025-03-30', '08:00:00', '16:00:00'),
(218, 32, 2, '2025-03-24', '08:00:00', '16:00:00'),
(219, 32, 2, '2025-03-25', '08:00:00', '16:00:00'),
(220, 32, 2, '2025-03-26', '08:00:00', '16:00:00'),
(221, 32, 2, '2025-03-27', '08:00:00', '16:00:00'),
(222, 32, 2, '2025-03-28', '08:00:00', '16:00:00'),
(223, 32, 2, '2025-03-29', '08:00:00', '16:00:00'),
(224, 32, 2, '2025-03-30', '08:00:00', '16:00:00'),
(225, 33, 3, '2025-03-24', '08:00:00', '16:00:00'),
(226, 33, 3, '2025-03-25', '08:00:00', '16:00:00'),
(227, 33, 3, '2025-03-26', '08:00:00', '16:00:00'),
(228, 33, 3, '2025-03-27', '08:00:00', '16:00:00'),
(229, 33, 3, '2025-03-28', '08:00:00', '16:00:00'),
(230, 33, 3, '2025-03-29', '08:00:00', '16:00:00'),
(231, 33, 3, '2025-03-30', '08:00:00', '16:00:00'),
(232, 34, 4, '2025-03-24', '08:00:00', '16:00:00'),
(233, 34, 4, '2025-03-25', '08:00:00', '16:00:00'),
(234, 34, 4, '2025-03-26', '08:00:00', '16:00:00'),
(235, 34, 4, '2025-03-27', '08:00:00', '16:00:00'),
(236, 34, 4, '2025-03-28', '08:00:00', '16:00:00'),
(237, 34, 4, '2025-03-29', '08:00:00', '16:00:00'),
(238, 34, 4, '2025-03-30', '08:00:00', '16:00:00'),
(239, 35, 5, '2025-03-24', '08:00:00', '16:00:00'),
(240, 35, 5, '2025-03-25', '08:00:00', '16:00:00'),
(241, 35, 5, '2025-03-26', '08:00:00', '16:00:00'),
(242, 35, 5, '2025-03-27', '08:00:00', '16:00:00'),
(243, 35, 5, '2025-03-28', '08:00:00', '16:00:00'),
(244, 35, 5, '2025-03-29', '08:00:00', '16:00:00'),
(245, 35, 5, '2025-03-30', '08:00:00', '16:00:00'),
(246, 36, 6, '2025-03-24', '08:00:00', '16:00:00'),
(247, 36, 6, '2025-03-25', '08:00:00', '16:00:00'),
(248, 36, 6, '2025-03-26', '08:00:00', '16:00:00'),
(249, 36, 6, '2025-03-27', '08:00:00', '16:00:00'),
(250, 36, 6, '2025-03-28', '08:00:00', '16:00:00'),
(251, 36, 6, '2025-03-29', '08:00:00', '16:00:00'),
(252, 36, 6, '2025-03-30', '08:00:00', '16:00:00'),
(253, 37, 7, '2025-03-24', '08:00:00', '16:00:00'),
(254, 37, 7, '2025-03-25', '08:00:00', '16:00:00'),
(255, 37, 7, '2025-03-26', '08:00:00', '16:00:00'),
(256, 37, 7, '2025-03-27', '08:00:00', '16:00:00'),
(257, 37, 7, '2025-03-28', '08:00:00', '16:00:00'),
(258, 37, 7, '2025-03-29', '08:00:00', '16:00:00'),
(259, 37, 7, '2025-03-30', '08:00:00', '16:00:00'),
(260, 38, 8, '2025-03-24', '08:00:00', '16:00:00'),
(261, 38, 8, '2025-03-25', '08:00:00', '16:00:00'),
(262, 38, 8, '2025-03-26', '08:00:00', '16:00:00'),
(263, 38, 8, '2025-03-27', '08:00:00', '16:00:00'),
(264, 38, 8, '2025-03-28', '08:00:00', '16:00:00'),
(265, 38, 8, '2025-03-29', '08:00:00', '16:00:00'),
(266, 38, 8, '2025-03-30', '08:00:00', '16:00:00'),
(267, 39, 9, '2025-03-24', '08:00:00', '16:00:00'),
(268, 39, 9, '2025-03-25', '08:00:00', '16:00:00'),
(269, 39, 9, '2025-03-26', '08:00:00', '16:00:00'),
(270, 39, 9, '2025-03-27', '08:00:00', '16:00:00'),
(271, 39, 9, '2025-03-28', '08:00:00', '16:00:00'),
(272, 39, 9, '2025-03-29', '08:00:00', '16:00:00'),
(273, 39, 9, '2025-03-30', '08:00:00', '16:00:00'),
(274, 40, 10, '2025-03-24', '08:00:00', '16:00:00'),
(275, 40, 10, '2025-03-25', '08:00:00', '16:00:00'),
(276, 40, 10, '2025-03-26', '08:00:00', '16:00:00'),
(277, 40, 10, '2025-03-27', '08:00:00', '16:00:00'),
(278, 40, 10, '2025-03-28', '08:00:00', '16:00:00'),
(279, 40, 10, '2025-03-29', '08:00:00', '16:00:00'),
(280, 40, 10, '2025-03-30', '08:00:00', '16:00:00'),
(281, 41, 1, '2025-03-31', '08:00:00', '16:00:00'),
(282, 41, 1, '2025-04-01', '08:00:00', '16:00:00'),
(283, 41, 1, '2025-04-02', '08:00:00', '16:00:00'),
(284, 41, 1, '2025-04-03', '08:00:00', '16:00:00'),
(285, 41, 1, '2025-04-04', '08:00:00', '16:00:00'),
(286, 41, 1, '2025-04-05', '08:00:00', '16:00:00'),
(287, 41, 1, '2025-04-06', '08:00:00', '16:00:00'),
(288, 42, 2, '2025-03-31', '08:00:00', '16:00:00'),
(289, 42, 2, '2025-04-01', '08:00:00', '16:00:00'),
(290, 42, 2, '2025-04-02', '08:00:00', '16:00:00'),
(291, 42, 2, '2025-04-03', '08:00:00', '16:00:00'),
(292, 42, 2, '2025-04-04', '08:00:00', '16:00:00'),
(293, 42, 2, '2025-04-05', '08:00:00', '16:00:00'),
(294, 42, 2, '2025-04-06', '08:00:00', '16:00:00'),
(295, 43, 3, '2025-03-31', '08:00:00', '16:00:00'),
(296, 43, 3, '2025-04-01', '08:00:00', '16:00:00'),
(297, 43, 3, '2025-04-02', '08:00:00', '16:00:00'),
(298, 43, 3, '2025-04-03', '08:00:00', '16:00:00'),
(299, 43, 3, '2025-04-04', '08:00:00', '16:00:00'),
(300, 43, 3, '2025-04-05', '08:00:00', '16:00:00'),
(301, 43, 3, '2025-04-06', '08:00:00', '16:00:00'),
(302, 44, 4, '2025-03-31', '08:00:00', '16:00:00'),
(303, 44, 4, '2025-04-01', '08:00:00', '16:00:00'),
(304, 44, 4, '2025-04-02', '08:00:00', '16:00:00'),
(305, 44, 4, '2025-04-03', '08:00:00', '16:00:00'),
(306, 44, 4, '2025-04-04', '08:00:00', '16:00:00'),
(307, 44, 4, '2025-04-05', '08:00:00', '16:00:00'),
(308, 44, 4, '2025-04-06', '08:00:00', '16:00:00'),
(309, 45, 5, '2025-03-31', '08:00:00', '16:00:00'),
(310, 45, 5, '2025-04-01', '08:00:00', '16:00:00'),
(311, 45, 5, '2025-04-02', '08:00:00', '16:00:00'),
(312, 45, 5, '2025-04-03', '08:00:00', '16:00:00'),
(313, 45, 5, '2025-04-04', '08:00:00', '16:00:00'),
(314, 45, 5, '2025-04-05', '08:00:00', '16:00:00'),
(315, 45, 5, '2025-04-06', '08:00:00', '16:00:00'),
(316, 46, 6, '2025-03-31', '08:00:00', '16:00:00'),
(317, 46, 6, '2025-04-01', '08:00:00', '16:00:00'),
(318, 46, 6, '2025-04-02', '08:00:00', '16:00:00'),
(319, 46, 6, '2025-04-03', '08:00:00', '16:00:00'),
(320, 46, 6, '2025-04-04', '08:00:00', '16:00:00'),
(321, 46, 6, '2025-04-05', '08:00:00', '16:00:00'),
(322, 46, 6, '2025-04-06', '08:00:00', '16:00:00'),
(323, 47, 7, '2025-03-31', '08:00:00', '16:00:00'),
(324, 47, 7, '2025-04-01', '08:00:00', '16:00:00'),
(325, 47, 7, '2025-04-02', '08:00:00', '16:00:00'),
(326, 47, 7, '2025-04-03', '08:00:00', '16:00:00'),
(327, 47, 7, '2025-04-04', '08:00:00', '16:00:00'),
(328, 47, 7, '2025-04-05', '08:00:00', '16:00:00'),
(329, 47, 7, '2025-04-06', '08:00:00', '16:00:00'),
(330, 48, 8, '2025-03-31', '08:00:00', '16:00:00'),
(331, 48, 8, '2025-04-01', '08:00:00', '16:00:00'),
(332, 48, 8, '2025-04-02', '08:00:00', '16:00:00'),
(333, 48, 8, '2025-04-03', '08:00:00', '16:00:00'),
(334, 48, 8, '2025-04-04', '08:00:00', '16:00:00'),
(335, 48, 8, '2025-04-05', '08:00:00', '16:00:00'),
(336, 48, 8, '2025-04-06', '08:00:00', '16:00:00'),
(337, 49, 9, '2025-03-31', '08:00:00', '16:00:00'),
(338, 49, 9, '2025-04-01', '08:00:00', '16:00:00'),
(339, 49, 9, '2025-04-02', '08:00:00', '16:00:00'),
(340, 49, 9, '2025-04-03', '08:00:00', '16:00:00'),
(341, 49, 9, '2025-04-04', '08:00:00', '16:00:00'),
(342, 49, 9, '2025-04-05', '08:00:00', '16:00:00'),
(343, 49, 9, '2025-04-06', '08:00:00', '16:00:00'),
(344, 50, 10, '2025-03-31', '08:00:00', '16:00:00'),
(345, 50, 10, '2025-04-01', '08:00:00', '16:00:00'),
(346, 50, 10, '2025-04-02', '08:00:00', '16:00:00'),
(347, 50, 10, '2025-04-03', '08:00:00', '16:00:00'),
(348, 50, 10, '2025-04-04', '08:00:00', '16:00:00'),
(349, 50, 10, '2025-04-05', '08:00:00', '16:00:00'),
(350, 50, 10, '2025-04-06', '08:00:00', '16:00:00'),
(351, 51, 1, '2025-04-07', '08:00:00', '16:00:00'),
(352, 51, 1, '2025-04-08', '08:00:00', '16:00:00'),
(353, 51, 1, '2025-04-09', '08:00:00', '16:00:00'),
(354, 51, 1, '2025-04-10', '08:00:00', '16:00:00'),
(355, 51, 1, '2025-04-11', '08:00:00', '16:00:00'),
(356, 51, 1, '2025-04-12', '08:00:00', '16:00:00'),
(357, 51, 1, '2025-04-13', '08:00:00', '16:00:00'),
(358, 52, 2, '2025-04-07', '08:00:00', '16:00:00'),
(359, 52, 2, '2025-04-08', '08:00:00', '16:00:00'),
(360, 52, 2, '2025-04-09', '08:00:00', '16:00:00'),
(361, 52, 2, '2025-04-10', '08:00:00', '16:00:00'),
(362, 52, 2, '2025-04-11', '08:00:00', '16:00:00'),
(363, 52, 2, '2025-04-12', '08:00:00', '16:00:00'),
(364, 52, 2, '2025-04-13', '08:00:00', '16:00:00'),
(365, 53, 3, '2025-04-07', '08:00:00', '16:00:00'),
(366, 53, 3, '2025-04-08', '08:00:00', '16:00:00'),
(367, 53, 3, '2025-04-09', '08:00:00', '16:00:00'),
(368, 53, 3, '2025-04-10', '08:00:00', '16:00:00'),
(369, 53, 3, '2025-04-11', '08:00:00', '16:00:00'),
(370, 53, 3, '2025-04-12', '08:00:00', '16:00:00'),
(371, 53, 3, '2025-04-13', '08:00:00', '16:00:00'),
(372, 54, 4, '2025-04-07', '08:00:00', '16:00:00'),
(373, 54, 4, '2025-04-08', '08:00:00', '16:00:00'),
(374, 54, 4, '2025-04-09', '08:00:00', '16:00:00'),
(375, 54, 4, '2025-04-10', '08:00:00', '16:00:00'),
(376, 54, 4, '2025-04-11', '08:00:00', '16:00:00'),
(377, 54, 4, '2025-04-12', '08:00:00', '16:00:00'),
(378, 54, 4, '2025-04-13', '08:00:00', '16:00:00'),
(379, 55, 5, '2025-04-07', '08:00:00', '16:00:00'),
(380, 55, 5, '2025-04-08', '08:00:00', '16:00:00'),
(381, 55, 5, '2025-04-09', '08:00:00', '16:00:00'),
(382, 55, 5, '2025-04-10', '08:00:00', '16:00:00'),
(383, 55, 5, '2025-04-11', '08:00:00', '16:00:00'),
(384, 55, 5, '2025-04-12', '08:00:00', '16:00:00'),
(385, 55, 5, '2025-04-13', '08:00:00', '16:00:00'),
(386, 56, 6, '2025-04-07', '08:00:00', '16:00:00'),
(387, 56, 6, '2025-04-08', '08:00:00', '16:00:00'),
(388, 56, 6, '2025-04-09', '08:00:00', '16:00:00'),
(389, 56, 6, '2025-04-10', '08:00:00', '16:00:00'),
(390, 56, 6, '2025-04-11', '08:00:00', '16:00:00'),
(391, 56, 6, '2025-04-12', '08:00:00', '16:00:00'),
(392, 56, 6, '2025-04-13', '08:00:00', '16:00:00'),
(393, 57, 7, '2025-04-07', '08:00:00', '16:00:00'),
(394, 57, 7, '2025-04-08', '08:00:00', '16:00:00'),
(395, 57, 7, '2025-04-09', '08:00:00', '16:00:00'),
(396, 57, 7, '2025-04-10', '08:00:00', '16:00:00'),
(397, 57, 7, '2025-04-11', '08:00:00', '16:00:00'),
(398, 57, 7, '2025-04-12', '08:00:00', '16:00:00'),
(399, 57, 7, '2025-04-13', '08:00:00', '16:00:00'),
(400, 58, 8, '2025-04-07', '08:00:00', '16:00:00'),
(401, 58, 8, '2025-04-08', '08:00:00', '16:00:00'),
(402, 58, 8, '2025-04-09', '08:00:00', '16:00:00'),
(403, 58, 8, '2025-04-10', '08:00:00', '16:00:00'),
(404, 58, 8, '2025-04-11', '08:00:00', '16:00:00'),
(405, 58, 8, '2025-04-12', '08:00:00', '16:00:00'),
(406, 58, 8, '2025-04-13', '08:00:00', '16:00:00'),
(407, 59, 9, '2025-04-07', '08:00:00', '16:00:00'),
(408, 59, 9, '2025-04-08', '08:00:00', '16:00:00'),
(409, 59, 9, '2025-04-09', '08:00:00', '16:00:00'),
(410, 59, 9, '2025-04-10', '08:00:00', '16:00:00'),
(411, 59, 9, '2025-04-11', '08:00:00', '16:00:00'),
(412, 59, 9, '2025-04-12', '08:00:00', '16:00:00'),
(413, 59, 9, '2025-04-13', '08:00:00', '16:00:00'),
(414, 60, 10, '2025-04-07', '08:00:00', '16:00:00'),
(415, 60, 10, '2025-04-08', '08:00:00', '16:00:00'),
(416, 60, 10, '2025-04-09', '08:00:00', '16:00:00'),
(417, 60, 10, '2025-04-10', '08:00:00', '16:00:00'),
(418, 60, 10, '2025-04-11', '08:00:00', '16:00:00'),
(419, 60, 10, '2025-04-12', '08:00:00', '16:00:00'),
(420, 60, 10, '2025-04-13', '08:00:00', '16:00:00'),
(421, 61, 1, '2025-04-14', '08:00:00', '16:00:00'),
(422, 61, 1, '2025-04-15', '08:00:00', '16:00:00'),
(423, 61, 1, '2025-04-16', '08:00:00', '16:00:00'),
(424, 61, 1, '2025-04-17', '08:00:00', '16:00:00'),
(425, 61, 1, '2025-04-18', '08:00:00', '16:00:00'),
(426, 61, 1, '2025-04-19', '08:00:00', '16:00:00'),
(427, 61, 1, '2025-04-20', '08:00:00', '16:00:00'),
(428, 62, 2, '2025-04-14', '08:00:00', '16:00:00'),
(429, 62, 2, '2025-04-15', '08:00:00', '16:00:00'),
(430, 62, 2, '2025-04-16', '08:00:00', '16:00:00'),
(431, 62, 2, '2025-04-17', '08:00:00', '16:00:00'),
(432, 62, 2, '2025-04-18', '08:00:00', '16:00:00'),
(433, 62, 2, '2025-04-19', '08:00:00', '16:00:00'),
(434, 62, 2, '2025-04-20', '08:00:00', '16:00:00'),
(435, 63, 3, '2025-04-14', '08:00:00', '16:00:00'),
(436, 63, 3, '2025-04-15', '08:00:00', '16:00:00'),
(437, 63, 3, '2025-04-16', '08:00:00', '16:00:00'),
(438, 63, 3, '2025-04-17', '08:00:00', '16:00:00'),
(439, 63, 3, '2025-04-18', '08:00:00', '16:00:00'),
(440, 63, 3, '2025-04-19', '08:00:00', '16:00:00'),
(441, 63, 3, '2025-04-20', '08:00:00', '16:00:00'),
(442, 64, 4, '2025-04-14', '08:00:00', '16:00:00'),
(443, 64, 4, '2025-04-15', '08:00:00', '16:00:00'),
(444, 64, 4, '2025-04-16', '08:00:00', '16:00:00'),
(445, 64, 4, '2025-04-17', '08:00:00', '16:00:00'),
(446, 64, 4, '2025-04-18', '08:00:00', '16:00:00'),
(447, 64, 4, '2025-04-19', '08:00:00', '16:00:00'),
(448, 64, 4, '2025-04-20', '08:00:00', '16:00:00'),
(449, 65, 5, '2025-04-14', '08:00:00', '16:00:00'),
(450, 65, 5, '2025-04-15', '08:00:00', '16:00:00'),
(451, 65, 5, '2025-04-16', '08:00:00', '16:00:00'),
(452, 65, 5, '2025-04-17', '08:00:00', '16:00:00'),
(453, 65, 5, '2025-04-18', '08:00:00', '16:00:00'),
(454, 65, 5, '2025-04-19', '08:00:00', '16:00:00'),
(455, 65, 5, '2025-04-20', '08:00:00', '16:00:00'),
(456, 66, 6, '2025-04-14', '08:00:00', '16:00:00'),
(457, 66, 6, '2025-04-15', '08:00:00', '16:00:00'),
(458, 66, 6, '2025-04-16', '08:00:00', '16:00:00'),
(459, 66, 6, '2025-04-17', '08:00:00', '16:00:00'),
(460, 66, 6, '2025-04-18', '08:00:00', '16:00:00'),
(461, 66, 6, '2025-04-19', '08:00:00', '16:00:00'),
(462, 66, 6, '2025-04-20', '08:00:00', '16:00:00'),
(463, 67, 7, '2025-04-14', '08:00:00', '16:00:00'),
(464, 67, 7, '2025-04-15', '08:00:00', '16:00:00'),
(465, 67, 7, '2025-04-16', '08:00:00', '16:00:00'),
(466, 67, 7, '2025-04-17', '08:00:00', '16:00:00'),
(467, 67, 7, '2025-04-18', '08:00:00', '16:00:00'),
(468, 67, 7, '2025-04-19', '08:00:00', '16:00:00'),
(469, 67, 7, '2025-04-20', '08:00:00', '16:00:00'),
(470, 68, 8, '2025-04-14', '08:00:00', '16:00:00'),
(471, 68, 8, '2025-04-15', '08:00:00', '16:00:00'),
(472, 68, 8, '2025-04-16', '08:00:00', '16:00:00'),
(473, 68, 8, '2025-04-17', '08:00:00', '16:00:00'),
(474, 68, 8, '2025-04-18', '08:00:00', '16:00:00'),
(475, 68, 8, '2025-04-19', '08:00:00', '16:00:00'),
(476, 68, 8, '2025-04-20', '08:00:00', '16:00:00'),
(477, 69, 9, '2025-04-14', '08:00:00', '16:00:00'),
(478, 69, 9, '2025-04-15', '08:00:00', '16:00:00'),
(479, 69, 9, '2025-04-16', '08:00:00', '16:00:00'),
(480, 69, 9, '2025-04-17', '08:00:00', '16:00:00'),
(481, 69, 9, '2025-04-18', '08:00:00', '16:00:00'),
(482, 69, 9, '2025-04-19', '08:00:00', '16:00:00'),
(483, 69, 9, '2025-04-20', '08:00:00', '16:00:00'),
(484, 70, 10, '2025-04-14', '08:00:00', '16:00:00'),
(485, 70, 10, '2025-04-15', '08:00:00', '16:00:00'),
(486, 70, 10, '2025-04-16', '08:00:00', '16:00:00'),
(487, 70, 10, '2025-04-17', '08:00:00', '16:00:00'),
(488, 70, 10, '2025-04-18', '08:00:00', '16:00:00'),
(489, 70, 10, '2025-04-19', '08:00:00', '16:00:00'),
(490, 70, 10, '2025-04-20', '08:00:00', '16:00:00'),
(491, 71, 1, '2025-04-21', '08:00:00', '16:00:00'),
(492, 71, 1, '2025-04-22', '08:00:00', '16:00:00'),
(493, 71, 1, '2025-04-23', '08:00:00', '16:00:00'),
(494, 71, 1, '2025-04-24', '08:00:00', '16:00:00'),
(495, 71, 1, '2025-04-25', '08:00:00', '16:00:00'),
(496, 71, 1, '2025-04-26', '08:00:00', '16:00:00'),
(497, 71, 1, '2025-04-27', '08:00:00', '16:00:00'),
(498, 72, 2, '2025-04-21', '08:00:00', '16:00:00'),
(499, 72, 2, '2025-04-22', '08:00:00', '16:00:00'),
(500, 72, 2, '2025-04-23', '08:00:00', '16:00:00'),
(501, 72, 2, '2025-04-24', '08:00:00', '16:00:00'),
(502, 72, 2, '2025-04-25', '08:00:00', '16:00:00'),
(503, 72, 2, '2025-04-26', '08:00:00', '16:00:00'),
(504, 72, 2, '2025-04-27', '08:00:00', '16:00:00'),
(505, 73, 3, '2025-04-21', '08:00:00', '16:00:00'),
(506, 73, 3, '2025-04-22', '08:00:00', '16:00:00'),
(507, 73, 3, '2025-04-23', '08:00:00', '16:00:00'),
(508, 73, 3, '2025-04-24', '08:00:00', '16:00:00'),
(509, 73, 3, '2025-04-25', '08:00:00', '16:00:00'),
(510, 73, 3, '2025-04-26', '08:00:00', '16:00:00'),
(511, 73, 3, '2025-04-27', '08:00:00', '16:00:00'),
(512, 74, 4, '2025-04-21', '08:00:00', '16:00:00'),
(513, 74, 4, '2025-04-22', '08:00:00', '16:00:00'),
(514, 74, 4, '2025-04-23', '08:00:00', '16:00:00'),
(515, 74, 4, '2025-04-24', '08:00:00', '16:00:00'),
(516, 74, 4, '2025-04-25', '08:00:00', '16:00:00'),
(517, 74, 4, '2025-04-26', '08:00:00', '16:00:00'),
(518, 74, 4, '2025-04-27', '08:00:00', '16:00:00'),
(519, 75, 5, '2025-04-21', '08:00:00', '16:00:00'),
(520, 75, 5, '2025-04-22', '08:00:00', '16:00:00'),
(521, 75, 5, '2025-04-23', '08:00:00', '16:00:00'),
(522, 75, 5, '2025-04-24', '08:00:00', '16:00:00'),
(523, 75, 5, '2025-04-25', '08:00:00', '16:00:00'),
(524, 75, 5, '2025-04-26', '08:00:00', '16:00:00'),
(525, 75, 5, '2025-04-27', '08:00:00', '16:00:00'),
(526, 76, 6, '2025-04-21', '08:00:00', '16:00:00'),
(527, 76, 6, '2025-04-22', '08:00:00', '16:00:00'),
(528, 76, 6, '2025-04-23', '08:00:00', '16:00:00'),
(529, 76, 6, '2025-04-24', '08:00:00', '16:00:00'),
(530, 76, 6, '2025-04-25', '08:00:00', '16:00:00'),
(531, 76, 6, '2025-04-26', '08:00:00', '16:00:00'),
(532, 76, 6, '2025-04-27', '08:00:00', '16:00:00'),
(533, 77, 7, '2025-04-21', '08:00:00', '16:00:00'),
(534, 77, 7, '2025-04-22', '08:00:00', '16:00:00'),
(535, 77, 7, '2025-04-23', '08:00:00', '16:00:00'),
(536, 77, 7, '2025-04-24', '08:00:00', '16:00:00'),
(537, 77, 7, '2025-04-25', '08:00:00', '16:00:00'),
(538, 77, 7, '2025-04-26', '08:00:00', '16:00:00'),
(539, 77, 7, '2025-04-27', '08:00:00', '16:00:00'),
(540, 78, 8, '2025-04-21', '08:00:00', '16:00:00'),
(541, 78, 8, '2025-04-22', '08:00:00', '16:00:00'),
(542, 78, 8, '2025-04-23', '08:00:00', '16:00:00'),
(543, 78, 8, '2025-04-24', '08:00:00', '16:00:00'),
(544, 78, 8, '2025-04-25', '08:00:00', '16:00:00'),
(545, 78, 8, '2025-04-26', '08:00:00', '16:00:00'),
(546, 78, 8, '2025-04-27', '08:00:00', '16:00:00'),
(547, 79, 9, '2025-04-21', '08:00:00', '16:00:00'),
(548, 79, 9, '2025-04-22', '08:00:00', '16:00:00'),
(549, 79, 9, '2025-04-23', '08:00:00', '16:00:00'),
(550, 79, 9, '2025-04-24', '08:00:00', '16:00:00'),
(551, 79, 9, '2025-04-25', '08:00:00', '16:00:00'),
(552, 79, 9, '2025-04-26', '08:00:00', '16:00:00'),
(553, 79, 9, '2025-04-27', '08:00:00', '16:00:00'),
(554, 80, 10, '2025-04-21', '08:00:00', '16:00:00'),
(555, 80, 10, '2025-04-22', '08:00:00', '16:00:00'),
(556, 80, 10, '2025-04-23', '08:00:00', '16:00:00'),
(557, 80, 10, '2025-04-24', '08:00:00', '16:00:00'),
(558, 80, 10, '2025-04-25', '08:00:00', '16:00:00'),
(559, 80, 10, '2025-04-26', '08:00:00', '16:00:00'),
(560, 80, 10, '2025-04-27', '08:00:00', '16:00:00'),
(561, 81, 1, '2025-04-28', '08:00:00', '16:00:00'),
(562, 81, 1, '2025-04-29', '08:00:00', '16:00:00'),
(563, 81, 1, '2025-04-30', '08:00:00', '16:00:00'),
(564, 81, 1, '2025-05-01', '08:00:00', '16:00:00'),
(565, 81, 1, '2025-05-02', '08:00:00', '16:00:00'),
(566, 81, 1, '2025-05-03', '08:00:00', '16:00:00'),
(567, 81, 1, '2025-05-04', '08:00:00', '16:00:00'),
(568, 82, 2, '2025-04-28', '08:00:00', '16:00:00'),
(569, 82, 2, '2025-04-29', '08:00:00', '16:00:00'),
(570, 82, 2, '2025-04-30', '08:00:00', '16:00:00'),
(571, 82, 2, '2025-05-01', '08:00:00', '16:00:00'),
(572, 82, 2, '2025-05-02', '08:00:00', '16:00:00'),
(573, 82, 2, '2025-05-03', '08:00:00', '16:00:00'),
(574, 82, 2, '2025-05-04', '08:00:00', '16:00:00'),
(575, 83, 3, '2025-04-28', '08:00:00', '16:00:00'),
(576, 83, 3, '2025-04-29', '08:00:00', '16:00:00'),
(577, 83, 3, '2025-04-30', '08:00:00', '16:00:00'),
(578, 83, 3, '2025-05-01', '08:00:00', '16:00:00'),
(579, 83, 3, '2025-05-02', '08:00:00', '16:00:00'),
(580, 83, 3, '2025-05-03', '08:00:00', '16:00:00'),
(581, 83, 3, '2025-05-04', '08:00:00', '16:00:00'),
(582, 84, 4, '2025-04-28', '08:00:00', '16:00:00'),
(583, 84, 4, '2025-04-29', '08:00:00', '16:00:00'),
(584, 84, 4, '2025-04-30', '08:00:00', '16:00:00'),
(585, 84, 4, '2025-05-01', '08:00:00', '16:00:00'),
(586, 84, 4, '2025-05-02', '08:00:00', '16:00:00'),
(587, 84, 4, '2025-05-03', '08:00:00', '16:00:00'),
(588, 84, 4, '2025-05-04', '08:00:00', '16:00:00'),
(589, 85, 5, '2025-04-28', '08:00:00', '16:00:00'),
(590, 85, 5, '2025-04-29', '08:00:00', '16:00:00'),
(591, 85, 5, '2025-04-30', '08:00:00', '16:00:00'),
(592, 85, 5, '2025-05-01', '08:00:00', '16:00:00'),
(593, 85, 5, '2025-05-02', '08:00:00', '16:00:00'),
(594, 85, 5, '2025-05-03', '08:00:00', '16:00:00'),
(595, 85, 5, '2025-05-04', '08:00:00', '16:00:00'),
(596, 86, 6, '2025-04-28', '08:00:00', '16:00:00'),
(597, 86, 6, '2025-04-29', '08:00:00', '16:00:00'),
(598, 86, 6, '2025-04-30', '08:00:00', '16:00:00'),
(599, 86, 6, '2025-05-01', '08:00:00', '16:00:00'),
(600, 86, 6, '2025-05-02', '08:00:00', '16:00:00'),
(601, 86, 6, '2025-05-03', '08:00:00', '16:00:00'),
(602, 86, 6, '2025-05-04', '08:00:00', '16:00:00'),
(603, 87, 7, '2025-04-28', '08:00:00', '16:00:00'),
(604, 87, 7, '2025-04-29', '08:00:00', '16:00:00'),
(605, 87, 7, '2025-04-30', '08:00:00', '16:00:00'),
(606, 87, 7, '2025-05-01', '08:00:00', '16:00:00'),
(607, 87, 7, '2025-05-02', '08:00:00', '16:00:00'),
(608, 87, 7, '2025-05-03', '08:00:00', '16:00:00'),
(609, 87, 7, '2025-05-04', '08:00:00', '16:00:00'),
(610, 88, 8, '2025-04-28', '08:00:00', '16:00:00'),
(611, 88, 8, '2025-04-29', '08:00:00', '16:00:00'),
(612, 88, 8, '2025-04-30', '08:00:00', '16:00:00'),
(613, 88, 8, '2025-05-01', '08:00:00', '16:00:00'),
(614, 88, 8, '2025-05-02', '08:00:00', '16:00:00'),
(615, 88, 8, '2025-05-03', '08:00:00', '16:00:00'),
(616, 88, 8, '2025-05-04', '08:00:00', '16:00:00'),
(617, 89, 9, '2025-04-28', '08:00:00', '16:00:00'),
(618, 89, 9, '2025-04-29', '08:00:00', '16:00:00'),
(619, 89, 9, '2025-04-30', '08:00:00', '16:00:00'),
(620, 89, 9, '2025-05-01', '08:00:00', '16:00:00'),
(621, 89, 9, '2025-05-02', '08:00:00', '16:00:00'),
(622, 89, 9, '2025-05-03', '08:00:00', '16:00:00'),
(623, 89, 9, '2025-05-04', '08:00:00', '16:00:00'),
(624, 90, 10, '2025-04-28', '08:00:00', '16:00:00'),
(625, 90, 10, '2025-04-29', '08:00:00', '16:00:00'),
(626, 90, 10, '2025-04-30', '08:00:00', '16:00:00'),
(627, 90, 10, '2025-05-01', '08:00:00', '16:00:00'),
(628, 90, 10, '2025-05-02', '08:00:00', '16:00:00'),
(629, 90, 10, '2025-05-03', '08:00:00', '16:00:00'),
(630, 90, 10, '2025-05-04', '08:00:00', '16:00:00'),
(631, 91, 1, '2025-05-05', '08:00:00', '16:00:00'),
(632, 91, 1, '2025-05-06', '08:00:00', '16:00:00'),
(633, 91, 1, '2025-05-07', '08:00:00', '16:00:00'),
(634, 91, 1, '2025-05-08', '08:00:00', '16:00:00'),
(635, 91, 1, '2025-05-09', '08:00:00', '16:00:00'),
(636, 91, 1, '2025-05-10', '08:00:00', '16:00:00'),
(637, 91, 1, '2025-05-11', '08:00:00', '16:00:00'),
(638, 92, 2, '2025-05-05', '08:00:00', '16:00:00'),
(639, 92, 2, '2025-05-06', '08:00:00', '16:00:00'),
(640, 92, 2, '2025-05-07', '08:00:00', '16:00:00'),
(641, 92, 2, '2025-05-08', '08:00:00', '16:00:00'),
(642, 92, 2, '2025-05-09', '08:00:00', '16:00:00'),
(643, 92, 2, '2025-05-10', '08:00:00', '16:00:00'),
(644, 92, 2, '2025-05-11', '08:00:00', '16:00:00'),
(645, 93, 3, '2025-05-05', '08:00:00', '16:00:00'),
(646, 93, 3, '2025-05-06', '08:00:00', '16:00:00'),
(647, 93, 3, '2025-05-07', '08:00:00', '16:00:00'),
(648, 93, 3, '2025-05-08', '08:00:00', '16:00:00'),
(649, 93, 3, '2025-05-09', '08:00:00', '16:00:00'),
(650, 93, 3, '2025-05-10', '08:00:00', '16:00:00'),
(651, 93, 3, '2025-05-11', '08:00:00', '16:00:00'),
(652, 94, 4, '2025-05-05', '08:00:00', '16:00:00'),
(653, 94, 4, '2025-05-06', '08:00:00', '16:00:00'),
(654, 94, 4, '2025-05-07', '08:00:00', '16:00:00'),
(655, 94, 4, '2025-05-08', '08:00:00', '16:00:00'),
(656, 94, 4, '2025-05-09', '08:00:00', '16:00:00'),
(657, 94, 4, '2025-05-10', '08:00:00', '16:00:00'),
(658, 94, 4, '2025-05-11', '08:00:00', '16:00:00'),
(659, 95, 5, '2025-05-05', '08:00:00', '16:00:00'),
(660, 95, 5, '2025-05-06', '08:00:00', '16:00:00'),
(661, 95, 5, '2025-05-07', '08:00:00', '16:00:00'),
(662, 95, 5, '2025-05-08', '08:00:00', '16:00:00'),
(663, 95, 5, '2025-05-09', '08:00:00', '16:00:00'),
(664, 95, 5, '2025-05-10', '08:00:00', '16:00:00'),
(665, 95, 5, '2025-05-11', '08:00:00', '16:00:00'),
(666, 96, 6, '2025-05-05', '08:00:00', '16:00:00'),
(667, 96, 6, '2025-05-06', '08:00:00', '16:00:00'),
(668, 96, 6, '2025-05-07', '08:00:00', '16:00:00'),
(669, 96, 6, '2025-05-08', '08:00:00', '16:00:00'),
(670, 96, 6, '2025-05-09', '08:00:00', '16:00:00'),
(671, 96, 6, '2025-05-10', '08:00:00', '16:00:00'),
(672, 96, 6, '2025-05-11', '08:00:00', '16:00:00'),
(673, 97, 7, '2025-05-05', '08:00:00', '16:00:00'),
(674, 97, 7, '2025-05-06', '08:00:00', '16:00:00'),
(675, 97, 7, '2025-05-07', '08:00:00', '16:00:00'),
(676, 97, 7, '2025-05-08', '08:00:00', '16:00:00'),
(677, 97, 7, '2025-05-09', '08:00:00', '16:00:00'),
(678, 97, 7, '2025-05-10', '08:00:00', '16:00:00'),
(679, 97, 7, '2025-05-11', '08:00:00', '16:00:00'),
(680, 98, 8, '2025-05-05', '08:00:00', '16:00:00'),
(681, 98, 8, '2025-05-06', '08:00:00', '16:00:00'),
(682, 98, 8, '2025-05-07', '08:00:00', '16:00:00'),
(683, 98, 8, '2025-05-08', '08:00:00', '16:00:00'),
(684, 98, 8, '2025-05-09', '08:00:00', '16:00:00'),
(685, 98, 8, '2025-05-10', '08:00:00', '16:00:00'),
(686, 98, 8, '2025-05-11', '08:00:00', '16:00:00'),
(687, 99, 9, '2025-05-05', '08:00:00', '16:00:00'),
(688, 99, 9, '2025-05-06', '08:00:00', '16:00:00'),
(689, 99, 9, '2025-05-07', '08:00:00', '16:00:00'),
(690, 99, 9, '2025-05-08', '08:00:00', '16:00:00'),
(691, 99, 9, '2025-05-09', '08:00:00', '16:00:00'),
(692, 99, 9, '2025-05-10', '08:00:00', '16:00:00'),
(693, 99, 9, '2025-05-11', '08:00:00', '16:00:00'),
(694, 100, 10, '2025-05-05', '08:00:00', '16:00:00'),
(695, 100, 10, '2025-05-06', '08:00:00', '16:00:00'),
(696, 100, 10, '2025-05-07', '08:00:00', '16:00:00'),
(697, 100, 10, '2025-05-08', '08:00:00', '16:00:00'),
(698, 100, 10, '2025-05-09', '08:00:00', '16:00:00'),
(699, 100, 10, '2025-05-10', '08:00:00', '16:00:00'),
(700, 100, 10, '2025-05-11', '08:00:00', '16:00:00');

INSERT INTO validacion (idValidacion, timeStampValidacion, latitudCoordinador, longitudCoordinador, distanciaError, resultado, idHorario) VALUES
-- Horario 1: 2025-03-03
(1, '2025-03-03 07:30:00', 19.432600, -99.133200, 4.50, 'Fallido', 1),
(2, '2025-03-03 07:40:00', 19.432590, -99.133210, 6.80, 'Interrumpido', 1),
(3, '2025-03-03 07:50:00', 19.432608, -99.133209, 1.00, 'Exitoso', 1), -- Último y exitoso

-- Horario 2: 2025-03-04
(4, '2025-03-04 07:55:00', 34.052240, -118.243680, 5.00, 'Fallido', 2),
(5, '2025-03-04 08:20:00', 34.052235, -118.243683, 0.90, 'Exitoso', 2), -- Tarde pero exitoso

-- Horario 3: 2025-03-05
(6, '2025-03-05 07:45:00', 40.712770, -74.005970, 4.50, 'Fallido', 3),
(7, '2025-03-05 07:55:00', 40.712776, -74.005974, 1.20, 'Exitoso', 3),

-- Horario 4: 2025-03-06
(8, '2025-03-06 07:30:00', 51.507350, -0.127750, 5.60, 'Fallido', 4),
(9, '2025-03-06 07:50:00', 51.507355, -0.127755, 8.10, 'Interrumpido', 4),
(10, '2025-03-06 08:40:00', 51.507351, -0.127758, 1.10, 'Exitoso', 4), -- Tarde pero exitoso

-- Horario 5: 2025-03-07
(11, '2025-03-07 07:20:00', 48.856610, 2.352220, 6.20, 'Fallido', 5),
(12, '2025-03-07 07:40:00', 48.856613, 2.352222, 0.80, 'Exitoso', 5);  -- Último y exitoso


select * from reporte;

INSERT INTO reporte (idReporte, tipoReporte, fechaRecepcion, estado, asunto, descripcion, respuesta, idReserva, idHorario) VALUES
(1, 'Solicitud de reparación', '2025-03-12', 'En proceso', 'Portón dañado', 'Portón principal no cierra correctamente.', NULL, NULL, 242),
(2, 'Alerta', '2025-03-22', 'Cerrado', 'Sillas rotas', 'Varias sillas del salón están dañadas.', NULL, NULL, 253),
(3, 'Alerta', '2025-03-26', 'En proceso', 'Tablero de básquet roto', 'Tablero presenta fisuras.', NULL, NULL, 244),
(4, 'Alerta', '2025-03-26', 'En proceso', 'Daño en cerco perimétrico', 'Cerco roto en zona posterior.', NULL, NULL, 253),
(5, 'Alerta', '2025-03-19', 'En proceso', 'Sillas rotas', 'Varias sillas del salón están dañadas.', NULL, NULL, 248),
(6, 'Solicitud de reparación', '2025-03-24', 'Cerrado', 'Accidente leve', 'Usuario sufrió caída en escalera.', NULL, NULL, 245),
(7, 'Alerta', '2025-03-30', 'Abierto', 'Piso resbaloso', 'Superficie de losa muy resbalosa.', NULL, NULL, 241),
(8, 'Solicitud de reparación', '2025-03-27', 'En proceso', 'Puerta de emergencia atascada', 'No abre correctamente desde el interior.', NULL, NULL, 256),
(9, 'Alerta', '2025-03-16', 'En proceso', 'Red WiFi inestable', 'Desconexiones frecuentes en oficinas.', NULL, NULL, 250),
(10, 'Alerta', '2025-03-17', 'Abierto', 'Apagón', 'Corte de energía durante evento.', NULL, NULL, 252),
(11, 'Solicitud de reparación', '2025-03-23', 'En proceso', 'Falla en luminaria', 'Luminarias de cancha techada sin funcionar.', NULL, NULL, 246),
(12, 'Solicitud de reparación', '2025-03-14', 'En proceso', 'Goteras en techado', 'Filtraciones en techo de gimnasio.', NULL, NULL, 249),
(13, 'Alerta', '2025-03-21', 'Cerrado', 'Fuga de agua', 'Fuga detectada en baños del complejo.', NULL, NULL, 254),
(14, 'Alerta', '2025-03-29', 'Abierto', 'Vidrio roto', 'Vidrio de gimnasio roto, causa desconocida.', NULL, NULL, 247),
(15, 'Solicitud de reparación', '2025-03-11', 'Cerrado', 'Problemas de sonido', 'Altavoces no emiten sonido en cancha.', NULL, NULL, 251);

UPDATE usuario
SET telefono = '987654321'
WHERE idRol NOT IN (1, 2);

-- Insertar descuentos de prueba
INSERT INTO descuento (codigo, tipoDescuento, valor, fechaInicio, fechaFinal, idServicio) VALUES
-- Descuentos de porcentaje
('GIMNASIO20', 'PORCENTAJE', 20.0, '2024-01-01', '2025-12-31', 1),
('FUTBOL15', 'PORCENTAJE', 15.0, '2024-01-01', '2025-12-31', 2),
('BASKET25', 'PORCENTAJE', 25.0, '2024-01-01', '2025-12-31', 3),
('VERANO30', 'PORCENTAJE', 30.0, '2024-12-01', '2025-03-31', 1),

-- Descuentos de monto fijo
('PROMO10', 'FIJO', 10.0, '2024-01-01', '2025-12-31', 1),
('ESTUDIANTE5', 'FIJO', 5.0, '2024-01-01', '2025-12-31', 2),
('FAMILIAR20', 'FIJO', 20.0, '2024-06-01', '2025-06-30', 3),

-- Descuentos temporales (solo válidos por algunos meses)
('NAVIDAD50', 'PORCENTAJE', 50.0, '2024-12-01', '2025-01-15', 1),
('ANIVERSARIO', 'FIJO', 15.0, '2025-05-01', '2025-05-31', 2),

-- Descuentos para servicios específicos
('TENIS10', 'PORCENTAJE', 10.0, '2024-01-01', '2025-12-31', 4),
('VOLLEY15', 'FIJO', 15.0, '2024-01-01', '2025-12-31', 5);

-- CREAMOS UNA RESERVA DE PRUEBA PARA PROBAR EL CUPÓN DEPORTE10
-- Creamos la información de pago primero
INSERT INTO informacionpago (
    total,
    estado,
    fecha
) VALUES (
    30.00,               -- Precio ejemplo para una hora de cancha
    'Pendiente',         -- Estado pendiente para poder aplicar cupón
    curdate()
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
    curdate(),           -- Fecha de hoy
    '10:00:00',         -- Hora inicio
    '11:00:00',         -- Hora fin
    0,                  -- Estado PENDIENTE
    1,                  -- Usar el primer usuario (cambiar si es necesario)
    1,                  -- Usar la primera instancia de servicio (cambiar si es necesario)
    @idInfoPago         -- Usar el ID de información de pago creado arriba
);