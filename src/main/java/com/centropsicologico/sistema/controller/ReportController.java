package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.dto.MonthlyReportDto;
import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.entity.Expense;
import com.centropsicologico.sistema.entity.Income;
import com.centropsicologico.sistema.enums.AppointmentStatus;
import com.centropsicologico.sistema.repository.AppointmentRepository;
import com.centropsicologico.sistema.repository.ExpenseRepository;
import com.centropsicologico.sistema.repository.IncomeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final AppointmentRepository appointmentRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    public ReportController(
            AppointmentRepository appointmentRepository,
            IncomeRepository incomeRepository,
            ExpenseRepository expenseRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
    }

    @GetMapping("/monthly")
    public MonthlyReportDto getMonthlyReport(
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(a -> !a.getDate().isBefore(startDate) && !a.getDate().isAfter(endDate))
                .toList();

        Long totalAppointments = (long) appointments.size();

        Long attendedAppointments = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.ATENDIDA)
                .count();

        Long cancelledAppointments = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELADA)
                .count();

        Long noShowAppointments = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.NO_ASISTIO)
                .count();

        List<Income> incomes = incomeRepository.findByDateBetweenAndActiveTrue(startDate, endDate);
        List<Expense> expenses = expenseRepository.findByDateBetweenAndActiveTrue(startDate, endDate);

        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal profit = totalIncome.subtract(totalExpense);

        String result = profit.compareTo(BigDecimal.ZERO) >= 0 ? "GANANCIA" : "PÉRDIDA";

        return new MonthlyReportDto(
                totalAppointments,
                attendedAppointments,
                cancelledAppointments,
                noShowAppointments,
                totalIncome,
                totalExpense,
                profit,
                result
        );
    }
}