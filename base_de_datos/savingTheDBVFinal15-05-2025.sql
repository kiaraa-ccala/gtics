-- MySQL Workbench Forward Engineering

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
  `fecha` DATE NULL DEFAULT NULL,
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
    REFERENCES `gestiondeportiva`.`complejodeportivo` (`idComplejoDeportivo`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
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


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
