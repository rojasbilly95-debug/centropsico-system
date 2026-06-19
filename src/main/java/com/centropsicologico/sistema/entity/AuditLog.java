package com.centropsicologico.sistema.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_movimiento")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long id;

    @Column(name = "modulo", nullable = false, length = 80)
    private String module;

    @Column(name = "accion", nullable = false, length = 120)
    private String action;

    @Column(name = "entidad", length = 120)
    private String entityName;

    @Column(name = "id_entidad")
    private Long entityId;

    @Column(name = "descripcion", nullable = false, length = 700)
    private String description;

    @Column(name = "usuario_correo", length = 150)
    private String userEmail;

    @Column(name = "usuario_rol", length = 60)
    private String userRole;

    @Column(name = "nivel", nullable = false, length = 20)
    private String severity = "INFO";

    @Column(name = "revisado", nullable = false)
    private Boolean reviewed = false;

    @Column(name = "notificado_admin", nullable = false)
    private Boolean adminNotified = false;

    @Column(name = "importante", nullable = false)
    private Boolean important = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "estado", nullable = false)
    private Boolean active = true;
}