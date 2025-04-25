package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;
    @Column(name = "apellido", nullable = false, length = 45)
    private String apellido;
    @Column(name = "dni", nullable = false, length = 8)
    private String dni;
    @Column(name = "direccion", nullable = false, length = 45)
    private String direccion;
    @Column(name = "distrito", nullable = false, length = 45)
    private String distrito;
    @Column(name = "provincia", nullable = false, length = 45)
    private String provincia;
    @Column(name = "departamento", nullable = false, length = 45)
    private String departamento;
    @ManyToOne
    @JoinColumn(name="idSector")
    private Sector sector;
    @ManyToOne
    @JoinColumn(name = "idTercerizado")
    private Tercerizado tercerizado;
    @ManyToOne
    @JoinColumn(name = "idRol")
    private Rol rol;

}
