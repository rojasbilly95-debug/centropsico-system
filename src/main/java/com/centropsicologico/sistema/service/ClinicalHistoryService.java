package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.entity.ClinicalHistory;

import java.util.List;

public interface ClinicalHistoryService {

    ClinicalHistory save(Long patientId, ClinicalHistory history, String userEmail, String role);

    List<ClinicalHistory> findByPatient(Long patientId, String userEmail, String role);

    ClinicalHistory findById(Long id);

    void delete(Long id, String userEmail, String role);
}