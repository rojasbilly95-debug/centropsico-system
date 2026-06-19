package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.dto.DashboardDto;
import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.entity.Expense;
import com.centropsicologico.sistema.entity.Income;
import com.centropsicologico.sistema.entity.Lead;
import com.centropsicologico.sistema.enums.AppointmentStatus;
import com.centropsicologico.sistema.repository.AppointmentRepository;
import com.centropsicologico.sistema.repository.ExpenseRepository;
import com.centropsicologico.sistema.repository.IncomeRepository;
import com.centropsicologico.sistema.repository.LeadRepository;
import com.centropsicologico.sistema.repository.PatientRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

        private final PatientRepository patientRepository;
        private final AppointmentRepository appointmentRepository;
        private final IncomeRepository incomeRepository;
        private final ExpenseRepository expenseRepository;
        private final LeadRepository leadRepository;

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

        public DashboardController(
                        PatientRepository patientRepository,
                        AppointmentRepository appointmentRepository,
                        IncomeRepository incomeRepository,
                        ExpenseRepository expenseRepository,
                        LeadRepository leadRepository) {
                this.patientRepository = patientRepository;
                this.appointmentRepository = appointmentRepository;
                this.incomeRepository = incomeRepository;
                this.expenseRepository = expenseRepository;
                this.leadRepository = leadRepository;
        }

        @GetMapping
        public DashboardDto getDashboard(@RequestParam(defaultValue = "month") String period) {

                LocalDate today = LocalDate.now();

                DateRange range = resolveDateRange(period, today);
                LocalDate startDate = range.startDate();
                LocalDate endDate = range.endDate();

                List<Appointment> todayAppointmentsList = appointmentRepository.findByDate(today);
                List<Appointment> periodAppointments = appointmentRepository.findByDateBetween(startDate, endDate);

                List<Income> periodIncomes = incomeRepository.findByDateBetweenAndActiveTrue(startDate, endDate);
                List<Expense> periodExpenses = expenseRepository.findByDateBetweenAndActiveTrue(startDate, endDate);

                List<Lead> leads = leadRepository.findAll();

                Long totalPatients = patientRepository.count();
                Long todayAppointments = (long) todayAppointmentsList.size();

                BigDecimal totalIncome = periodIncomes.stream()
                                .map(Income::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalExpense = periodExpenses.stream()
                                .map(Expense::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal profit = totalIncome.subtract(totalExpense);

                Long pendingAppointmentPayments = periodAppointments.stream()
                                .filter(this::isAppointmentPaymentPending)
                                .count();

                Long pendingLeadPayments = leads.stream()
                                .filter(this::isLeadPaymentPending)
                                .count();

                Long pendingPayments = pendingAppointmentPayments + pendingLeadPayments;

                Long pendingLeads = leads.stream()
                                .filter(this::isLeadPending)
                                .count();

                List<DashboardDto.StatusItemDto> appointmentStatus = buildAppointmentStatus(periodAppointments);

                DashboardDto.FinanceWeeksDto financeWeeks = buildFinanceWeeks(
                                startDate,
                                endDate,
                                periodIncomes,
                                periodExpenses);

                List<DashboardDto.UpcomingAppointmentDto> upcomingAppointments = buildUpcomingAppointments(today);

                Long totalAppointmentsMonth = (long) periodAppointments.size();

                return new DashboardDto(
                                totalPatients,
                                todayAppointments,
                                totalIncome,
                                totalExpense,
                                profit,
                                pendingPayments,
                                pendingLeads,
                                totalAppointmentsMonth,
                                appointmentStatus,
                                financeWeeks,
                                upcomingAppointments);
        }

private List<DashboardDto.StatusItemDto> buildAppointmentStatus(List<Appointment> appointments) {
    return List.of(
            new DashboardDto.StatusItemDto(
                    "Programadas",
                    countStatus(appointments, AppointmentStatus.PROGRAMADA),
                    "#2563eb"
            ),
            new DashboardDto.StatusItemDto(
                    "Atendidas",
                    countStatus(appointments, AppointmentStatus.ATENDIDA),
                    "#047857"
            ),
            new DashboardDto.StatusItemDto(
                    "Canceladas",
                    countStatus(appointments, AppointmentStatus.CANCELADA),
                    "#b42318"
            ),
            new DashboardDto.StatusItemDto(
                    "No asistió",
                    countStatus(appointments, AppointmentStatus.NO_ASISTIO),
                    "#c2410c"
            ),
            new DashboardDto.StatusItemDto(
                    "Reprogramadas",
                    countStatus(appointments, AppointmentStatus.REPROGRAMADA),
                    "#475467"
            )
    );
}     

private Long countStatus(List<Appointment> appointments, AppointmentStatus status) {
                return appointments.stream()
                                .filter(appointment -> appointment.getStatus() == status)
                                .count();
        }

        private DashboardDto.FinanceWeeksDto buildFinanceWeeks(
                        LocalDate startDate,
                        LocalDate endDate,
                        List<Income> incomes,
                        List<Expense> expenses) {
                List<String> labels = new ArrayList<>();
                List<BigDecimal> incomeValues = new ArrayList<>();
                List<BigDecimal> expenseValues = new ArrayList<>();

                LocalDate currentStart = startDate;
                int periodIndex = 1;

                while (!currentStart.isAfter(endDate)) {
                        LocalDate currentEnd = currentStart.plusDays(6);

                        if (currentEnd.isAfter(endDate)) {
                                currentEnd = endDate;
                        }

                        final LocalDate weekStart = currentStart;
                        final LocalDate weekEnd = currentEnd;

                        BigDecimal weekIncome = incomes.stream()
                                        .filter(income -> isBetween(income.getDate(), weekStart, weekEnd))
                                        .map(Income::getAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal weekExpense = expenses.stream()
                                        .filter(expense -> isBetween(expense.getDate(), weekStart, weekEnd))
                                        .map(Expense::getAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        labels.add("Periodo " + periodIndex);
                        incomeValues.add(weekIncome);
                        expenseValues.add(weekExpense);

                        currentStart = currentStart.plusDays(7);
                        periodIndex++;
                }

                return new DashboardDto.FinanceWeeksDto(labels, incomeValues, expenseValues);
        }

        private List<DashboardDto.UpcomingAppointmentDto> buildUpcomingAppointments(LocalDate today) {
                return appointmentRepository
                                .findByDateGreaterThanEqualOrderByDateAscStartTimeAsc(today)
                                .stream()
                                .filter(appointment -> appointment.getStatus() != AppointmentStatus.CANCELADA)
                                .limit(5)
                                .map(this::mapUpcomingAppointment)
                                .toList();
        }

        private DashboardDto.UpcomingAppointmentDto mapUpcomingAppointment(Appointment appointment) {
                String patientName = appointment.getPatient().getFirstName() + " "
                                + appointment.getPatient().getLastName();
                String psychologistName = appointment.getPsychologist().getFirstName() + " "
                                + appointment.getPsychologist().getLastName();
                String serviceName = appointment.getService().getName();

                String date = appointment.getDate().format(DATE_FORMATTER);
                String time = appointment.getStartTime().format(TIME_FORMATTER);

                return new DashboardDto.UpcomingAppointmentDto(
                                appointment.getId(),
                                patientName,
                                serviceName,
                                psychologistName,
                                date,
                                time,
                                formatAppointmentStatus(appointment.getStatus()),
                                getInitials(patientName));
        }

        private boolean isAppointmentPaymentPending(Appointment appointment) {
                if (Boolean.FALSE.equals(appointment.getPaid())) {
                        return true;
                }

                if (appointment.getPendingAmount() != null
                                && appointment.getPendingAmount().compareTo(BigDecimal.ZERO) > 0) {
                        return true;
                }

                String paymentStatus = normalize(appointment.getPaymentStatus());

                Set<String> pendingStatuses = Set.of(
                                "PENDIENTE",
                                "PAGO_EN_REVISION",
                                "EN_REVISION",
                                "PARCIAL",
                                "NO_PAGADO");

                return pendingStatuses.contains(paymentStatus);
        }

        private boolean isLeadPaymentPending(Lead lead) {
                String paymentStatus = normalize(lead.getPaymentStatus());

                Set<String> pendingStatuses = Set.of(
                                "PENDIENTE",
                                "PAGO_EN_REVISION",
                                "EN_REVISION",
                                "PARCIAL");

                return pendingStatuses.contains(paymentStatus);
        }

        private boolean isLeadPending(Lead lead) {
                String status = normalize(lead.getStatus());

                Set<String> pendingStatuses = Set.of(
                                "NUEVO",
                                "PAGO_EN_REVISION",
                                "CONTACTADO",
                                "PRE_RESERVADO");

                return pendingStatuses.contains(status);
        }

        private boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
                return date != null
                                && !date.isBefore(start)
                                && !date.isAfter(end);
        }

        private String normalize(String value) {
                return value == null ? "" : value.trim().toUpperCase();
        }

        private String formatAppointmentStatus(AppointmentStatus status) {
                if (status == null) {
                        return "Sin estado";
                }

                return switch (status) {
                        case PROGRAMADA -> "Programada";
                        case ATENDIDA -> "Atendida";
                        case CANCELADA -> "Cancelada";
                        case NO_ASISTIO -> "No asistió";
                        case REPROGRAMADA -> "Reprogramada";
                };
        }

        private String getInitials(String fullName) {
                if (fullName == null || fullName.isBlank()) {
                        return "CP";
                }

                String[] parts = fullName.trim().split("\\s+");

                if (parts.length == 1) {
                        return parts[0].substring(0, 1).toUpperCase();
                }

                return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }

        private DateRange resolveDateRange(String period, LocalDate today) {
                String normalizedPeriod = normalize(period);

                return switch (normalizedPeriod) {
                        case "DAY" -> new DateRange(today, today);

                        case "WEEK" -> new DateRange(
                                        today.with(DayOfWeek.MONDAY),
                                        today.with(DayOfWeek.SUNDAY));

                        case "YEAR" -> new DateRange(
                                        today.withDayOfYear(1),
                                        today.withDayOfYear(today.lengthOfYear()));

                        default -> new DateRange(
                                        today.withDayOfMonth(1),
                                        today.withDayOfMonth(today.lengthOfMonth()));
                };
        }

        private record DateRange(LocalDate startDate, LocalDate endDate) {
        }
}