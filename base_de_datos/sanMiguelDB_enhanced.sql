-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema GestionDeportiva
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `GestionDeportiva` ;

-- -----------------------------------------------------
-- Schema GestionDeportiva
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `GestionDeportiva` DEFAULT CHARACTER SET utf8mb4 ;
USE `GestionDeportiva` ;

-- -----------------------------------------------------
-- Table `GestionDeportiva`.`sector`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`sector` (
  `idSector` INT NOT NULL,
  `nombre` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`idSector`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`complejodeportivo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`complejodeportivo` (
  `idComplejoDeportivo` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NULL DEFAULT NULL,
  `direccion` VARCHAR(45) NULL DEFAULT NULL,
  `idSector` INT NOT NULL,
  `numeroSoporte` VARCHAR(45) NULL DEFAULT NULL,
  `latitud` DECIMAL(10,0) NULL DEFAULT NULL,
  `longitud` DECIMAL(10,0) NULL DEFAULT NULL,
  PRIMARY KEY (`idComplejoDeportivo`),
  INDEX `fk_SportLocation_sector1_idx` (`idSector` ASC) VISIBLE,
  CONSTRAINT `fk_SportLocation_sector1`
    FOREIGN KEY (`idSector`)
    REFERENCES `GestionDeportiva`.`sector` (`idSector`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`foto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`foto` (
  `idFoto` INT NOT NULL AUTO_INCREMENT,
  `nombreFoto` VARCHAR(80) NULL DEFAULT NULL,
  `foto` LONGBLOB NULL DEFAULT NULL,
  `urlFoto` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`idFoto`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`tercerizado`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`tercerizado` (
  `idTercerizado` INT NOT NULL AUTO_INCREMENT,
  `ruc` VARCHAR(11) NULL DEFAULT NULL,
  `direccionFiscal` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`idTercerizado`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`Rol`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`Rol` (
  `idRol` INT NOT NULL,
  `nombre` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`idRol`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`usuario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`usuario` (
  `idUsuario` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NULL DEFAULT NULL,
  `apellido` VARCHAR(45) NULL DEFAULT NULL,
  `dni` VARCHAR(45) NULL DEFAULT NULL,
  `direccion` VARCHAR(45) NULL DEFAULT NULL,
  `distrito` VARCHAR(45) NULL DEFAULT NULL,
  `provincia` VARCHAR(45) NULL DEFAULT NULL,
  `departamento` VARCHAR(45) NULL DEFAULT NULL,
  `idSector` INT NULL DEFAULT NULL,
  `idTercerizado` INT NULL DEFAULT NULL,
  `idFoto` INT NOT NULL,
  `idRol` INT NOT NULL,
  `telefono` VARCHAR(9) NULL,
  PRIMARY KEY (`idUsuario`),
  INDEX `fk_usuarios_sector_idx` (`idSector` ASC) VISIBLE,
  INDEX `fk_usuarios_outsourced1_idx` (`idTercerizado` ASC) VISIBLE,
  INDEX `fk_Usuario_Foto1_idx` (`idFoto` ASC) VISIBLE,
  INDEX `fk_usuario_Rol1_idx` (`idRol` ASC) VISIBLE,
  CONSTRAINT `fk_Usuario_Foto1`
    FOREIGN KEY (`idFoto`)
    REFERENCES `GestionDeportiva`.`foto` (`idFoto`),
  CONSTRAINT `fk_usuarios_outsourced1`
    FOREIGN KEY (`idTercerizado`)
    REFERENCES `GestionDeportiva`.`tercerizado` (`idTercerizado`),
  CONSTRAINT `fk_usuarios_sector`
    FOREIGN KEY (`idSector`)
    REFERENCES `GestionDeportiva`.`sector` (`idSector`),
  CONSTRAINT `fk_usuario_Rol1`
    FOREIGN KEY (`idRol`)
    REFERENCES `GestionDeportiva`.`Rol` (`idRol`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`horariosemanal`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`horariosemanal` (
  `idHorarioSemanal` INT NOT NULL AUTO_INCREMENT,
  `idAdministrador` INT NOT NULL,
  `idCoordinador` INT NOT NULL,
  `fechaInicio` DATE NULL DEFAULT NULL,
  `fechaFin` DATE NULL DEFAULT NULL,
  `fechaCreacion` DATE NULL DEFAULT NULL,
  PRIMARY KEY (`idHorarioSemanal`, `idAdministrador`, `idCoordinador`),
  INDEX `fk_HorarioSemanal_Usuario1_idx` (`idAdministrador` ASC) VISIBLE,
  INDEX `fk_HorarioSemanal_Usuario2_idx` (`idCoordinador` ASC) VISIBLE,
  CONSTRAINT `fk_HorarioSemanal_Usuario1`
    FOREIGN KEY (`idAdministrador`)
    REFERENCES `GestionDeportiva`.`usuario` (`idUsuario`),
  CONSTRAINT `fk_HorarioSemanal_Usuario2`
    FOREIGN KEY (`idCoordinador`)
    REFERENCES `GestionDeportiva`.`usuario` (`idUsuario`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`horario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`horario` (
  `idHorario` INT NOT NULL AUTO_INCREMENT,
  `idHorarioSemanal` INT NOT NULL,
  `idAdministrador` INT NOT NULL,
  `idCoordinador` INT NOT NULL,
  `idComplejoDeportivo` INT NOT NULL,
  `fecha` DATE NULL DEFAULT NULL,
  `horaIngreso` TIME NULL DEFAULT NULL,
  `horaSalida` TIME NULL DEFAULT NULL,
  PRIMARY KEY (`idHorario`, `idHorarioSemanal`, `idAdministrador`, `idCoordinador`, `idComplejoDeportivo`),
  INDEX `fk_Horario_HorarioSemanal1_idx` (`idHorarioSemanal` ASC, `idAdministrador` ASC, `idCoordinador` ASC) VISIBLE,
  INDEX `fk_Horario_ComplejoDeportivo1_idx` (`idComplejoDeportivo` ASC) VISIBLE,
  CONSTRAINT `fk_Horario_ComplejoDeportivo1`
    FOREIGN KEY (`idComplejoDeportivo`)
    REFERENCES `GestionDeportiva`.`complejodeportivo` (`idComplejoDeportivo`),
  CONSTRAINT `fk_Horario_HorarioSemanal1`
    FOREIGN KEY (`idHorarioSemanal` , `idAdministrador` , `idCoordinador`)
    REFERENCES `GestionDeportiva`.`horariosemanal` (`idHorarioSemanal` , `idAdministrador` , `idCoordinador`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`servicio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`servicio` (
  `idServicio` INT NOT NULL,
  `nombre` VARCHAR(60) NULL DEFAULT NULL,
  PRIMARY KEY (`idServicio`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`instanciaservicio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`instanciaservicio` (
  `idInstanciaServicio` INT NOT NULL,
  `idServicio` INT NOT NULL,
  `idComplejoDeportivo` INT NOT NULL,
  `nombre` VARCHAR(45) NULL DEFAULT NULL,
  `capacidadMaxima` VARCHAR(45) NULL DEFAULT NULL,
  `modoAcceso` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`idServicio`, `idComplejoDeportivo`, `idInstanciaServicio`),
  INDEX `fk_Services_has_SportLocation_SportLocation1_idx` (`idComplejoDeportivo` ASC) VISIBLE,
  INDEX `fk_Services_has_SportLocation_Services1_idx` (`idServicio` ASC) VISIBLE,
  CONSTRAINT `fk_Services_has_SportLocation_Services1`
    FOREIGN KEY (`idServicio`)
    REFERENCES `GestionDeportiva`.`servicio` (`idServicio`),
  CONSTRAINT `fk_Services_has_SportLocation_SportLocation1`
    FOREIGN KEY (`idComplejoDeportivo`)
    REFERENCES `GestionDeportiva`.`complejodeportivo` (`idComplejoDeportivo`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`informacionpago`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`informacionpago` (
  `idInformacionPago` INT NOT NULL,
  `fecha` DATE NULL DEFAULT NULL,
  `hora` TIME NULL DEFAULT NULL,
  `tipo` VARCHAR(45) NULL DEFAULT NULL,
  `total` DECIMAL(10,0) NULL DEFAULT NULL,
  `estado` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`idInformacionPago`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`reserva`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`reserva` (
  `idReserva` INT NOT NULL AUTO_INCREMENT,
  `idUsuario` INT NOT NULL,
  `idInformacionPago` INT NOT NULL,
  `fecha` DATE NULL DEFAULT NULL,
  `horaInicio` TIME NULL DEFAULT NULL,
  `horaFin` TIME NULL DEFAULT NULL,
  `estado` TINYINT NULL DEFAULT NULL,
  `fechaHoraRegistro` DATETIME NULL DEFAULT NULL,
  `idServicio` INT NOT NULL,
  `idComplejoDeportivo` INT NOT NULL,
  `idInstanciaServicio` INT NOT NULL,
  PRIMARY KEY (`idReserva`),
  INDEX `fk_ServiceAccess_Users1_idx` (`idUsuario` ASC) VISIBLE,
  INDEX `fk_ServiceAccess_PaymentInfo1_idx` (`idInformacionPago` ASC) VISIBLE,
  INDEX `fk_Reserva_InstanciaServicio1_idx` (`idServicio` ASC, `idComplejoDeportivo` ASC, `idInstanciaServicio` ASC) VISIBLE,
  CONSTRAINT `fk_Reserva_InstanciaServicio1`
    FOREIGN KEY (`idServicio` , `idComplejoDeportivo` , `idInstanciaServicio`)
    REFERENCES `GestionDeportiva`.`instanciaservicio` (`idServicio` , `idComplejoDeportivo` , `idInstanciaServicio`),
  CONSTRAINT `fk_ServiceAccess_PaymentInfo1`
    FOREIGN KEY (`idInformacionPago`)
    REFERENCES `GestionDeportiva`.`informacionpago` (`idInformacionPago`),
  CONSTRAINT `fk_ServiceAccess_Users1`
    FOREIGN KEY (`idUsuario`)
    REFERENCES `GestionDeportiva`.`usuario` (`idUsuario`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`reporte`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`reporte` (
  `idReporte` INT NOT NULL AUTO_INCREMENT,
  `tipoReporte` VARCHAR(45) NULL DEFAULT NULL,
  `fechaRecepcion` DATE NULL DEFAULT NULL,
  `estado` VARCHAR(45) NULL DEFAULT NULL,
  `asunto` VARCHAR(100) NULL DEFAULT NULL,
  `descripcion` VARCHAR(500) NULL DEFAULT NULL,
  `respuesta` VARCHAR(500) NULL DEFAULT NULL,
  `idReserva` INT NULL DEFAULT NULL,
  `idHorario` INT NOT NULL,
  `idHorarioSemanal` INT NOT NULL,
  `Horario_idAdministrador` INT NOT NULL,
  `idCoordinador` INT NOT NULL,
  `idComplejoDeportivo` INT NOT NULL,
  PRIMARY KEY (`idReporte`),
  INDEX `fk_Reporte_Reserva1_idx` (`idReserva` ASC) VISIBLE,
  INDEX `fk_Reporte_Horario1_idx` (`idHorario` ASC, `idHorarioSemanal` ASC, `Horario_idAdministrador` ASC, `idCoordinador` ASC, `idComplejoDeportivo` ASC) VISIBLE,
  CONSTRAINT `fk_Reporte_Horario1`
    FOREIGN KEY (`idHorario` , `idHorarioSemanal` , `Horario_idAdministrador` , `idCoordinador` , `idComplejoDeportivo`)
    REFERENCES `GestionDeportiva`.`horario` (`idHorario` , `idHorarioSemanal` , `idAdministrador` , `idCoordinador` , `idComplejoDeportivo`),
  CONSTRAINT `fk_Reporte_Reserva1`
    FOREIGN KEY (`idReserva`)
    REFERENCES `GestionDeportiva`.`reserva` (`idReserva`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`comentario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`comentario` (
  `idComentario` INT NOT NULL AUTO_INCREMENT,
  `fechaHora` DATETIME NULL DEFAULT NULL,
  `contenido` VARCHAR(300) NULL DEFAULT NULL,
  `idReporte` INT NOT NULL,
  PRIMARY KEY (`idComentario`),
  INDEX `fk_Comentario_Reporte1_idx` (`idReporte` ASC) VISIBLE,
  CONSTRAINT `fk_Comentario_Reporte1`
    FOREIGN KEY (`idReporte`)
    REFERENCES `GestionDeportiva`.`reporte` (`idReporte`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`credencial`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`credencial` (
  `idCredencial` INT NOT NULL AUTO_INCREMENT,
  `correo` VARCHAR(45) NULL DEFAULT NULL,
  `password` VARCHAR(45) NULL DEFAULT NULL,
  `idUsuario` INT NOT NULL,
  PRIMARY KEY (`idCredencial`),
  INDEX `fk_credencial_usuario1_idx` (`idUsuario` ASC) VISIBLE,
  CONSTRAINT `fk_credencial_usuario1`
    FOREIGN KEY (`idUsuario`)
    REFERENCES `GestionDeportiva`.`usuario` (`idUsuario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`descuento`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`descuento` (
  `idDescuento` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(45) NULL DEFAULT NULL,
  `tipoDescuento` VARCHAR(45) NULL DEFAULT NULL,
  `valor` DECIMAL NULL DEFAULT NULL,
  `fechaInicio` DATE NULL DEFAULT NULL,
  `fechaFinal` DATE NULL DEFAULT NULL,
  `idServicio` INT NOT NULL,
  PRIMARY KEY (`idDescuento`),
  INDEX `fk_Discount_Services1_idx` (`idServicio` ASC) VISIBLE,
  CONSTRAINT `fk_Discount_Services1`
    FOREIGN KEY (`idServicio`)
    REFERENCES `GestionDeportiva`.`servicio` (`idServicio`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`evidencia`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`evidencia` (
  `idEvidencia` INT NOT NULL AUTO_INCREMENT,
  `nombreArchivo` VARCHAR(100) NULL DEFAULT NULL,
  `urlArchivo` VARCHAR(100) NULL DEFAULT NULL,
  `archivo` LONGBLOB NULL DEFAULT NULL,
  `idReporte` INT NOT NULL,
  PRIMARY KEY (`idEvidencia`),
  INDEX `fk_Evidencia_Reporte1_idx` (`idReporte` ASC) VISIBLE,
  CONSTRAINT `fk_Evidencia_Reporte1`
    FOREIGN KEY (`idReporte`)
    REFERENCES `GestionDeportiva`.`reporte` (`idReporte`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`mantenimiento`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`mantenimiento` (
  `idMantenimiento` INT NOT NULL,
  `idServicio` INT NOT NULL,
  `fechaInicio` DATE NULL DEFAULT NULL,
  `fechaFin` DATE NULL DEFAULT NULL,
  `horaInicio` TIME NULL DEFAULT NULL,
  `horaFin` TIME NULL DEFAULT NULL,
  PRIMARY KEY (`idMantenimiento`),
  INDEX `fk_Mantenimiento_Servicio1_idx` (`idServicio` ASC) VISIBLE,
  CONSTRAINT `fk_Mantenimiento_Servicio1`
    FOREIGN KEY (`idServicio`)
    REFERENCES `GestionDeportiva`.`servicio` (`idServicio`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`tarifa`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`tarifa` (
  `idTarifa` INT NOT NULL AUTO_INCREMENT,
  `idServicio` INT NOT NULL,
  `tipoServicio` VARCHAR(45) NULL DEFAULT NULL,
  `diaSemana` VARCHAR(45) NULL DEFAULT NULL,
  `horaInicio` TIME NULL DEFAULT NULL,
  `horaFin` TIME NULL DEFAULT NULL,
  `monto` DECIMAL NULL DEFAULT NULL,
  PRIMARY KEY (`idTarifa`),
  INDEX `fk_Prices_Services1_idx` (`idServicio` ASC) VISIBLE,
  CONSTRAINT `fk_Prices_Services1`
    FOREIGN KEY (`idServicio`)
    REFERENCES `GestionDeportiva`.`servicio` (`idServicio`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `GestionDeportiva`.`validacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `GestionDeportiva`.`validacion` (
  `idValidacion` INT NOT NULL AUTO_INCREMENT,
  `timeStampValidacion` DATETIME NULL DEFAULT NULL,
  `latitudCoordinador` DECIMAL(10,0) NULL DEFAULT NULL,
  `longitudCoordinador` DECIMAL(10,0) NULL DEFAULT NULL,
  `distanciaError` DECIMAL(10,0) NULL DEFAULT NULL,
  `resultado` VARCHAR(45) NULL DEFAULT NULL,
  `idHorario` INT NOT NULL,
  PRIMARY KEY (`idValidacion`),
  INDEX `fk_Validacion_Horario1_idx` (`idHorario` ASC) VISIBLE,
  CONSTRAINT `fk_Validacion_Horario1`
    FOREIGN KEY (`idHorario`)
    REFERENCES `GestionDeportiva`.`horario` (`idHorario`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
