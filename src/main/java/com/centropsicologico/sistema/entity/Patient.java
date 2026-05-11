package com.centropsicologico.sistema.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "paciente")
@Getter
@Setter
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Long id;

    @Column(name = "nombres", nullable = false, length = 100)
    private String firstName;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String lastName;

    @Column(name = "dni", nullable = false, unique = true, length = 15)
    private String dni;

    @Column(name = "fecha_nacimiento")
    private LocalDate birthDate;

    @Column(name = "sexo", length = 20)
    private String gender;

    @Column(name = "telefono", length = 20)
    private String phone;

    @Column(name = "correo", length = 120)
    private String email;

    @Column(name = "direccion", length = 150)
    private String address;

    @Column(name = "contacto_emergencia", length = 120)
    private String emergencyContact;

    @Column(name = "telefono_emergencia", length = 20)
    private String emergencyPhone;

    @Column(name = "estado", nullable = false)
    private Boolean active = true;
}