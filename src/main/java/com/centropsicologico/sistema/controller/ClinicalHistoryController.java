package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.entity.ClinicalHistory;
import com.centropsicologico.sistema.service.ClinicalHistoryService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
            @RequestBody ClinicalHistory history,
            Principal principal,
            Authentication authentication
    ) {
        return clinicalHistoryService.save(
                patientId,
                history,
                principal.getName(),
                getRole(authentication)
        );
    }

    @GetMapping("/patient/{patientId}")
    public List<ClinicalHistory> findByPatient(
            @PathVariable Long patientId,
            Principal principal,
            Authentication authentication
    ) {
        return clinicalHistoryService.findByPatient(
                patientId,
                principal.getName(),
                getRole(authentication)
        );
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id,
            Principal principal,
            Authentication authentication
    ) {
        clinicalHistoryService.delete(
                id,
                principal.getName(),
                getRole(authentication)
        );
    }

    private String getRole(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return "SIN_ROL";
        }

        return authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse("SIN_ROL");
    }
}