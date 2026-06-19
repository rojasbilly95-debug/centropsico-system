package com.centropsicologico.sistema.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nombres", nullable = false, length = 100)
    private String firstName;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String lastName;

    @Column(name = "correo", nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "contrasena", nullable = false, length = 255)
    private String password;

    @Column(name = "rol", nullable = false, length = 50)
    private String role;

    @Column(name = "estado", nullable = false)
    private Boolean active = true;

    /*
     * Datos de perfil del usuario autenticado
     */
    @Column(name = "telefono", length = 30)
    private String phone;

    @Column(name = "foto_perfil", length = 255)
    private String profileImageUrl;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}