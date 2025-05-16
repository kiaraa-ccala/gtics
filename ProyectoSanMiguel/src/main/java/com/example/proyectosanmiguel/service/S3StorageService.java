package com.example.proyectosanmiguel.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3StorageService {

    /**
     * Sube un archivo a S3 (o al sistema local en modo dev), en la ruta lógica especificada.
     *
     * @param file       Archivo a subir
     * @param pathPrefix Ruta lógica donde se almacenará (ej: "reportes/usuario/12/")
     * @return key generado o ruta local del archivo
     * @throws IOException si hay error al guardar el archivo
     */
    String upload(MultipartFile file, String nombreDestino) throws IOException;
}