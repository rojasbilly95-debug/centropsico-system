package com.centropsicologico.sistema.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "promocion")
@Getter
@Setter
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_promocion")
    private Long id;

    @Column(name = "titulo", nullable = false, length = 150)
    private String title;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String description;

    @Column(name = "porcentaje_descuento")
    private Double discountPercent;

    @Column(name = "fecha_inicio")
    private LocalDate startDate;

    @Column(name = "fecha_fin")
    private LocalDate endDate;

    @Column(name = "estado", nullable = false)
    private Boolean active = true;
}