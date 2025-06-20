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
    public byte[] generarReportesGeneral(List<?> datos, String PathReporte, Map<String, Object> parametrosPersonalizados) {
        try {
            System.out.println("=== INICIANDO GENERACIÓN DE REPORTE ===");
            System.out.println("Path del reporte: " + PathReporte);
            System.out.println("Cantidad de datos: " + (datos != null ? datos.size() : "null"));

            // Cargar el archivo .jasper precompilado desde resources
            InputStream jasperStream = getClass().getResourceAsStream(PathReporte);
            if (jasperStream == null) {
                System.err.println("ERROR: No se pudo cargar el archivo jasper: " + PathReporte);
                return null;
            }

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
            System.out.println("Reporte jasper cargado exitosamente");

            // Cargar imágenes desde /resources/imagenes/
            Map<String, Object> parametros = new HashMap<>();

            // Verificar y cargar cada imagen
            InputStream logoStream = getClass().getResourceAsStream("/imagenes/logo-muni-2023-responsive.png");
            InputStream watermarkStream = getClass().getResourceAsStream("/imagenes/watermarkSANM.png");
            InputStream forma1Stream = getClass().getResourceAsStream("/imagenes/forma1.png");
            InputStream forma2Stream = getClass().getResourceAsStream("/imagenes/forma2.png");

            if (logoStream != null) {
                parametros.put("logomuni", logoStream);
                System.out.println("Logo cargado exitosamente");
            } else {
                System.err.println("WARNING: No se pudo cargar el logo");
            }

            if (watermarkStream != null) {
                parametros.put("watermark", watermarkStream);
                System.out.println("Watermark cargado exitosamente");
            } else {
                System.err.println("WARNING: No se pudo cargar el watermark");
            }

            if (forma1Stream != null) {
                parametros.put("forma1", forma1Stream);
                System.out.println("Forma1 cargada exitosamente");
            } else {
                System.err.println("WARNING: No se pudo cargar forma1");
            }

            if (forma2Stream != null) {
                parametros.put("forma2", forma2Stream);
                System.out.println("Forma2 cargada exitosamente");
            } else {
                System.err.println("WARNING: No se pudo cargar forma2");
            }

            // Agregar los parámetros personalizados (nombre, cargo, etc.)
            if (parametrosPersonalizados != null) {
                parametros.putAll(parametrosPersonalizados);
            }

            // Cargar datos del reporte
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(datos);
            System.out.println("DataSource creado con " + datos.size() + " registros");

            // Llenar el reporte con los datos y parámetros
            System.out.println("Llenando el reporte...");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);
            System.out.println("Reporte llenado exitosamente");

            // Exportar a PDF como arreglo de bytes
            System.out.println("Exportando a PDF...");
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            System.out.println("PDF generado exitosamente. Tamaño: " + (pdfBytes != null ? pdfBytes.length + " bytes" : "null"));

            return pdfBytes;

        } catch (Exception e) {
            System.err.println("ERROR al generar el reporte de reservas: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
