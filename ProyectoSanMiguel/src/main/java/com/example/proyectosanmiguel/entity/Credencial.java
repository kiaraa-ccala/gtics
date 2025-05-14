package com.example.proyectosanmiguel.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "credencial")
public class Credencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCredencial", nullable = false)
    private Integer idCredencial;

    @Column(name = "correo", nullable = false, length = 45)
    private String correo;

    @Column(name = "password", nullable = false, length = 45)
    private String password;

    @OneToOne
    @JoinColumn(name = "idUsuario")
    private Usuario usuario;

}
