package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.Patient;
import com.centropsicologico.sistema.exception.BusinessRuleException;
import com.centropsicologico.sistema.exception.ResourceNotFoundException;
import com.centropsicologico.sistema.repository.PatientRepository;
import com.centropsicologico.sistema.service.NotificationService;
import com.centropsicologico.sistema.service.PatientService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final NotificationService notificationService;

    public PatientServiceImpl(
            PatientRepository patientRepository,
            NotificationService notificationService) {

        this.patientRepository = patientRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Patient save(Patient patient) {

        if (patientRepository.existsByDni(patient.getDni())) {
            throw new BusinessRuleException("Ya existe un paciente con ese DNI");
        }

        patient.setActive(true);

        Patient savedPatient = patientRepository.save(patient);

        String message = "Se registró al paciente "
                + savedPatient.getFirstName() + " "
                + savedPatient.getLastName()
                + " con DNI "
                + savedPatient.getDni();

        notificationService.createForRole(
                "Nuevo paciente registrado",
                message,
                "PACIENTE_CREADO",
                "ADMIN"
        );

        notificationService.createForRole(
                "Nuevo paciente registrado",
                message,
                "PACIENTE_CREADO",
                "RECEPCIONISTA"
        );

        return savedPatient;
    }

    @Override
    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    @Override
    public List<Patient> findActivePatients() {
        return patientRepository.findByActiveTrue();
    }

    @Override
    public Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Paciente no encontrado con id: " + id));
    }

    @Override
    public Patient update(Long id, Patient patient) {

        Patient currentPatient = findById(id);

        if (!currentPatient.getDni().equals(patient.getDni())
                && patientRepository.existsByDni(patient.getDni())) {

            throw new BusinessRuleException("Ya existe otro paciente con ese DNI");
        }

        currentPatient.setFirstName(patient.getFirstName());
        currentPatient.setLastName(patient.getLastName());
        currentPatient.setDni(patient.getDni());
        currentPatient.setBirthDate(patient.getBirthDate());
        currentPatient.setGender(patient.getGender());
        currentPatient.setPhone(patient.getPhone());
        currentPatient.setEmail(patient.getEmail());
        currentPatient.setAddress(patient.getAddress());
        currentPatient.setEmergencyContact(patient.getEmergencyContact());
        currentPatient.setEmergencyPhone(patient.getEmergencyPhone());

        if (patient.getActive() != null) {
            currentPatient.setActive(patient.getActive());
        }

        Patient updatedPatient = patientRepository.save(currentPatient);

        String message = "Se actualizó el paciente "
                + updatedPatient.getFirstName() + " "
                + updatedPatient.getLastName();

        notificationService.createForRole(
                "Paciente actualizado",
                message,
                "PACIENTE_EDITADO",
                "ADMIN"
        );

        return updatedPatient;
    }

    @Override
    public Patient toggleActive(Long id) {

        Patient patient = findById(id);

        patient.setActive(!patient.getActive());

        Patient updatedPatient = patientRepository.save(patient);

        String status = updatedPatient.getActive()
                ? "activado"
                : "desactivado";

        notificationService.createForRole(
                "Estado de paciente modificado",
                "El paciente "
                        + updatedPatient.getFirstName()
                        + " "
                        + updatedPatient.getLastName()
                        + " fue "
                        + status,
                "PACIENTE_ESTADO",
                "ADMIN"
        );

        return updatedPatient;
    }

    @Override
    public void delete(Long id) {

        Patient patient = findById(id);

        patient.setActive(false);

        patientRepository.save(patient);

        notificationService.createForRole(
                "Paciente desactivado",
                "El paciente "
                        + patient.getFirstName()
                        + " "
                        + patient.getLastName()
                        + " fue desactivado",
                "PACIENTE_ELIMINADO",
                "ADMIN"
        );
    }
}