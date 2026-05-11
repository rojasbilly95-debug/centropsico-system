package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.ClinicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClinicalHistoryRepository extends JpaRepository<ClinicalHistory, Long> {

    List<ClinicalHistory> findByPatientIdAndActiveTrueOrderByDateDesc(Long patientId);
}