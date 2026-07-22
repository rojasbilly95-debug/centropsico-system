package com.centropsicologico.sistema.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    /*
     * Estado de revisión financiera:
     * PENDIENTE, REVISADO, CONTABILIZADO, OBSERVADO, ANULADO
     */
    @Column(name = "estado_revision", length = 30)
    private String reviewStatus = "PENDIENTE";

    /*
     * Usuario que revisó o validó el gasto.
     */
    @Column(name = "revisado_por", length = 120)
    private String reviewedBy;

    /*
     * Fecha y hora en que se revisó el gasto.
     */
    @Column(name = "fecha_revision")
    private LocalDateTime reviewedAt;

    /*
     * Observación escrita al momento de revisar el gasto.
     */
    @Column(name = "observacion_revision", length = 255)
    private String reviewObservation;

    /*
     * Origen del gasto:
     * MANUAL, OPERATIVO, SERVICIO, CAMPAÑA, OTRO
     */
    @Column(name = "origen", length = 80)
    private String origin;

    /*
     * Referencia del gasto:
     * comprobante, código, documento, observación breve, etc.
     */
    @Column(name = "referencia", length = 120)
    private String reference;

    @PrePersist
    public void prePersist() {
        if (this.active == null) {
            this.active = true;
        }

        if (this.reviewStatus == null || this.reviewStatus.trim().isEmpty()) {
            this.reviewStatus = "PENDIENTE";
        }

        if (this.origin == null || this.origin.trim().isEmpty()) {
            this.origin = "MANUAL";
        }
    }
}