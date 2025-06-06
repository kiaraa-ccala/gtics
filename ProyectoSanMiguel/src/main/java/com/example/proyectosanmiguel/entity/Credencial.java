package com.example.proyectosanmiguel.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
    @Email(message = "El correo debe ser válido")
    @Size(max = 45, message = "El correo no debe exceder 45 caracteres")
    @Column(name = "correo", nullable = false, length = 45)
    private String correo;    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 45, message = "La contraseña debe tener entre 8 y 45 caracteres")
    @Pattern(regexp = "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\\s).*", message = "La contraseña debe tener al menos 1 mayúscula, 1 minúscula y 1 número")
    @Column(name = "password", nullable = false, length = 45)
    private String password;

    @OneToOne
    @JoinColumn(name = "idUsuario")
    private Usuario usuario;

}
