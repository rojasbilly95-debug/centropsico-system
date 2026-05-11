package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.entity.PsychologistAvailability;

import java.util.List;

public interface PsychologistAvailabilityService {

    PsychologistAvailability save(PsychologistAvailability availability);

    List<PsychologistAvailability> findAll();

    List<PsychologistAvailability> findActive();

    List<PsychologistAvailability> findByPsychologist(Long psychologistId);

    PsychologistAvailability findById(Long id);

    PsychologistAvailability update(Long id, PsychologistAvailability availability);

    PsychologistAvailability toggleActive(Long id);

    void delete(Long id);

    long countActiveByPsychologist(Long psychologistId);
}