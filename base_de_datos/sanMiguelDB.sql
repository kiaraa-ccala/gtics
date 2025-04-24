-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `mydb` ;

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `mydb` DEFAULT CHARACTER SET utf8 ;
USE `mydb` ;

-- -----------------------------------------------------
-- Table `mydb`.`Sector`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Sector` (
  `idSector` INT NOT NULL,
  `nombre` VARCHAR(45) NULL,
  PRIMARY KEY (`idSector`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Tercerizado`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Tercerizado` (
  `idTercerizado` INT NOT NULL AUTO_INCREMENT,
  `ruc` VARCHAR(11) NULL,
  `direccionFiscal` VARCHAR(45) NULL,
  PRIMARY KEY (`idTercerizado`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Foto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Foto` (
  `idFoto` INT NOT NULL AUTO_INCREMENT,
  `nombreFoto` VARCHAR(80) NULL,
  `foto` LONGBLOB NULL,
  `urlFoto` VARCHAR(100) NULL,
  PRIMARY KEY (`idFoto`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Usuario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Usuario` (
  `idUsuario` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NULL,
  `apellido` VARCHAR(45) NULL,
  `dni` VARCHAR(8) NULL,
  `direccion` VARCHAR(45) NULL,
  `distrito` VARCHAR(45) NULL,
  `provincia` VARCHAR(45) NULL,
  `departamento` VARCHAR(45) NULL,
  `idSector` INT NULL,
  `idTercerizado` INT NULL,
  `idFoto` INT NOT NULL,
  PRIMARY KEY (`idUsuario`),
  INDEX `fk_usuarios_sector_idx` (`idSector` ASC) VISIBLE,
  INDEX `fk_usuarios_outsourced1_idx` (`idTercerizado` ASC) VISIBLE,
  INDEX `fk_Usuario_Foto1_idx` (`idFoto` ASC) VISIBLE,
  CONSTRAINT `fk_usuarios_sector`
    FOREIGN KEY (`idSector`)
    REFERENCES `mydb`.`Sector` (`idSector`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_usuarios_outsourced1`
    FOREIGN KEY (`idTercerizado`)
    REFERENCES `mydb`.`Tercerizado` (`idTercerizado`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Usuario_Foto1`
    FOREIGN KEY (`idFoto`)
    REFERENCES `mydb`.`Foto` (`idFoto`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Credencial`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Credencial` (
  `idCredencial` INT NOT NULL AUTO_INCREMENT,
  `correo` VARCHAR(45) NULL,
  `password` VARCHAR(45) NULL,
  `idUsuario` INT NOT NULL,
  PRIMARY KEY (`idCredencial`),
  INDEX `fk_credentials_usuarios1_idx` (`idUsuario` ASC) VISIBLE,
  CONSTRAINT `fk_credentials_usuarios1`
    FOREIGN KEY (`idUsuario`)
    REFERENCES `mydb`.`Usuario` (`idUsuario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Roles`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Roles` (
  `idRol` INT NOT NULL,
  `nombre` VARCHAR(45) NULL,
  PRIMARY KEY (`idRol`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`RolUsuario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`RolUsuario` (
  `idRolUsuario` INT NOT NULL AUTO_INCREMENT,
  `idUsuario` INT NOT NULL,
  `idRol` INT NOT NULL,
  PRIMARY KEY (`idRolUsuario`),
  INDEX `fk_usuarios_has_roles_roles1_idx` (`idRol` ASC) VISIBLE,
  INDEX `fk_usuarios_has_roles_usuarios1_idx` (`idUsuario` ASC) VISIBLE,
  CONSTRAINT `fk_usuarios_has_roles_usuarios1`
    FOREIGN KEY (`idUsuario`)
    REFERENCES `mydb`.`Usuario` (`idUsuario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_usuarios_has_roles_roles1`
    FOREIGN KEY (`idRol`)
    REFERENCES `mydb`.`Roles` (`idRol`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Servicio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Servicio` (
  `idServicio` INT NOT NULL,
  `nombre` VARCHAR(60) NULL,
  PRIMARY KEY (`idServicio`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`ComplejoDeportivo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`ComplejoDeportivo` (
  `idComplejoDeportivo` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NULL,
  `direccion` VARCHAR(45) NULL,
  `idSector` INT NOT NULL,
  `numeroSoporte` VARCHAR(45) NULL,
  `latitud` DECIMAL NULL,
  `longitud` DECIMAL NULL,
  PRIMARY KEY (`idComplejoDeportivo`),
  INDEX `fk_SportLocation_sector1_idx` (`idSector` ASC) VISIBLE,
  CONSTRAINT `fk_SportLocation_sector1`
    FOREIGN KEY (`idSector`)
    REFERENCES `mydb`.`Sector` (`idSector`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`InstanciaServicio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`InstanciaServicio` (
  `idInstanciaServicio` INT NOT NULL,
  `idServicio` INT NOT NULL,
  `idComplejoDeportivo` INT NOT NULL,
  `nombre` VARCHAR(45) NULL,
  `capacidadMaxima` VARCHAR(45) NULL,
  `modoAcceso` VARCHAR(45) NULL,
  PRIMARY KEY (`idServicio`, `idComplejoDeportivo`, `idInstanciaServicio`),
  INDEX `fk_Services_has_SportLocation_SportLocation1_idx` (`idComplejoDeportivo` ASC) VISIBLE,
  INDEX `fk_Services_has_SportLocation_Services1_idx` (`idServicio` ASC) VISIBLE,
  CONSTRAINT `fk_Services_has_SportLocation_Services1`
    FOREIGN KEY (`idServicio`)
    REFERENCES `mydb`.`Servicio` (`idServicio`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Services_has_SportLocation_SportLocation1`
    FOREIGN KEY (`idComplejoDeportivo`)
    REFERENCES `mydb`.`ComplejoDeportivo` (`idComplejoDeportivo`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`InformacionPago`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`InformacionPago` (
  `idInformacionPago` INT NOT NULL,
  `fecha` DATE NULL,
  `hora` TIME NULL,
  `tipo` VARCHAR(45) NULL,
  `total` DECIMAL NULL,
  `estado` VARCHAR(45) NULL,
  PRIMARY KEY (`idInformacionPago`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Reserva`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Reserva` (
  `idReserva` INT NOT NULL AUTO_INCREMENT,
  `idUsuario` INT NOT NULL,
  `idInformacionPago` INT NOT NULL,
  `fecha` DATE NULL,
  `horaInicio` TIME NULL,
  `horaFin` TIME NULL,
  `estado` TINYINT NULL,
  `fechaHoraRegistro` DATETIME NULL,
  `idServicio` INT NOT NULL,
  `idComplejoDeportivo` INT NOT NULL,
  `idInstanciaServicio` INT NOT NULL,
  PRIMARY KEY (`idReserva`),
  INDEX `fk_ServiceAccess_Users1_idx` (`idUsuario` ASC) VISIBLE,
  INDEX `fk_ServiceAccess_PaymentInfo1_idx` (`idInformacionPago` ASC) VISIBLE,
  INDEX `fk_Reserva_InstanciaServicio1_idx` (`idServicio` ASC, `idComplejoDeportivo` ASC, `idInstanciaServicio` ASC) VISIBLE,
  CONSTRAINT `fk_ServiceAccess_Users1`
    FOREIGN KEY (`idUsuario`)
    REFERENCES `mydb`.`Usuario` (`idUsuario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_ServiceAccess_PaymentInfo1`
    FOREIGN KEY (`idInformacionPago`)
    REFERENCES `mydb`.`InformacionPago` (`idInformacionPago`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Reserva_InstanciaServicio1`
    FOREIGN KEY (`idServicio` , `idComplejoDeportivo` , `idInstanciaServicio`)
    REFERENCES `mydb`.`InstanciaServicio` (`idServicio` , `idComplejoDeportivo` , `idInstanciaServicio`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Tarifa`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Tarifa` (
  `idTarifa` INT NOT NULL AUTO_INCREMENT,
  `idServicio` INT NOT NULL,
  `tipoServicio` VARCHAR(45) NULL,
  `diaSemana` VARCHAR(45) NULL,
  `horaInicio` TIME NULL,
  `horaFin` TIME NULL,
  `monto` DECIMAL NULL,
  PRIMARY KEY (`idTarifa`),
  INDEX `fk_Prices_Services1_idx` (`idServicio` ASC) VISIBLE,
  CONSTRAINT `fk_Prices_Services1`
    FOREIGN KEY (`idServicio`)
    REFERENCES `mydb`.`Servicio` (`idServicio`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Descuento`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Descuento` (
  `idDescuento` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(45) NULL,
  `tipoDescuento` VARCHAR(45) NULL,
  `valor` DECIMAL NULL,
  `fechaInicio` DATE NULL,
  `fechaFinal` DATE NULL,
  `idServicio` INT NOT NULL,
  PRIMARY KEY (`idDescuento`),
  INDEX `fk_Discount_Services1_idx` (`idServicio` ASC) VISIBLE,
  CONSTRAINT `fk_Discount_Services1`
    FOREIGN KEY (`idServicio`)
    REFERENCES `mydb`.`Servicio` (`idServicio`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Mantenimiento`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Mantenimiento` (
  `idMantenimiento` INT NOT NULL,
  `idServicio` INT NOT NULL,
  `fechaInicio` DATE NULL,
  `fechaFin` DATE NULL,
  `horaInicio` TIME NULL,
  `horaFin` TIME NULL,
  PRIMARY KEY (`idMantenimiento`),
  INDEX `fk_Mantenimiento_Servicio1_idx` (`idServicio` ASC) VISIBLE,
  CONSTRAINT `fk_Mantenimiento_Servicio1`
    FOREIGN KEY (`idServicio`)
    REFERENCES `mydb`.`Servicio` (`idServicio`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`HorarioSemanal`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`HorarioSemanal` (
  `idHorarioSemanal` INT NOT NULL AUTO_INCREMENT,
  `idAdministrador` INT NOT NULL,
  `idCoordinador` INT NOT NULL,
  `fechaInicio` DATE NULL,
  `fechaFin` DATE NULL,
  `fechaCreacion` DATE NULL,
  PRIMARY KEY (`idHorarioSemanal`, `idAdministrador`, `idCoordinador`),
  INDEX `fk_HorarioSemanal_Usuario1_idx` (`idAdministrador` ASC) VISIBLE,
  INDEX `fk_HorarioSemanal_Usuario2_idx` (`idCoordinador` ASC) VISIBLE,
  CONSTRAINT `fk_HorarioSemanal_Usuario1`
    FOREIGN KEY (`idAdministrador`)
    REFERENCES `mydb`.`Usuario` (`idUsuario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_HorarioSemanal_Usuario2`
    FOREIGN KEY (`idCoordinador`)
    REFERENCES `mydb`.`Usuario` (`idUsuario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Horario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Horario` (
  `idHorario` INT NOT NULL AUTO_INCREMENT,
  `idHorarioSemanal` INT NOT NULL,
  `idAdministrador` INT NOT NULL,
  `idCoordinador` INT NOT NULL,
  `idComplejoDeportivo` INT NOT NULL,
  `fecha` DATE NULL,
  `horaIngreso` TIME NULL,
  `horaSalida` TIME NULL,
  PRIMARY KEY (`idHorario`, `idHorarioSemanal`, `idAdministrador`, `idCoordinador`, `idComplejoDeportivo`),
  INDEX `fk_Horario_HorarioSemanal1_idx` (`idHorarioSemanal` ASC, `idAdministrador` ASC, `idCoordinador` ASC) VISIBLE,
  INDEX `fk_Horario_ComplejoDeportivo1_idx` (`idComplejoDeportivo` ASC) VISIBLE,
  CONSTRAINT `fk_Horario_HorarioSemanal1`
    FOREIGN KEY (`idHorarioSemanal` , `idAdministrador` , `idCoordinador`)
    REFERENCES `mydb`.`HorarioSemanal` (`idHorarioSemanal` , `idAdministrador` , `idCoordinador`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Horario_ComplejoDeportivo1`
    FOREIGN KEY (`idComplejoDeportivo`)
    REFERENCES `mydb`.`ComplejoDeportivo` (`idComplejoDeportivo`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Validacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Validacion` (
  `idValidacion` INT NOT NULL AUTO_INCREMENT,
  `timeStampValidacion` DATETIME NULL,
  `latitudCoordinador` DECIMAL NULL,
  `longitudCoordinador` DECIMAL NULL,
  `distanciaError` DECIMAL NULL,
  `resultado` VARCHAR(45) NULL,
  `idHorario` INT NOT NULL,
  PRIMARY KEY (`idValidacion`),
  INDEX `fk_Validacion_Horario1_idx` (`idHorario` ASC) VISIBLE,
  CONSTRAINT `fk_Validacion_Horario1`
    FOREIGN KEY (`idHorario`)
    REFERENCES `mydb`.`Horario` (`idHorario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Reporte`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Reporte` (
  `idReporte` INT NOT NULL AUTO_INCREMENT,
  `tipoReporte` VARCHAR(45) NULL,
  `fechaRecepcion` DATE NULL,
  `estado` VARCHAR(45) NULL,
  `asunto` VARCHAR(100) NULL,
  `descripcion` VARCHAR(500) NULL,
  `respuesta` VARCHAR(500) NULL,
  `idReserva` INT NULL,
  `idHorario` INT NOT NULL,
  `idHorarioSemanal` INT NOT NULL,
  `Horario_idAdministrador` INT NOT NULL,
  `idCoordinador` INT NOT NULL,
  `idComplejoDeportivo` INT NOT NULL,
  PRIMARY KEY (`idReporte`),
  INDEX `fk_Reporte_Reserva1_idx` (`idReserva` ASC) VISIBLE,
  INDEX `fk_Reporte_Horario1_idx` (`idHorario` ASC, `idHorarioSemanal` ASC, `Horario_idAdministrador` ASC, `idCoordinador` ASC, `idComplejoDeportivo` ASC) VISIBLE,
  CONSTRAINT `fk_Reporte_Reserva1`
    FOREIGN KEY (`idReserva`)
    REFERENCES `mydb`.`Reserva` (`idReserva`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Reporte_Horario1`
    FOREIGN KEY (`idHorario` , `idHorarioSemanal` , `Horario_idAdministrador` , `idCoordinador` , `idComplejoDeportivo`)
    REFERENCES `mydb`.`Horario` (`idHorario` , `idHorarioSemanal` , `idAdministrador` , `idCoordinador` , `idComplejoDeportivo`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Comentario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Comentario` (
  `idComentario` INT NOT NULL AUTO_INCREMENT,
  `fechaHora` DATETIME NULL,
  `contenido` VARCHAR(300) NULL,
  `idReporte` INT NOT NULL,
  PRIMARY KEY (`idComentario`),
  INDEX `fk_Comentario_Reporte1_idx` (`idReporte` ASC) VISIBLE,
  CONSTRAINT `fk_Comentario_Reporte1`
    FOREIGN KEY (`idReporte`)
    REFERENCES `mydb`.`Reporte` (`idReporte`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Evidencia`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Evidencia` (
  `idEvidencia` INT NOT NULL AUTO_INCREMENT,
  `nombreArchivo` VARCHAR(100) NULL,
  `urlArchivo` VARCHAR(100) NULL,
  `archivo` LONGBLOB NULL,
  `idReporte` INT NOT NULL,
  PRIMARY KEY (`idEvidencia`),
  INDEX `fk_Evidencia_Reporte1_idx` (`idReporte` ASC) VISIBLE,
  CONSTRAINT `fk_Evidencia_Reporte1`
    FOREIGN KEY (`idReporte`)
    REFERENCES `mydb`.`Reporte` (`idReporte`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
