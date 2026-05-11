package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.entity.ClinicalHistory;
import com.centropsicologico.sistema.service.ClinicalHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinical-history")
public class ClinicalHistoryController {

    private final ClinicalHistoryService clinicalHistoryService;

    public ClinicalHistoryController(ClinicalHistoryService clinicalHistoryService) {
        this.clinicalHistoryService = clinicalHistoryService;
    }

    @PostMapping("/patient/{patientId}")
    public ClinicalHistory save(
            @PathVariable Long patientId,
            @RequestBody ClinicalHistory history
    ) {
        return clinicalHistoryService.save(patientId, history);
    }

    @GetMapping("/patient/{patientId}")
    public List<ClinicalHistory> findByPatient(@PathVariable Long patientId) {
        return clinicalHistoryService.findByPatient(patientId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        clinicalHistoryService.delete(id);
    }
}