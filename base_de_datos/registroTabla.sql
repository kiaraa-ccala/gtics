CREATE TABLE registro_asistencia (
    id_registro INT AUTO_INCREMENT PRIMARY KEY,
    id_coordinador INT NOT NULL,
    id_complejo INT NOT NULL,
    tipo_registro ENUM('entrada', 'salida') NOT NULL,
    fecha_hora DATETIME NOT NULL,
    
    -- COORDENADAS DEL COORDINADOR (no del complejo)
    latitud_coordinador DECIMAL(10, 8),
    longitud_coordinador DECIMAL(11, 8),
    precision_metros DOUBLE,
    distancia_complejo DOUBLE,
    
    validado BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_coordinador) REFERENCES usuario(idUsuario) ON DELETE CASCADE,
    FOREIGN KEY (id_complejo) REFERENCES complejodeportivo(idComplejoDeportivo) ON DELETE CASCADE,
    
    INDEX idx_coordinador_fecha (id_coordinador, fecha_hora),
    INDEX idx_tipo_registro (tipo_registro)
);

-- Agregar la columna es_tardanza a la tabla registro_asistencia
ALTER TABLE registro_asistencia 
ADD COLUMN es_tardanza BOOLEAN DEFAULT FALSE 
AFTER validado;

-- Verificar que la columna se agregó correctamente
DESCRIBE registro_asistencia;
