package com.example.proyectosanmiguel.service;


import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    public byte[] generarReporteSanMiguel(List<?> datos) {
        try {
            // Cargar el archivo .jasper precompilado desde resources
            InputStream jasperStream = getClass().getResourceAsStream("/reportes/ReporteSanMiguelPUCP.jasper");
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);

            // Cargar imágenes desde /resources/imagenes/
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("logomuni", getClass().getResourceAsStream("/imagenes/logo-muni-2023-responsive.png"));
            parametros.put("forma1", getClass().getResourceAsStream("/imagenes/forma1.png"));
            parametros.put("forma2", getClass().getResourceAsStream("/imagenes/forma2.png"));
            parametros.put("watermark", getClass().getResourceAsStream("/imagenes/watermarkSANM.png"));

            // Cargar datos del reporte
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(datos);

            // Llenar el reporte con los datos y parámetros
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

            // Exportar a PDF como arreglo de bytes
            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            System.err.println("Error al generar el reporte San Miguel: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
