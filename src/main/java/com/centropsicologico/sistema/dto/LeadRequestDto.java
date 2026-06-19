package com.centropsicologico.sistema.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeadRequestDto {

    private String fullName;
    private String email;
    private String phone;

    private String serviceInterest;
    private Long serviceId;

    private String modality;

    private String psychologistName;
    private Long psychologistId;

    private String preferredDate;
    private String preferredTime;

    private Double servicePrice;
    private Double advancePercent;
    private Double advanceAmount;

    private String paymentMethod;
    private String operationCode;

    private String message;

    private Boolean consentAccepted;
    private String consentVersion;
}