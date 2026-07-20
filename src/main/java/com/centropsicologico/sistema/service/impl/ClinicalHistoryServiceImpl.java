package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.ClinicalHistory;
import com.centropsicologico.sistema.entity.Patient;
import com.centropsicologico.sistema.exception.BusinessRuleException;
import com.centropsicologico.sistema.exception.ResourceNotFoundException;
import com.centropsicologico.sistema.repository.AppointmentRepository;
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
    private final AppointmentRepository appointmentRepository;

    public ClinicalHistoryServiceImpl(
            ClinicalHistoryRepository clinicalHistoryRepository,
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository
    ) {
        this.clinicalHistoryRepository = clinicalHistoryRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public ClinicalHistory save(Long patientId, ClinicalHistory history, String userEmail, String role) {
        validateClinicalAccess(patientId, userEmail, role);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con id: " + patientId));

        history.setPatient(patient);
        history.setDate(LocalDateTime.now());
        history.setActive(true);

        return clinicalHistoryRepository.save(history);
    }

    @Override
    public List<ClinicalHistory> findByPatient(Long patientId, String userEmail, String role) {
        validateClinicalAccess(patientId, userEmail, role);

        return clinicalHistoryRepository.findByPatientIdAndActiveTrueOrderByDateDesc(patientId);
    }

    @Override
    public ClinicalHistory findById(Long id) {
        return clinicalHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Historia clínica no encontrada con id: " + id));
    }

    @Override
    public void delete(Long id, String userEmail, String role) {
        ClinicalHistory history = findById(id);

        if (history.getPatient() == null || history.getPatient().getId() == null) {
            throw new BusinessRuleException("La historia clínica no tiene paciente asociado");
        }

        validateClinicalAccess(history.getPatient().getId(), userEmail, role);

        history.setActive(false);
        clinicalHistoryRepository.save(history);
    }

    private void validateClinicalAccess(Long patientId, String userEmail, String role) {
        if (patientId == null) {
            throw new BusinessRuleException("Debe indicar el paciente");
        }

        if ("ADMIN".equals(role)) {
            return;
        }

        if ("PSICOLOGO".equals(role)) {
            if (userEmail == null || userEmail.trim().isEmpty()) {
                throw new BusinessRuleException("No se pudo identificar al psicólogo autenticado");
            }

            boolean hasAssignedAppointment = appointmentRepository
                    .existsByPatientIdAndPsychologistEmail(patientId, userEmail);

            if (!hasAssignedAppointment) {
                throw new BusinessRuleException(
                        "No puedes acceder a la historia clínica de un paciente que no está asignado a tus citas"
                );
            }

            return;
        }

        throw new BusinessRuleException("No tienes permiso para acceder a la historia clínica");
    }
}