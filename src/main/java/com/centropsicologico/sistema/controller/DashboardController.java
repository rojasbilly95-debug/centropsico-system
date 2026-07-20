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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("America/Lima");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final LeadRepository leadRepository;

    public DashboardController(
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            IncomeRepository incomeRepository,
            ExpenseRepository expenseRepository,
            LeadRepository leadRepository
    ) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.leadRepository = leadRepository;
    }

    @GetMapping
    public DashboardDto getDashboard(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication
    ) {
        validateAuthentication(authentication);

        String authenticatedEmail = authentication.getName();
        String authenticatedRole = getAuthenticatedRole(authentication);
        String normalizedPeriod = normalizePeriod(period);

        boolean isAdmin = "ADMIN".equals(authenticatedRole);
        boolean isReceptionist = "RECEPCIONISTA".equals(authenticatedRole);
        boolean isPsychologist = "PSICOLOGO".equals(authenticatedRole);

        ZonedDateTime currentDateTime =
                ZonedDateTime.now(BUSINESS_ZONE);

        LocalDate today =
                currentDateTime.toLocalDate();

        LocalTime currentTime =
                currentDateTime.toLocalTime();

        DateRange dateRange =
                resolveDateRange(normalizedPeriod, today);

        LocalDate startDate =
                dateRange.startDate();

        LocalDate endDate =
                dateRange.endDate();

        /*
         * =====================================================
         * CITAS SEGÚN EL ROL
         * =====================================================
         */

        List<Appointment> periodAppointments;
        List<Appointment> todayAppointments;
        List<Appointment> upcomingAppointmentsSource;

        if (isPsychologist) {
            periodAppointments =
                    appointmentRepository
                            .findByPsychologistEmailAndDateBetween(
                                    authenticatedEmail,
                                    startDate,
                                    endDate
                            );

            todayAppointments =
                    appointmentRepository
                            .findByPsychologistEmailAndDate(
                                    authenticatedEmail,
                                    today
                            );

            upcomingAppointmentsSource =
                    appointmentRepository
                            .findByPsychologistEmailAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(
                                    authenticatedEmail,
                                    today
                            );
        } else {
            periodAppointments =
                    appointmentRepository
                            .findByDateBetween(
                                    startDate,
                                    endDate
                            );

            todayAppointments =
                    appointmentRepository.findByDate(today);

            upcomingAppointmentsSource =
                    appointmentRepository
                            .findByDateGreaterThanEqualOrderByDateAscStartTimeAsc(
                                    today
                            );
        }

        /*
         * =====================================================
         * INDICADORES DE CITAS
         * =====================================================
         */

        long totalPeriodAppointments =
                periodAppointments.size();

        long totalTodayAppointments =
                todayAppointments.size();

        /*
         * Citas de hoy que continúan PROGRAMADAS y todavía
         * no han terminado.
         */
        long todayScheduledAppointments =
                todayAppointments
                        .stream()
                        .filter(appointment ->
                                isActiveScheduledAppointment(
                                        appointment,
                                        today,
                                        currentTime
                                )
                        )
                        .count();

        /*
         * Citas PROGRAMADAS vigentes dentro del periodo.
         * No incluye las citas pasadas pendientes de cierre.
         */
        long scheduledAppointments =
                periodAppointments
                        .stream()
                        .filter(appointment ->
                                isActiveScheduledAppointment(
                                        appointment,
                                        today,
                                        currentTime
                                )
                        )
                        .count();

        /*
         * Citas cuya fecha y hora final ya pasaron,
         * pero continúan en estado PROGRAMADA.
         */
        long overdueScheduledAppointments =
                periodAppointments
                        .stream()
                        .filter(appointment ->
                                isOverdueScheduledAppointment(
                                        appointment,
                                        today,
                                        currentTime
                                )
                        )
                        .count();

        long attendedAppointments =
                countStatus(
                        periodAppointments,
                        AppointmentStatus.ATENDIDA
                );

        long cancelledAppointments =
                countStatus(
                        periodAppointments,
                        AppointmentStatus.CANCELADA
                );

        long noShowAppointments =
                countStatus(
                        periodAppointments,
                        AppointmentStatus.NO_ASISTIO
                );

        long rescheduledAppointments =
                countStatus(
                        periodAppointments,
                        AppointmentStatus.REPROGRAMADA
                );

        /*
         * =====================================================
         * PACIENTES SEGÚN EL ROL
         * =====================================================
         */

        long totalPatients;

        if (isPsychologist) {
            totalPatients =
                    periodAppointments
                            .stream()
                            .filter(appointment ->
                                    appointment != null &&
                                    appointment.getPatient() != null &&
                                    appointment.getPatient().getId() != null
                            )
                            .map(appointment ->
                                    appointment.getPatient().getId()
                            )
                            .distinct()
                            .count();
        } else {
            totalPatients =
                    patientRepository.count();
        }

        /*
         * =====================================================
         * INFORMACIÓN FINANCIERA
         * SOLO ADMIN
         * =====================================================
         */

        List<Income> periodIncomes =
                List.of();

        List<Expense> periodExpenses =
                List.of();

        BigDecimal totalIncome =
                BigDecimal.ZERO;

        BigDecimal totalExpense =
                BigDecimal.ZERO;

        BigDecimal profit =
                BigDecimal.ZERO;

        DashboardDto.FinanceWeeksDto financeWeeks =
                emptyFinanceWeeks();

        if (isAdmin) {
            periodIncomes =
                    incomeRepository
                            .findByDateBetweenAndActiveTrue(
                                    startDate,
                                    endDate
                            );

            periodExpenses =
                    expenseRepository
                            .findByDateBetweenAndActiveTrue(
                                    startDate,
                                    endDate
                            );

            totalIncome =
                    periodIncomes
                            .stream()
                            .map(Income::getAmount)
                            .map(this::safeAmount)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            totalExpense =
                    periodExpenses
                            .stream()
                            .map(Expense::getAmount)
                            .map(this::safeAmount)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            profit =
                    totalIncome.subtract(totalExpense);

            financeWeeks =
                    buildFinanceWeeks(
                            startDate,
                            endDate,
                            periodIncomes,
                            periodExpenses
                    );
        }

        /*
         * =====================================================
         * PAGOS Y PRE-RESERVAS
         * ADMIN Y RECEPCIONISTA
         * =====================================================
         */

        long pendingPayments = 0L;
        long pendingLeads = 0L;

        if (isAdmin || isReceptionist) {
            List<Lead> leads =
                    leadRepository.findAll();

            long pendingAppointmentPayments =
                    periodAppointments
                            .stream()
                            .filter(this::isAppointmentPaymentPending)
                            .count();

            long pendingLeadPayments =
                    leads
                            .stream()
                            .filter(this::isLeadPaymentPending)
                            .count();

            pendingPayments =
                    pendingAppointmentPayments +
                    pendingLeadPayments;

            pendingLeads =
                    leads
                            .stream()
                            .filter(this::isLeadPending)
                            .count();
        }

        /*
         * El psicólogo no recibe información financiera
         * ni de pre-reservas.
         */
        if (isPsychologist) {
            pendingPayments = 0L;
            pendingLeads = 0L;
        }

        /*
         * =====================================================
         * GRÁFICO Y PRÓXIMAS CITAS
         * =====================================================
         */

        List<DashboardDto.StatusItemDto> appointmentStatus =
                buildAppointmentStatus(
                        periodAppointments,
                        today,
                        currentTime
                );

        List<DashboardDto.UpcomingAppointmentDto> upcomingAppointments =
                buildUpcomingAppointments(
                        upcomingAppointmentsSource,
                        today,
                        currentTime
                );

        /*
         * =====================================================
         * RESPUESTA
         * =====================================================
         */

        DashboardDto response =
                new DashboardDto();

        response.setRole(authenticatedRole);
        response.setPeriod(normalizedPeriod);

        response.setTotalPatients(totalPatients);

        response.setPeriodAppointments(
                totalPeriodAppointments
        );

        response.setTodayAppointments(
                totalTodayAppointments
        );

        response.setTodayScheduledAppointments(
                todayScheduledAppointments
        );

        response.setOverdueScheduledAppointments(
                overdueScheduledAppointments
        );

        response.setScheduledAppointments(
                scheduledAppointments
        );

        response.setAttendedAppointments(
                attendedAppointments
        );

        response.setCancelledAppointments(
                cancelledAppointments
        );

        response.setNoShowAppointments(
                noShowAppointments
        );

        response.setRescheduledAppointments(
                rescheduledAppointments
        );

        response.setTotalIncome(totalIncome);
        response.setTotalExpense(totalExpense);
        response.setProfit(profit);

        response.setPendingPayments(
                pendingPayments
        );

        response.setPendingLeads(
                pendingLeads
        );

        /*
         * Compatibilidad temporal con versiones antiguas
         * del frontend.
         */
        response.setTotalAppointmentsMonth(
                totalPeriodAppointments
        );

        response.setAppointmentStatus(
                appointmentStatus
        );

        response.setFinanceWeeks(
                financeWeeks
        );

        response.setUpcomingAppointments(
                upcomingAppointments
        );

        return response;
    }

    /*
     * =========================================================
     * ESTADOS DE CITAS
     * =========================================================
     */

    private List<DashboardDto.StatusItemDto> buildAppointmentStatus(
            List<Appointment> appointments,
            LocalDate today,
            LocalTime currentTime
    ) {
        long activeScheduled =
                appointments
                        .stream()
                        .filter(appointment ->
                                isActiveScheduledAppointment(
                                        appointment,
                                        today,
                                        currentTime
                                )
                        )
                        .count();

        long overdueScheduled =
                appointments
                        .stream()
                        .filter(appointment ->
                                isOverdueScheduledAppointment(
                                        appointment,
                                        today,
                                        currentTime
                                )
                        )
                        .count();

        return List.of(
                new DashboardDto.StatusItemDto(
                        "Programadas",
                        activeScheduled,
                        "#2563eb"
                ),

                new DashboardDto.StatusItemDto(
                        "Pendientes de cierre",
                        overdueScheduled,
                        "#f59e0b"
                ),

                new DashboardDto.StatusItemDto(
                        "Atendidas",
                        countStatus(
                                appointments,
                                AppointmentStatus.ATENDIDA
                        ),
                        "#047857"
                ),

                new DashboardDto.StatusItemDto(
                        "Canceladas",
                        countStatus(
                                appointments,
                                AppointmentStatus.CANCELADA
                        ),
                        "#b42318"
                ),

                new DashboardDto.StatusItemDto(
                        "No asistió",
                        countStatus(
                                appointments,
                                AppointmentStatus.NO_ASISTIO
                        ),
                        "#c2410c"
                ),

                new DashboardDto.StatusItemDto(
                        "Reprogramadas",
                        countStatus(
                                appointments,
                                AppointmentStatus.REPROGRAMADA
                        ),
                        "#475467"
                )
        );
    }

    private Long countStatus(
            List<Appointment> appointments,
            AppointmentStatus status
    ) {
        if (appointments == null) {
            return 0L;
        }

        return appointments
                .stream()
                .filter(appointment ->
                        appointment != null &&
                        appointment.getStatus() == status
                )
                .count();
    }

    /*
     * =========================================================
     * CITAS PROGRAMADAS VIGENTES Y VENCIDAS
     * =========================================================
     */

    private boolean isActiveScheduledAppointment(
            Appointment appointment,
            LocalDate today,
            LocalTime currentTime
    ) {
        if (
                appointment == null ||
                appointment.getStatus() != AppointmentStatus.PROGRAMADA ||
                appointment.getDate() == null
        ) {
            return false;
        }

        return !isOverdueScheduledAppointment(
                appointment,
                today,
                currentTime
        );
    }

    private boolean isOverdueScheduledAppointment(
            Appointment appointment,
            LocalDate today,
            LocalTime currentTime
    ) {
        if (
                appointment == null ||
                appointment.getStatus() != AppointmentStatus.PROGRAMADA ||
                appointment.getDate() == null
        ) {
            return false;
        }

        if (appointment.getDate().isBefore(today)) {
            return true;
        }

        if (appointment.getDate().isAfter(today)) {
            return false;
        }

        /*
         * La cita corresponde a hoy.
         * Se considera vencida cuando su hora final ya pasó.
         */
        if (appointment.getEndTime() != null) {
            return !appointment
                    .getEndTime()
                    .isAfter(currentTime);
        }

        /*
         * Respaldo para registros antiguos sin hora final.
         */
        return appointment.getStartTime() != null &&
                appointment
                        .getStartTime()
                        .isBefore(currentTime);
    }

    /*
     * =========================================================
     * GRÁFICO FINANCIERO
     * =========================================================
     */

    private DashboardDto.FinanceWeeksDto buildFinanceWeeks(
            LocalDate startDate,
            LocalDate endDate,
            List<Income> incomes,
            List<Expense> expenses
    ) {
        List<String> labels =
                new ArrayList<>();

        List<BigDecimal> incomeValues =
                new ArrayList<>();

        List<BigDecimal> expenseValues =
                new ArrayList<>();

        LocalDate currentStart =
                startDate;

        int periodIndex =
                1;

        while (!currentStart.isAfter(endDate)) {
            LocalDate currentEnd =
                    currentStart.plusDays(6);

            if (currentEnd.isAfter(endDate)) {
                currentEnd =
                        endDate;
            }

            final LocalDate periodStart =
                    currentStart;

            final LocalDate periodEnd =
                    currentEnd;

            BigDecimal periodIncome =
                    incomes
                            .stream()
                            .filter(income ->
                                    income != null &&
                                    isBetween(
                                            income.getDate(),
                                            periodStart,
                                            periodEnd
                                    )
                            )
                            .map(Income::getAmount)
                            .map(this::safeAmount)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            BigDecimal periodExpense =
                    expenses
                            .stream()
                            .filter(expense ->
                                    expense != null &&
                                    isBetween(
                                            expense.getDate(),
                                            periodStart,
                                            periodEnd
                                    )
                            )
                            .map(Expense::getAmount)
                            .map(this::safeAmount)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            labels.add(
                    "Periodo " + periodIndex
            );

            incomeValues.add(
                    periodIncome
            );

            expenseValues.add(
                    periodExpense
            );

            currentStart =
                    currentStart.plusDays(7);

            periodIndex++;
        }

        return new DashboardDto.FinanceWeeksDto(
                labels,
                incomeValues,
                expenseValues
        );
    }

    private DashboardDto.FinanceWeeksDto emptyFinanceWeeks() {
        return new DashboardDto.FinanceWeeksDto(
                List.of(),
                List.of(),
                List.of()
        );
    }

    /*
     * =========================================================
     * PRÓXIMAS CITAS
     * =========================================================
     */

    private List<DashboardDto.UpcomingAppointmentDto> buildUpcomingAppointments(
            List<Appointment> appointments,
            LocalDate today,
            LocalTime currentTime
    ) {
        return appointments
                .stream()
                .filter(appointment ->
                        isUpcomingAppointment(
                                appointment,
                                today,
                                currentTime
                        )
                )
                .limit(5)
                .map(this::mapUpcomingAppointment)
                .toList();
    }

    private boolean isUpcomingAppointment(
            Appointment appointment,
            LocalDate today,
            LocalTime currentTime
    ) {
        if (
                appointment == null ||
                appointment.getDate() == null ||
                appointment.getStatus() != AppointmentStatus.PROGRAMADA
        ) {
            return false;
        }

        if (appointment.getDate().isAfter(today)) {
            return true;
        }

        if (appointment.getDate().isBefore(today)) {
            return false;
        }

        /*
         * Si la cita es de hoy, continúa apareciendo mientras
         * no haya terminado.
         */
        if (appointment.getEndTime() != null) {
            return appointment
                    .getEndTime()
                    .isAfter(currentTime);
        }

        /*
         * Respaldo para citas antiguas sin hora final.
         */
        return appointment.getStartTime() == null ||
                !appointment
                        .getStartTime()
                        .isBefore(currentTime);
    }

    private DashboardDto.UpcomingAppointmentDto mapUpcomingAppointment(
            Appointment appointment
    ) {
        String patientName =
                buildFullName(
                        appointment.getPatient() != null
                                ? appointment.getPatient().getFirstName()
                                : "",
                        appointment.getPatient() != null
                                ? appointment.getPatient().getLastName()
                                : ""
                );

        String psychologistName =
                buildFullName(
                        appointment.getPsychologist() != null
                                ? appointment.getPsychologist().getFirstName()
                                : "",
                        appointment.getPsychologist() != null
                                ? appointment.getPsychologist().getLastName()
                                : ""
                );

        String serviceName =
                appointment.getService() != null &&
                appointment.getService().getName() != null
                        ? appointment.getService().getName()
                        : "Servicio psicológico";

        String formattedDate =
                appointment.getDate() != null
                        ? appointment
                                .getDate()
                                .format(DATE_FORMATTER)
                        : "-";

        String formattedTime =
                appointment.getStartTime() != null
                        ? appointment
                                .getStartTime()
                                .format(TIME_FORMATTER)
                        : "-";

        return new DashboardDto.UpcomingAppointmentDto(
                appointment.getId(),
                patientName,
                serviceName,
                psychologistName,
                formattedDate,
                formattedTime,
                formatAppointmentStatus(
                        appointment.getStatus()
                ),
                getInitials(patientName)
        );
    }

    /*
     * =========================================================
     * PAGOS Y SOLICITUDES
     * =========================================================
     */

    private boolean isAppointmentPaymentPending(
            Appointment appointment
    ) {
        if (appointment == null) {
            return false;
        }

        /*
         * Las citas canceladas o con inasistencia no deben
         * aparecer como pagos pendientes operativos.
         */
        if (
                appointment.getStatus() == AppointmentStatus.CANCELADA ||
                appointment.getStatus() == AppointmentStatus.NO_ASISTIO
        ) {
            return false;
        }

        if (Boolean.FALSE.equals(appointment.getPaid())) {
            return true;
        }

        if (
                appointment.getPendingAmount() != null &&
                appointment
                        .getPendingAmount()
                        .compareTo(BigDecimal.ZERO) > 0
        ) {
            return true;
        }

        String paymentStatus =
                normalizeText(
                        appointment.getPaymentStatus()
                );

        Set<String> pendingStatuses =
                Set.of(
                        "PENDIENTE",
                        "PAGO_EN_REVISION",
                        "EN_REVISION",
                        "PARCIAL",
                        "NO_PAGADO"
                );

        return pendingStatuses.contains(
                paymentStatus
        );
    }

    private boolean isLeadPaymentPending(
            Lead lead
    ) {
        if (lead == null) {
            return false;
        }

        String paymentStatus =
                normalizeText(
                        lead.getPaymentStatus()
                );

        Set<String> pendingStatuses =
                Set.of(
                        "PENDIENTE",
                        "PAGO_EN_REVISION",
                        "EN_REVISION",
                        "PARCIAL"
                );

        return pendingStatuses.contains(
                paymentStatus
        );
    }

    private boolean isLeadPending(
            Lead lead
    ) {
        if (lead == null) {
            return false;
        }

        String leadStatus =
                normalizeText(
                        lead.getStatus()
                );

        Set<String> pendingStatuses =
                Set.of(
                        "NUEVO",
                        "PAGO_EN_REVISION",
                        "CONTACTADO",
                        "PRE_RESERVADO"
                );

        return pendingStatuses.contains(
                leadStatus
        );
    }

    /*
     * =========================================================
     * AUTENTICACIÓN Y ROL
     * =========================================================
     */

    private void validateAuthentication(
            Authentication authentication
    ) {
        if (
                authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado"
            );
        }
    }

    private String getAuthenticatedRole(
            Authentication authentication
    ) {
        return authentication
                .getAuthorities()
                .stream()
                .map(authority ->
                        authority
                                .getAuthority()
                                .replace("ROLE_", "")
                                .trim()
                                .toUpperCase(Locale.ROOT)
                )
                .filter(role ->
                        role.equals("ADMIN") ||
                        role.equals("RECEPCIONISTA") ||
                        role.equals("PSICOLOGO")
                )
                .findFirst()
                .orElse("SIN_ROL");
    }

    /*
     * =========================================================
     * PERIODO
     * =========================================================
     */

    private String normalizePeriod(
            String period
    ) {
        String normalized =
                period == null
                        ? ""
                        : period
                                .trim()
                                .toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case "day", "week", "month", "year" ->
                    normalized;

            default ->
                    "month";
        };
    }

    private DateRange resolveDateRange(
            String period,
            LocalDate today
    ) {
        return switch (period) {
            case "day" ->
                    new DateRange(
                            today,
                            today
                    );

            case "week" ->
                    new DateRange(
                            today.with(DayOfWeek.MONDAY),
                            today.with(DayOfWeek.SUNDAY)
                    );

            case "year" ->
                    new DateRange(
                            today.withDayOfYear(1),
                            today.withDayOfYear(
                                    today.lengthOfYear()
                            )
                    );

            default ->
                    new DateRange(
                            today.withDayOfMonth(1),
                            today.withDayOfMonth(
                                    today.lengthOfMonth()
                            )
                    );
        };
    }

    /*
     * =========================================================
     * HELPERS
     * =========================================================
     */

    private boolean isBetween(
            LocalDate date,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return date != null &&
                !date.isBefore(startDate) &&
                !date.isAfter(endDate);
    }

    private BigDecimal safeAmount(
            BigDecimal amount
    ) {
        return amount != null
                ? amount
                : BigDecimal.ZERO;
    }

    private String normalizeText(
            Object value
    ) {
        return value == null
                ? ""
                : String.valueOf(value)
                        .trim()
                        .toUpperCase(Locale.ROOT);
    }

    private String buildFullName(
            String firstName,
            String lastName
    ) {
        String fullName =
                (
                        (firstName != null ? firstName : "") +
                        " " +
                        (lastName != null ? lastName : "")
                )
                        .replaceAll("\\s+", " ")
                        .trim();

        return fullName.isBlank()
                ? "Sin nombre"
                : fullName;
    }

    private String formatAppointmentStatus(
            AppointmentStatus status
    ) {
        if (status == null) {
            return "Sin estado";
        }

        return switch (status) {
            case PROGRAMADA ->
                    "Programada";

            case ATENDIDA ->
                    "Atendida";

            case CANCELADA ->
                    "Cancelada";

            case NO_ASISTIO ->
                    "No asistió";

            case REPROGRAMADA ->
                    "Reprogramada";
        };
    }

    private String getInitials(
            String fullName
    ) {
        if (
                fullName == null ||
                fullName.isBlank()
        ) {
            return "CP";
        }

        String[] parts =
                fullName
                        .trim()
                        .split("\\s+");

        if (parts.length == 1) {
            return parts[0]
                    .substring(0, 1)
                    .toUpperCase(Locale.ROOT);
        }

        return (
                parts[0].substring(0, 1) +
                parts[1].substring(0, 1)
        ).toUpperCase(Locale.ROOT);
    }

    private record DateRange(
            LocalDate startDate,
            LocalDate endDate
    ) {
    }
}
