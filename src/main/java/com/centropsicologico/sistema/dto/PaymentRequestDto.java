package com.centropsicologico.sistema.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequestDto {
    private BigDecimal amount;
    private String method;
    private String operationCode;
    private String observation;
    private String registeredBy;
}