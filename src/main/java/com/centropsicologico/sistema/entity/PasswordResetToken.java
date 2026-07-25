package com.centropsicologico.sistema.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "password_reset_token",
        indexes = {
                @Index(
                        name = "idx_password_reset_usuario",
                        columnList = "id_usuario"
                ),
                @Index(
                        name = "idx_password_reset_expiracion",
                        columnList = "fecha_expiracion"
                )
        }
)
@Getter
@Setter
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_password_reset")
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_usuario",
            nullable = false
    )
    private User user;

    /*
     * Nunca se almacena el token original.
     * Solo se guarda su hash SHA-256.
     */
    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String tokenHash;

    @Column(
            name = "fecha_creacion",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "fecha_expiracion",
            nullable = false
    )
    private LocalDateTime expiresAt;

    @Column(
            name = "utilizado",
            nullable = false
    )
    private boolean used = false;

    @Column(name = "fecha_uso")
    private LocalDateTime usedAt;

    @Column(
            name = "revocado",
            nullable = false
    )
    private boolean revoked = false;

    @Column(name = "fecha_revocacion")
    private LocalDateTime revokedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}