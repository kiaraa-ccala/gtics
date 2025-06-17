package com.example.proyectosanmiguel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "registro_asistencia")
public class RegistroAsistencia {
      @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private Integer idRegistro;
    
    @ManyToOne
    @JoinColumn(name = "id_coordinador", referencedColumnName = "idUsuario")
    private Usuario coordinador;
    
    @ManyToOne
    @JoinColumn(name = "id_complejo", referencedColumnName = "idComplejoDeportivo")
    private ComplejoDeportivo complejo;
    
    @Column(name = "tipo_registro")
    private String tipoRegistro; // 'entrada' o 'salida'
    
    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;
    
    @Column(name = "latitud_coordinador")
    private Double latitudCoordinador;
    
    @Column(name = "longitud_coordinador")
    private Double longitudCoordinador;
    
    @Column(name = "precision_metros")
    private Double precisionMetros;
    
    @Column(name = "distancia_complejo")
    private Double distanciaComplejo;
      @Column(name = "validado")
    private Boolean validado = true;
    
    @Column(name = "es_tardanza")
    private Boolean esTardanza = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}