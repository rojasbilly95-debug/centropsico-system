package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.Psychologist;
import com.centropsicologico.sistema.exception.ResourceNotFoundException;
import com.centropsicologico.sistema.repository.PsychologistAvailabilityRepository;
import com.centropsicologico.sistema.repository.PsychologistRepository;
import com.centropsicologico.sistema.repository.UserRepository;
import com.centropsicologico.sistema.service.AuditLogService;
import com.centropsicologico.sistema.service.NotificationService;
import com.centropsicologico.sistema.service.PsychologistService;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PsychologistServiceImpl
        implements PsychologistService {

    private final PsychologistRepository psychologistRepository;
    private final PsychologistAvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public PsychologistServiceImpl(
            PsychologistRepository psychologistRepository,
            PsychologistAvailabilityRepository availabilityRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            AuditLogService auditLogService
    ) {
        this.psychologistRepository =
                psychologistRepository;

        this.availabilityRepository =
                availabilityRepository;

        this.userRepository =
                userRepository;

        this.notificationService =
                notificationService;

        this.auditLogService =
                auditLogService;
    }

    /*
     * =========================================================
     * REGISTRAR PSICÓLOGO
     * =========================================================
     */

    @Override
    public Psychologist save(
            Psychologist psychologist
    ) {
        psychologist.setActive(true);

        Psychologist saved =
                psychologistRepository.save(
                        psychologist
                );

        completePsychologistInformation(saved);

        notifyAdmin(
                "Psicólogo registrado",
                "Se registró al psicólogo "
                        + getFullName(saved)
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
                        + safe(
                                saved.getSpecialty(),
                                "No especificada"
                        )
        );

        return saved;
    }

    /*
     * =========================================================
     * LISTAR TODOS LOS PSICÓLOGOS
     * =========================================================
     */

    @Override
    public List<Psychologist> findAll() {
        List<Psychologist> psychologists =
                psychologistRepository.findAll();

        psychologists.forEach(
                this::completePsychologistInformation
        );

        return psychologists;
    }

    /*
     * =========================================================
     * LISTAR PSICÓLOGOS ACTIVOS
     * =========================================================
     */

    @Override
    public List<Psychologist>
    findActivePsychologists() {

        List<Psychologist> psychologists =
                psychologistRepository
                        .findByActiveTrue();

        psychologists.forEach(
                this::completePsychologistInformation
        );

        return psychologists;
    }

    /*
     * =========================================================
     * BUSCAR PSICÓLOGO POR ID
     * =========================================================
     */

    @Override
    public Psychologist findById(Long id) {
        Psychologist psychologist =
                psychologistRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Psicólogo no encontrado con id: "
                                                + id
                                )
                        );

        completePsychologistInformation(
                psychologist
        );

        return psychologist;
    }

    /*
     * =========================================================
     * ACTUALIZAR PSICÓLOGO
     * =========================================================
     */

    @Override
    public Psychologist update(
            Long id,
            Psychologist psychologist
    ) {
        Psychologist currentPsychologist =
                findById(id);

        currentPsychologist.setFirstName(
                psychologist.getFirstName()
        );

        currentPsychologist.setLastName(
                psychologist.getLastName()
        );

        currentPsychologist.setSpecialty(
                psychologist.getSpecialty()
        );

        currentPsychologist.setPhone(
                psychologist.getPhone()
        );

        currentPsychologist.setEmail(
                psychologist.getEmail()
        );

        if (psychologist.getActive() != null) {
            currentPsychologist.setActive(
                    psychologist.getActive()
            );
        }

        Psychologist updated =
                psychologistRepository.save(
                        currentPsychologist
                );

        completePsychologistInformation(
                updated
        );

        notifyAdmin(
                "Psicólogo actualizado",
                "Se actualizaron los datos del psicólogo "
                        + getFullName(updated)
                        + ".",
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
                        + safe(
                                updated.getSpecialty(),
                                "No especificada"
                        )
        );

        return updated;
    }

    /*
     * =========================================================
     * ACTIVAR O DESACTIVAR PSICÓLOGO
     * =========================================================
     */

    @Override
    public Psychologist toggleActive(Long id) {
        Psychologist psychologist =
                findById(id);

        psychologist.setActive(
                !Boolean.TRUE.equals(
                        psychologist.getActive()
                )
        );

        Psychologist updated =
                psychologistRepository.save(
                        psychologist
                );

        completePsychologistInformation(
                updated
        );

        String status =
                Boolean.TRUE.equals(
                        updated.getActive()
                )
                        ? "reactivado"
                        : "desactivado";

        notifyAdmin(
                "Estado de psicólogo modificado",
                "El psicólogo "
                        + getFullName(updated)
                        + " fue "
                        + status
                        + ".",
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

    /*
     * =========================================================
     * DESACTIVAR PSICÓLOGO
     * =========================================================
     */

    @Override
    public void delete(Long id) {
        Psychologist psychologist =
                findById(id);

        psychologist.setActive(false);

        Psychologist updated =
                psychologistRepository.save(
                        psychologist
                );

        completePsychologistInformation(
                updated
        );

        notifyAdmin(
                "Psicólogo desactivado",
                "El psicólogo "
                        + getFullName(updated)
                        + " fue desactivado.",
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

    /*
     * =========================================================
     * COMPLETAR INFORMACIÓN ADICIONAL
     * =========================================================
     */

    private void completePsychologistInformation(
            Psychologist psychologist
    ) {
        if (psychologist == null) {
            return;
        }

        setAvailabilityCount(
                psychologist
        );

        setProfileImage(
                psychologist
        );
    }

    /*
     * =========================================================
     * CONTAR DISPONIBILIDADES ACTIVAS
     * =========================================================
     */

    private void setAvailabilityCount(
            Psychologist psychologist
    ) {
        if (
                psychologist == null ||
                psychologist.getId() == null
        ) {
            return;
        }

        long count =
                availabilityRepository
                        .countByPsychologistIdAndActiveTrue(
                                psychologist.getId()
                        );

        psychologist.setAvailabilityCount(
                count
        );
    }

    /*
     * =========================================================
     * OBTENER FOTO DESDE LA CUENTA DE USUARIO
     * =========================================================
     */

    private void setProfileImage(
            Psychologist psychologist
    ) {
        /*
         * Valores iniciales por si no existe
         * una cuenta de usuario relacionada.
         */
        psychologist.setProfileImageBase64("");
        psychologist.setProfileImageUrl("");

        String psychologistEmail =
                psychologist.getEmail();

        if (
                psychologistEmail == null ||
                psychologistEmail.isBlank()
        ) {
            return;
        }

        /*
         * Busca una cuenta de usuario que tenga
         * el mismo correo del psicólogo.
         */
        userRepository
                .findByEmailIgnoreCase(
                        psychologistEmail.trim()
                )
                .ifPresent(user -> {

                    psychologist.setProfileImageBase64(
                            user.getProfileImageBase64()
                    );

                    psychologist.setProfileImageUrl(
                            user.getProfileImageUrl()
                    );
                });
    }

    /*
     * =========================================================
     * OBTENER NOMBRE COMPLETO
     * =========================================================
     */

    private String getFullName(
            Psychologist psychologist
    ) {
        if (psychologist == null) {
            return "Psicólogo";
        }

        String firstName =
                psychologist.getFirstName() != null
                        ? psychologist.getFirstName()
                        : "";

        String lastName =
                psychologist.getLastName() != null
                        ? psychologist.getLastName()
                        : "";

        String fullName =
                (firstName + " " + lastName)
                        .trim();

        return fullName.isEmpty()
                ? "Psicólogo"
                : fullName;
    }

    /*
     * =========================================================
     * EVITAR VALORES VACÍOS
     * =========================================================
     */

    private String safe(
            String value,
            String defaultValue
    ) {
        if (
                value == null ||
                value.trim().isEmpty()
        ) {
            return defaultValue;
        }

        return value.trim();
    }

    /*
     * =========================================================
     * NOTIFICAR AL ADMINISTRADOR
     * =========================================================
     */

    private void notifyAdmin(
            String title,
            String message,
            String type
    ) {
        notificationService.createForRole(
                title,
                message,
                type,
                "ADMIN"
        );
    }
}