package com.centropsicologico.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {

    private Long totalPatients;
    private Long todayAppointments;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal profit;

    private Long pendingPayments;
    private Long pendingLeads;
    private Long totalAppointmentsMonth;

    private List<StatusItemDto> appointmentStatus;
    private FinanceWeeksDto financeWeeks;
    private List<UpcomingAppointmentDto> upcomingAppointments;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusItemDto {
        private String label;
        private Long value;
        private String color;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinanceWeeksDto {
        private List<String> labels;
        private List<BigDecimal> income;
        private List<BigDecimal> expense;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingAppointmentDto {
        private Long id;
        private String patient;
        private String service;
        private String psychologist;
        private String date;
        private String time;
        private String status;
        private String initials;
    }
}