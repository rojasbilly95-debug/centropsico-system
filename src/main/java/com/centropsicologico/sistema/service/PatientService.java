package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.entity.Patient;

import java.util.List;

public interface PatientService {

    Patient save(Patient patient);

    List<Patient> findAll();

    List<Patient> findActivePatients();

    Patient findById(Long id);

    Patient update(Long id, Patient patient);

    Patient toggleActive(Long id);

    void delete(Long id);
}