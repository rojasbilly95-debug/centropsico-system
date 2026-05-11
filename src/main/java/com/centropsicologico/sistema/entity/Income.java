package com.centropsicologico.sistema.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ingreso")
@Getter
@Setter
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ingreso")
    private Long id;

    @Column(name = "descripcion", nullable = false, length = 150)
    private String description;

    @Column(name = "monto", nullable = false)
    private BigDecimal amount;

    @Column(name = "fecha", nullable = false)
    private LocalDate date;

    @Column(name = "metodo_pago", length = 50)
    private String paymentMethod;

    @Column(name = "estado", nullable = false)
    private Boolean active = true;
}