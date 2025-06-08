package com.example.proyectosanmiguel.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "foto")
public class Foto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFoto", nullable = false)
    private Integer idFoto;

    @Column(name = "nombreFoto", nullable = false, length = 80)
    private String nombreFoto;


    // Guardar la foto como un arreglo de bytes
    //backup de la foto en caso de que no se pueda subir a S3
    @Lob
    @Column(name = "foto")
    private byte[] foto;

    // para no cambiar la BD, esta tambien sera la ruta logica del archivo en S3
    // en la BD se guardara el nombre del archivo, no la URL completa
    // este es key de S3, que es el nombre del archivo con su prefijo
    @Column(name = "urlFoto", nullable = false, length = 100)
    private String urlFoto;

}
