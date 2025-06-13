-- Script para insertar datos de prueba para el sistema de cupones/descuentos
-- Ejecutar después de crear la base de datos principal

USE gestiondeportiva;

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

-- Verificar que se insertaron correctamente
SELECT 
    d.codigo,
    d.tipoDescuento,
    d.valor,
    d.fechaInicio,
    d.fechaFinal,
    s.nombre as servicio
FROM descuento d
LEFT JOIN servicio s ON d.idServicio = s.idServicio
ORDER BY d.fechaInicio DESC;
