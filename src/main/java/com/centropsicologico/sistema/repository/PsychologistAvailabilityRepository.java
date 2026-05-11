package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.Psychologist;
import com.centropsicologico.sistema.entity.PsychologistAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface PsychologistAvailabilityRepository extends JpaRepository<PsychologistAvailability, Long> {

    List<PsychologistAvailability> findByPsychologistId(Long psychologistId);

    List<PsychologistAvailability> findByPsychologistIdAndActiveTrue(Long psychologistId);

    List<PsychologistAvailability> findByActiveTrue();

    long countByPsychologistIdAndActiveTrue(Long psychologistId);

    boolean existsByPsychologistAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThanAndActiveTrue(
            Psychologist psychologist,
            DayOfWeek dayOfWeek,
            LocalTime endTime,
            LocalTime startTime
    );

    boolean existsByPsychologistAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThanAndActiveTrueAndIdNot(
            Psychologist psychologist,
            DayOfWeek dayOfWeek,
            LocalTime endTime,
            LocalTime startTime,
            Long id
    );

    List<PsychologistAvailability> findByDayOfWeekAndActiveTrue(DayOfWeek dayOfWeek);
}