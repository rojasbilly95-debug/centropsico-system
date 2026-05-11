package com.centropsicologico.sistema.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long id;

    @Column(name = "titulo", nullable = false, length = 120)
    private String title;

    @Column(name = "mensaje", nullable = false, length = 500)
    private String message;

    @Column(name = "tipo", nullable = false, length = 50)
    private String type;

    @Column(name = "rol_destino", nullable = false, length = 50)
    private String targetRole;

    @Column(name = "correo_destino", length = 120)
    private String targetEmail;

    @Column(name = "leido", nullable = false)
    private Boolean read = false;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "estado", nullable = false)
    private Boolean active = true;
}