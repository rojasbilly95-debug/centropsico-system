package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.entity.ClinicalHistory;

import java.util.List;

public interface ClinicalHistoryService {

    ClinicalHistory save(Long patientId, ClinicalHistory history);

    List<ClinicalHistory> findByPatient(Long patientId);

    ClinicalHistory findById(Long id);

    void delete(Long id);
}