package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.Psychologist;
import com.centropsicologico.sistema.exception.ResourceNotFoundException;
import com.centropsicologico.sistema.repository.PsychologistAvailabilityRepository;
import com.centropsicologico.sistema.repository.PsychologistRepository;
import com.centropsicologico.sistema.service.AuditLogService;
import com.centropsicologico.sistema.service.NotificationService;
import com.centropsicologico.sistema.service.PsychologistService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PsychologistServiceImpl implements PsychologistService {

    private final PsychologistRepository psychologistRepository;
    private final PsychologistAvailabilityRepository availabilityRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public PsychologistServiceImpl(
            PsychologistRepository psychologistRepository,
            PsychologistAvailabilityRepository availabilityRepository,
            NotificationService notificationService,
            AuditLogService auditLogService) {

        this.psychologistRepository = psychologistRepository;
        this.availabilityRepository = availabilityRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Override
    public Psychologist save(Psychologist psychologist) {
        psychologist.setActive(true);

        Psychologist saved = psychologistRepository.save(psychologist);
        setAvailabilityCount(saved);

        notifyAdmin(
                "Psicólogo registrado",
                "Se registró al psicólogo " + getFullName(saved)
                        + ". Ahora debe configurarse su disponibilidad.",
                "PSICOLOGO_CREADO"
        );

        auditLogService.record(
                "PSICÓLOGOS",
                "REGISTRO DE PSICÓLOGO",
                "Psychologist",
                saved.getId(),
                "Se registró al psicólogo "
                        + getFullName(saved)
                        + ". Especialidad: "
                        + safe(saved.getSpecialty(), "No especificada")
        );

        return saved;
    }

    @Override
    public List<Psychologist> findAll() {
        List<Psychologist> psychologists = psychologistRepository.findAll();
        psychologists.forEach(this::setAvailabilityCount);
        return psychologists;
    }

    @Override
    public List<Psychologist> findActivePsychologists() {
        List<Psychologist> psychologists = psychologistRepository.findByActiveTrue();
        psychologists.forEach(this::setAvailabilityCount);
        return psychologists;
    }

    @Override
    public Psychologist findById(Long id) {
        Psychologist psychologist = psychologistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Psicólogo no encontrado con id: " + id));

        setAvailabilityCount(psychologist);
        return psychologist;
    }

    @Override
    public Psychologist update(Long id, Psychologist psychologist) {
        Psychologist currentPsychologist = findById(id);

        currentPsychologist.setFirstName(psychologist.getFirstName());
        currentPsychologist.setLastName(psychologist.getLastName());
        currentPsychologist.setSpecialty(psychologist.getSpecialty());
        currentPsychologist.setPhone(psychologist.getPhone());
        currentPsychologist.setEmail(psychologist.getEmail());

        if (psychologist.getActive() != null) {
            currentPsychologist.setActive(psychologist.getActive());
        }

        Psychologist updated = psychologistRepository.save(currentPsychologist);
        setAvailabilityCount(updated);

        notifyAdmin(
                "Psicólogo actualizado",
                "Se actualizaron los datos del psicólogo " + getFullName(updated) + ".",
                "PSICOLOGO_EDITADO"
        );

        auditLogService.record(
                "PSICÓLOGOS",
                "ACTUALIZACIÓN DE PSICÓLOGO",
                "Psychologist",
                updated.getId(),
                "Se actualizaron los datos del psicólogo "
                        + getFullName(updated)
                        + ". Especialidad: "
                        + safe(updated.getSpecialty(), "No especificada")
        );

        return updated;
    }

    @Override
    public Psychologist toggleActive(Long id) {
        Psychologist psychologist = findById(id);
        psychologist.setActive(!Boolean.TRUE.equals(psychologist.getActive()));

        Psychologist updated = psychologistRepository.save(psychologist);
        setAvailabilityCount(updated);

        String status = Boolean.TRUE.equals(updated.getActive())
                ? "reactivado"
                : "desactivado";

        notifyAdmin(
                "Estado de psicólogo modificado",
                "El psicólogo " + getFullName(updated) + " fue " + status + ".",
                "PSICOLOGO_ESTADO"
        );

        auditLogService.record(
                "PSICÓLOGOS",
                "CAMBIO DE ESTADO DE PSICÓLOGO",
                "Psychologist",
                updated.getId(),
                "El psicólogo "
                        + getFullName(updated)
                        + " fue "
                        + status
        );

        return updated;
    }

    @Override
    public void delete(Long id) {
        Psychologist psychologist = findById(id);
        psychologist.setActive(false);

        Psychologist updated = psychologistRepository.save(psychologist);
        setAvailabilityCount(updated);

        notifyAdmin(
                "Psicólogo desactivado",
                "El psicólogo " + getFullName(updated) + " fue desactivado.",
                "PSICOLOGO_ELIMINADO"
        );

        auditLogService.record(
                "PSICÓLOGOS",
                "DESACTIVACIÓN DE PSICÓLOGO",
                "Psychologist",
                updated.getId(),
                "El psicólogo "
                        + getFullName(updated)
                        + " fue desactivado"
        );
    }

    private void setAvailabilityCount(Psychologist psychologist) {
        if (psychologist == null || psychologist.getId() == null) return;

        long count = availabilityRepository.countByPsychologistIdAndActiveTrue(psychologist.getId());
        psychologist.setAvailabilityCount(count);
    }

    private String getFullName(Psychologist psychologist) {
        if (psychologist == null) return "Psicólogo";

        String firstName = psychologist.getFirstName() != null ? psychologist.getFirstName() : "";
        String lastName = psychologist.getLastName() != null ? psychologist.getLastName() : "";

        String fullName = (firstName + " " + lastName).trim();

        return fullName.isEmpty() ? "Psicólogo" : fullName;
    }

    private String safe(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return value.trim();
    }

    private void notifyAdmin(String title, String message, String type) {
        notificationService.createForRole(
                title,
                message,
                type,
                "ADMIN"
        );
    }
}