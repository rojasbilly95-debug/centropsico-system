package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.dto.DashboardDto;
import com.centropsicologico.sistema.repository.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    public DashboardController(
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            IncomeRepository incomeRepository,
            ExpenseRepository expenseRepository
    ) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
    }

    @GetMapping
    public DashboardDto getDashboard() {

        LocalDate today = LocalDate.now();

        Long totalPatients = patientRepository.count();

        Long todayAppointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getDate().equals(today))
                .count();

        BigDecimal totalIncome = incomeRepository.findAll().stream()
                .map(i -> i.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = expenseRepository.findAll().stream()
                .map(e -> e.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal profit = totalIncome.subtract(totalExpense);

        return new DashboardDto(
                totalPatients,
                todayAppointments,
                totalIncome,
                totalExpense,
                profit
        );
    }
}