package com.centropsicologico.sistema.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "psicologo")
@Getter
@Setter
public class Psychologist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_psicologo")
    private Long id;

    @Column(name = "nombres", nullable = false)
    private String firstName;

    @Column(name = "apellidos", nullable = false)
    private String lastName;

    @Column(name = "especialidad")
    private String specialty;

    @Column(name = "telefono")
    private String phone;

    @Column(name = "correo")
    private String email;

    @Column(name = "estado")
    private Boolean active = true;

    @Transient
    private Long availabilityCount;
}