package com.centropsicologico.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class MonthlyReportDto {

    private Long totalAppointments;
    private Long attendedAppointments;
    private Long cancelledAppointments;
    private Long noShowAppointments;

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal profit;

    private String result;
}