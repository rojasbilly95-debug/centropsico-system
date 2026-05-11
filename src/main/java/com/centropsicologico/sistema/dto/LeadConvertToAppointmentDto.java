package com.centropsicologico.sistema.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeadConvertToAppointmentDto {

    private Long patientId;

    private String reason;

    private String observation;

    private String registeredBy;
}