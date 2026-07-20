package com.centropsicologico.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailablePsychologistDto {

    /*
     * Identificador del psicólogo.
     */
    private Long psychologistId;

    /*
     * Nombre completo del psicólogo.
     */
    private String psychologistName;

    /*
     * Especialidad registrada.
     */
    private String specialty;

    /*
     * Correo del psicólogo.
     */
    private String email;

    /*
     * Horarios semanales activos.
     */
    private List<ScheduleDto> schedules =
            new ArrayList<>();


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleDto {

        /*
         * Día almacenado:
         * MONDAY, TUESDAY, WEDNESDAY, etc.
         */
        private String dayOfWeek;

        /*
         * Día mostrado:
         * Lunes, Martes, Miércoles, etc.
         */
        private String dayLabel;

        /*
         * Hora de inicio, por ejemplo 09:00.
         */
        private String startTime;

        /*
         * Hora de finalización, por ejemplo 17:00.
         */
        private String endTime;
    }
}