package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.dto.MonthlyReportDto;
import com.centropsicologico.sistema.dto.PsychologistPerformanceReportDto;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @GetMapping("/psychologist-performance")
    public List<PsychologistPerformanceReportDto> getPsychologistPerformanceReport(
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam(required = false) Long psychologistId
    ) {
        if (year == null || month == null) {
            throw new RuntimeException("Debe ingresar año y mes");
        }

        if (month < 1 || month > 12) {
            throw new RuntimeException("El mes debe estar entre 1 y 12");
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Appointment> attendedAppointments;

        if (psychologistId != null) {
            attendedAppointments = appointmentRepository
                    .findByStatusAndDateBetweenAndPsychologistId(
                            AppointmentStatus.ATENDIDA,
                            startDate,
                            endDate,
                            psychologistId
                    );
        } else {
            attendedAppointments = appointmentRepository
                    .findByStatusAndDateBetween(
                            AppointmentStatus.ATENDIDA,
                            startDate,
                            endDate
                    );
        }

        attendedAppointments = attendedAppointments.stream()
                .filter(a -> a.getPsychologist() != null)
                .sorted(Comparator
                        .comparing((Appointment a) -> safe(a.getPsychologist().getLastName()))
                        .thenComparing(a -> safe(a.getPsychologist().getFirstName()))
                        .thenComparing(Appointment::getDate)
                )
                .toList();

        Map<Long, List<Appointment>> appointmentsByPsychologist = attendedAppointments.stream()
                .collect(Collectors.groupingBy(
                        appointment -> appointment.getPsychologist().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return appointmentsByPsychologist.values().stream()
                .map(this::buildPsychologistPerformance)
                .toList();
    }

    private PsychologistPerformanceReportDto buildPsychologistPerformance(List<Appointment> appointments) {
        Appointment firstAppointment = appointments.get(0);

        Long psychologistId = firstAppointment.getPsychologist().getId();

        String psychologistName = safe(firstAppointment.getPsychologist().getFirstName())
                + " "
                + safe(firstAppointment.getPsychologist().getLastName());

        psychologistName = psychologistName.trim();

        Long totalPatients = appointments.stream()
                .filter(a -> a.getPatient() != null)
                .map(a -> a.getPatient().getId())
                .distinct()
                .count();

        Long totalAppointments = (long) appointments.size();

        List<PsychologistPerformanceReportDto.TherapySummaryDto> therapies = appointments.stream()
                .filter(a -> a.getService() != null)
                .collect(Collectors.groupingBy(
                        a -> safe(a.getService().getName()).isBlank()
                                ? "Servicio sin nombre"
                                : safe(a.getService().getName()),
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> new PsychologistPerformanceReportDto.TherapySummaryDto(
                        entry.getKey(),
                        entry.getValue()
                ))
                .sorted(Comparator.comparing(
                        PsychologistPerformanceReportDto.TherapySummaryDto::getTotal
                ).reversed())
                .toList();

        return new PsychologistPerformanceReportDto(
                psychologistId,
                psychologistName,
                totalPatients,
                totalAppointments,
                therapies
        );
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}