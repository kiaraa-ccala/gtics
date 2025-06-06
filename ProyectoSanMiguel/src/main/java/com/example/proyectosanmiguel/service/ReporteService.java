package com.example.proyectosanmiguel.service;


import net.sf.jasperreports.engine.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReporteService {

    public byte[] generarReporteSanMiguel() throws JRException {
        // 1. Cargar el archivo .jrxml desde resources/reportes/
        InputStream jrxmlStream = getClass().getResourceAsStream("/reportes/ReporteSanMiguelPUCP.jrxml");

        // 2. Compilar el .jrxml a JasperReport (esto puede hacerse en tiempo de ejecución o previamente a .jasper)
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

        // 3. Cargar imágenes desde resources/imagenes/
        Map<String, Object> params = new HashMap<>();
        params.put("logomuni", getClass().getResourceAsStream("/imagenes/logo-muni-2023-responsive.png"));
        params.put("forma1", getClass().getResourceAsStream("/imagenes/forma1.png"));
        params.put("forma2", getClass().getResourceAsStream("/imagenes/forma2.png"));
        params.put("watermark", getClass().getResourceAsStream("/imagenes/watermarkSANM.png"));

        // 4. Fuente de datos (puede ser vacía o una lista de objetos)
        JRDataSource dataSource = new JREmptyDataSource(1);

        // 5. Llenar el reporte
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

        // 6. Exportar a PDF
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}
