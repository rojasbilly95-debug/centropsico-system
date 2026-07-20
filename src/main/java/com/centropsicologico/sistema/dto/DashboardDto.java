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
public class DashboardDto {

    /*
     * =========================================================
     * CONTEXTO DEL DASHBOARD
     * =========================================================
     */

    /*
     * Rol del usuario autenticado:
     * ADMIN, RECEPCIONISTA o PSICOLOGO.
     */
    private String role = "";

    /*
     * Periodo seleccionado:
     * day, week, month o year.
     */
    private String period = "month";


    /*
     * =========================================================
     * INDICADORES PRINCIPALES
     * =========================================================
     */

    /*
     * ADMIN / RECEPCIONISTA:
     * total de pacientes registrados.
     *
     * PSICOLOGO:
     * pacientes diferentes relacionados con sus citas
     * dentro del periodo seleccionado.
     */
    private Long totalPatients = 0L;

    /*
     * Cantidad total de citas dentro del periodo seleccionado.
     *
     * Ejemplo:
     * - day: citas del día.
     * - week: citas de la semana.
     * - month: citas del mes.
     * - year: citas del año.
     */
    private Long periodAppointments = 0L;

    /*
     * Cantidad total de citas correspondientes al día actual.
     *
     * Se mantiene separada porque los recordatorios del dashboard
     * necesitan saber cuántas citas existen específicamente hoy.
     */
    private Long todayAppointments = 0L;

    /*
     * Citas PROGRAMADAS para el día actual.
     */
    private Long todayScheduledAppointments = 0L;


    /*
     * =========================================================
     * CITAS POR ESTADO EN EL PERIODO
     * =========================================================
     */

    private Long scheduledAppointments = 0L;
    private Long attendedAppointments = 0L;
    private Long cancelledAppointments = 0L;
    private Long noShowAppointments = 0L;
    private Long rescheduledAppointments = 0L;


    /*
     * =========================================================
     * INFORMACIÓN FINANCIERA
     * =========================================================
     */

    /*
     * Estos valores solo deben contener información real
     * cuando el usuario sea ADMIN.
     *
     * Para RECEPCIONISTA y PSICOLOGO deberán devolverse en cero.
     */
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpense = BigDecimal.ZERO;
    private BigDecimal profit = BigDecimal.ZERO;

    /*
     * ADMIN y RECEPCIONISTA pueden trabajar con pagos operativos.
     * Para PSICOLOGO debe devolverse cero.
     */
    private Long pendingPayments = 0L;

    /*
     * ADMIN y RECEPCIONISTA pueden consultar solicitudes.
     * Para PSICOLOGO debe devolverse cero.
     */
    private Long pendingLeads = 0L;


    /*
     * =========================================================
     * COMPATIBILIDAD TEMPORAL
     * =========================================================
     */

    /*
     * Se conserva temporalmente porque dashboard.js todavía
     * utiliza este nombre.
     *
     * Aunque se llame totalAppointmentsMonth, durante la
     * actualización contendrá el total del periodo seleccionado.
     *
     * Más adelante el frontend utilizará periodAppointments.
     */
    private Long totalAppointmentsMonth = 0L;


    /*
     * =========================================================
     * GRÁFICOS Y LISTADOS
     * =========================================================
     */

    private List<StatusItemDto> appointmentStatus = List.of();

    private FinanceWeeksDto financeWeeks =
            new FinanceWeeksDto(
                    List.of(),
                    List.of(),
                    List.of()
            );

    private List<UpcomingAppointmentDto> upcomingAppointments =
            List.of();


    /*
     * =========================================================
     * CONSTRUCTOR TEMPORAL DE COMPATIBILIDAD
     * =========================================================
     */

    /*
     * Este constructor conserva compatibilidad con el
     * DashboardController actual.
     *
     * Así el proyecto no presentará error mientras reemplazamos
     * el controlador en el siguiente paso.
     */
    public DashboardDto(
            Long totalPatients,
            Long todayAppointments,
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal profit,
            Long pendingPayments,
            Long pendingLeads,
            Long totalAppointmentsMonth,
            List<StatusItemDto> appointmentStatus,
            FinanceWeeksDto financeWeeks,
            List<UpcomingAppointmentDto> upcomingAppointments
    ) {
        this.totalPatients =
                valueOrZero(totalPatients);

        this.todayAppointments =
                valueOrZero(todayAppointments);

        /*
         * Temporalmente se toma el valor antiguo como
         * total del periodo.
         */
        this.periodAppointments =
                valueOrZero(totalAppointmentsMonth);

        this.totalIncome =
                decimalOrZero(totalIncome);

        this.totalExpense =
                decimalOrZero(totalExpense);

        this.profit =
                decimalOrZero(profit);

        this.pendingPayments =
                valueOrZero(pendingPayments);

        this.pendingLeads =
                valueOrZero(pendingLeads);

        this.totalAppointmentsMonth =
                valueOrZero(totalAppointmentsMonth);

        this.appointmentStatus =
                appointmentStatus != null
                        ? appointmentStatus
                        : List.of();

        this.financeWeeks =
                financeWeeks != null
                        ? financeWeeks
                        : new FinanceWeeksDto(
                                List.of(),
                                List.of(),
                                List.of()
                        );

        this.upcomingAppointments =
                upcomingAppointments != null
                        ? upcomingAppointments
                        : List.of();
    }


    /*
     * =========================================================
     * ELEMENTO DEL GRÁFICO DE ESTADOS
     * =========================================================
     */

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusItemDto {

        private String label;
        private Long value;
        private String color;
    }


    /*
     * =========================================================
     * DATOS DEL GRÁFICO FINANCIERO
     * =========================================================
     */

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinanceWeeksDto {

        private List<String> labels;
        private List<BigDecimal> income;
        private List<BigDecimal> expense;
    }


    /*
     * =========================================================
     * PRÓXIMAS CITAS
     * =========================================================
     */

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


    /*
     * =========================================================
     * HELPERS
     * =========================================================
     */

    private static Long valueOrZero(Long value) {
        return value != null
                ? value
                : 0L;
    }

    private static BigDecimal decimalOrZero(
            BigDecimal value
    ) {
        return value != null
                ? value
                : BigDecimal.ZERO;
    }
}