package com.centropsicologico.sistema.entity;

import com.centropsicologico.sistema.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "cita")
@Getter
@Setter
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "id_psicologo", nullable = false)
    private Psychologist psychologist;

    @ManyToOne
    @JoinColumn(name = "id_servicio", nullable = false)
    private ServiceEntity service;

    @Column(name = "fecha", nullable = false)
    private LocalDate date;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime startTime;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cita", nullable = false)
    private AppointmentStatus status;

    @Column(name = "motivo_consulta")
    private String reason;

    @Column(name = "observacion")
    private String observation;

    @Column(name = "pagado", nullable = false)
    private Boolean paid = false;

    @Column(name = "monto_pagado")
    private BigDecimal paidAmount;

    @Column(name = "monto_total")
    private BigDecimal totalAmount;

    @Column(name = "saldo_pendiente")
    private BigDecimal pendingAmount;

    @Column(name = "estado_pago", length = 30)
    private String paymentStatus;

    @Column(name = "metodo_pago", length = 50)
    private String paymentMethod;

    @Column(name = "fecha_pago")
    private LocalDate paymentDate;

    @Column(name = "fecha_hora_pago")
    private LocalDateTime paymentDateTime;

    @Column(name = "codigo_operacion", length = 100)
    private String operationCode;

    @Column(name = "observacion_pago", length = 255)
    private String paymentObservation;

    @Column(name = "registrado_por", length = 120)
    private String paymentRegisteredBy;
}