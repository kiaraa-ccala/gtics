package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario", nullable = false)
    private Integer idUsuario;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 45, message = "El nombre no debe exceder 45 caracteres")
    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 45, message = "El apellido no debe exceder 45 caracteres")
    @Column(name = "apellido", nullable = false, length = 45)
    private String apellido;
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
    @Column(name = "dni", nullable = false, length = 8)
    private String dni;
    @Size(max = 45, message = "La dirección no debe exceder 45 caracteres")
    @Column(name = "direccion", length = 45)
    private String direccion;
    @Size(max = 45, message = "El distrito no debe exceder 45 caracteres")
    @Column(name = "distrito", length = 45)
    private String distrito;
    @Size(max = 45, message = "La provincia no debe exceder 45 caracteres")
    @Column(name = "provincia", length = 45)
    private String provincia;
    @Size(max = 45, message = "El departamento no debe exceder 45 caracteres")
    @Column(name = "departamento", length = 45)
    private String departamento;
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener 9 dígitos")
    @Column(name = "telefono", nullable = false, length = 9)
    private String telefono;
    @Column(name = "activo")
    private Integer activo;
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
    @OneToOne
    @JoinColumn(name = "idFoto")
    private Foto foto;
}
