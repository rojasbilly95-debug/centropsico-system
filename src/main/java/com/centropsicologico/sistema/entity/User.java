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

    /*
     * Campo antiguo. Se mantiene para no romper código existente.
     * Ya no se usará para guardar fotos en Render.
     */
    @Column(name = "foto_perfil", length = 255)
    private String profileImageUrl;

    /*
     * Nueva forma de guardar la foto de perfil.
     * La imagen se almacena en Base64 dentro de la base de datos Aiven MySQL.
     * Así no se pierde cuando Render redepliega el proyecto.
     */
    @Lob
    @Column(name = "profile_image_base64", columnDefinition = "LONGTEXT")
    private String profileImageBase64;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.active == null) {
            this.active = true;
        }

        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}