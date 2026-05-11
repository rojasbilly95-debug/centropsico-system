package com.centropsicologico.sistema.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "interesado")
@Getter
@Setter
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interesado")
    private Long id;

    @Column(name = "nombres", nullable = false, length = 120)
    private String fullName;

    @Column(name = "correo", nullable = false, length = 120)
    private String email;

    @Column(name = "telefono", length = 30)
    private String phone;

    @Column(name = "tipo_atencion", length = 150)
    private String serviceInterest;

    @Column(name = "id_servicio")
    private Long serviceId;

    @Column(name = "modalidad", length = 50)
    private String modality;

    @Column(name = "psicologo_preferido", length = 150)
    private String psychologistName;

    @Column(name = "id_psicologo")
    private Long psychologistId;

    @Column(name = "fecha_preferida", length = 20)
    private String preferredDate;

    @Column(name = "hora_preferida", length = 20)
    private String preferredTime;

    @Column(name = "precio_servicio")
    private Double servicePrice;

    @Column(name = "porcentaje_adelanto")
    private Double advancePercent;

    @Column(name = "monto_adelanto")
    private Double advanceAmount;

    @Column(name = "metodo_pago", length = 50)
    private String paymentMethod;

    @Column(name = "codigo_operacion", length = 100)
    private String operationCode;

    @Column(name = "estado_pago", length = 40)
    private String paymentStatus;

    @Column(name = "mensaje", length = 700)
    private String message;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "estado", nullable = false, length = 30)
    private String status = "NUEVO";

    @Column(name = "id_cita")
    private Long appointmentId;
}