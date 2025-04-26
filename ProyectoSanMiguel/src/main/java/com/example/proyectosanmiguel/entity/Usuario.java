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
    @Column(name = "direccion", length = 45)
    private String direccion;
    @Column(name = "distrito", length = 45)
    private String distrito;
    @Column(name = "provincia", length = 45)
    private String provincia;
    @Column(name = "departamento", length = 45)
    private String departamento;
    @Column(name = "telefono", nullable = false, length = 9)
    private String telefono;
    @ManyToOne
    @JoinColumn(name="idSector")
    private Sector sector;
    @ManyToOne
    @JoinColumn(name = "idTercerizado")
    private Tercerizado tercerizado;
    @ManyToOne
    @JoinColumn(name = "idRol")
    private Rol rol;
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Credencial credencial;

}
