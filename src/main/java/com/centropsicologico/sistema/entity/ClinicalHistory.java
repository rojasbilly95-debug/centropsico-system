package com.centropsicologico.sistema.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "historia_clinica")
@Getter
@Setter
public class ClinicalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historia")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Patient patient;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    @Column(name = "motivo", length = 255)
    private String reason;

    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "evolucion", columnDefinition = "TEXT")
    private String evolution;

    @Column(name = "recomendaciones", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "psicologo", length = 120)
    private String psychologistName;

    @Column(name = "estado", nullable = false)
    private Boolean active = true;
}