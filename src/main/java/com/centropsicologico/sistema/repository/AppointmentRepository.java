package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.entity.Psychologist;
import com.centropsicologico.sistema.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPsychologistId(Long psychologistId);

    List<Appointment> findByDate(LocalDate date);

    List<Appointment> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Appointment> findByDateGreaterThanEqualOrderByDateAscStartTimeAsc(LocalDate date);

    boolean existsByPsychologistAndDateAndStartTimeLessThanAndEndTimeGreaterThan(
            Psychologist psychologist,
            LocalDate date,
            LocalTime endTime,
            LocalTime startTime
    );

    boolean existsByPsychologistAndDateAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
            Psychologist psychologist,
            LocalDate date,
            LocalTime endTime,
            LocalTime startTime,
            Long id
    );

    List<Appointment> findByPsychologistEmail(String email);

    List<Appointment> findByPsychologistEmailAndDate(String email, LocalDate date);

    List<Appointment> findByPsychologistIdAndDate(Long psychologistId, LocalDate date);

    List<Appointment> findByStatusAndDateBetween(
            AppointmentStatus status,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Appointment> findByStatusAndDateBetweenAndPsychologistId(
            AppointmentStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Long psychologistId
    );
}