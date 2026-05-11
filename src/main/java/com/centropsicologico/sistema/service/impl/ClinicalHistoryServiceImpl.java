package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.ClinicalHistory;
import com.centropsicologico.sistema.entity.Patient;
import com.centropsicologico.sistema.exception.ResourceNotFoundException;
import com.centropsicologico.sistema.repository.ClinicalHistoryRepository;
import com.centropsicologico.sistema.repository.PatientRepository;
import com.centropsicologico.sistema.service.ClinicalHistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClinicalHistoryServiceImpl implements ClinicalHistoryService {

    private final ClinicalHistoryRepository clinicalHistoryRepository;
    private final PatientRepository patientRepository;

    public ClinicalHistoryServiceImpl(
            ClinicalHistoryRepository clinicalHistoryRepository,
            PatientRepository patientRepository
    ) {
        this.clinicalHistoryRepository = clinicalHistoryRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public ClinicalHistory save(Long patientId, ClinicalHistory history) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con id: " + patientId));

        history.setPatient(patient);
        history.setDate(LocalDateTime.now());
        history.setActive(true);

        return clinicalHistoryRepository.save(history);
    }

    @Override
    public List<ClinicalHistory> findByPatient(Long patientId) {
        return clinicalHistoryRepository.findByPatientIdAndActiveTrueOrderByDateDesc(patientId);
    }

    @Override
    public ClinicalHistory findById(Long id) {
        return clinicalHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Historia clínica no encontrada con id: " + id));
    }

    @Override
    public void delete(Long id) {
        ClinicalHistory history = findById(id);
        history.setActive(false);
        clinicalHistoryRepository.save(history);
    }
}