package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.entity.Psychologist;
import com.centropsicologico.sistema.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    /*
     * =========================================================
     * CONSULTAS GENERALES
     * =========================================================
     */

    List<Appointment> findByDate(
            LocalDate date
    );

    List<Appointment> findByDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    List<Appointment>
    findByDateGreaterThanEqualOrderByDateAscStartTimeAsc(
            LocalDate date
    );


    /*
     * =========================================================
     * CONSULTAS POR PSICÓLOGO
     * =========================================================
     */

    List<Appointment> findByPsychologistId(
            Long psychologistId
    );

    List<Appointment> findByPsychologistEmail(
            String email
    );

    List<Appointment> findByPsychologistEmailAndDate(
            String email,
            LocalDate date
    );

    List<Appointment> findByPsychologistIdAndDate(
            Long psychologistId,
            LocalDate date
    );

    /*
     * Citas del psicólogo autenticado dentro del periodo
     * seleccionado: día, semana, mes o año.
     */
    List<Appointment>
    findByPsychologistEmailAndDateBetween(
            String email,
            LocalDate startDate,
            LocalDate endDate
    );

    /*
     * Próximas citas del psicólogo autenticado,
     * ordenadas por fecha y hora.
     */
    List<Appointment>
    findByPsychologistEmailAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(
            String email,
            LocalDate date
    );


    /*
     * =========================================================
     * CONSULTAS POR ESTADO
     * =========================================================
     */

    List<Appointment> findByStatusAndDateBetween(
            AppointmentStatus status,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Appointment>
    findByStatusAndDateBetweenAndPsychologistId(
            AppointmentStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Long psychologistId
    );


    /*
     * =========================================================
     * VALIDACIÓN DE CRUCES DE HORARIO
     * =========================================================
     */

    boolean
    existsByPsychologistAndDateAndStartTimeLessThanAndEndTimeGreaterThan(
            Psychologist psychologist,
            LocalDate date,
            LocalTime endTime,
            LocalTime startTime
    );

    boolean
    existsByPsychologistAndDateAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
            Psychologist psychologist,
            LocalDate date,
            LocalTime endTime,
            LocalTime startTime,
            Long id
    );


    /*
     * =========================================================
     * SEGURIDAD DE HISTORIA CLÍNICA
     * =========================================================
     */

    /*
     * Comprueba si un paciente está relacionado con un
     * psicólogo mediante una cita registrada.
     *
     * Se utiliza para impedir que el psicólogo consulte
     * historias clínicas de pacientes no asignados.
     */
    boolean existsByPatientIdAndPsychologistEmail(
            Long patientId,
            String psychologistEmail
    );
}