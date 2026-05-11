package com.centropsicologico.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class DashboardDto {

    private Long totalPatients;
    private Long todayAppointments;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal profit;
}