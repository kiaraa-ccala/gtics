package com.example.proyectosanmiguel.service;


import com.example.proyectosanmiguel.entity.Foto;
import com.example.proyectosanmiguel.repository.FotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@Profile("dev")
public class FakeS3Service implements S3StorageService {


    @Autowired
    private FotoRepository fotoRepository;


    @Override
    public String upload(MultipartFile file, String nombreDestino) throws IOException {
        Foto f = new Foto();
        f.setNombreFoto(nombreDestino);
        f.setFoto(file.getBytes()); // guarda como LONGBLOB
        fotoRepository.save(f);
        return "guardado en base de datos con ID: " + f.getIdFoto();
    }
}
