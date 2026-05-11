package com.centropsicologico.sistema.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "gasto")
@Getter
@Setter
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gasto")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_categoria_gasto")
    private ExpenseCategory category;

    @Column(name = "descripcion", nullable = false, length = 150)
    private String description;

    @Column(name = "monto", nullable = false)
    private BigDecimal amount;

    @Column(name = "fecha", nullable = false)
    private LocalDate date;

    @Column(name = "responsable", length = 100)
    private String responsible;

    @Column(name = "estado", nullable = false)
    private Boolean active = true;
}