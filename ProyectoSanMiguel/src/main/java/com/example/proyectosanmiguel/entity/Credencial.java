package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "credencial")
public class Credencial implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCredencial", nullable = false)
    private Integer idCredencial;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Column(name = "correo", nullable = false, length = 45)
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(name = "password", nullable = false, length = 45)
    private String password;

    @OneToOne
    @JoinColumn(name = "idUsuario")
    private Usuario usuario;

}
