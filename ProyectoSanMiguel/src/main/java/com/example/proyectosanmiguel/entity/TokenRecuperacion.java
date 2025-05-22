package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name="tokenRecuperacion")
public class TokenRecuperacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String token;

    @ManyToOne
    @JoinColumn(name = "idUsuario")
    private Usuario usuario;

    private LocalDateTime expiracion;
    private boolean usado;

}
