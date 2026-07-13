package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.dto.AdminUserNotificationRequestDto;
import com.centropsicologico.sistema.entity.Psychologist;
import com.centropsicologico.sistema.entity.User;
import com.centropsicologico.sistema.exception.BusinessRuleException;
import com.centropsicologico.sistema.exception.ResourceNotFoundException;
import com.centropsicologico.sistema.repository.PsychologistRepository;
import com.centropsicologico.sistema.repository.UserRepository;
import com.centropsicologico.sistema.service.AuditLogService;
import com.centropsicologico.sistema.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin-notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final UserRepository userRepository;
    private final PsychologistRepository psychologistRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @PostMapping("/users/{userId}")
    public Map<String, Object> sendNotificationToUser(
            @PathVariable Long userId,
            @RequestBody AdminUserNotificationRequestDto request
    ) {
        validateRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new BusinessRuleException("No se puede enviar una notificación a un usuario inactivo");
        }

        if (!hasText(user.getEmail())) {
            throw new BusinessRuleException("El usuario no tiene correo registrado para recibir notificaciones");
        }

        notificationService.createForUser(
                request.getTitle().trim(),
                request.getMessage().trim(),
                "MENSAJE_ADMIN",
                user.getEmail()
        );

        auditLogService.record(
                "USUARIOS",
                "NOTIFICACIÓN DIRECTA",
                "User",
                user.getId(),
                "Se envió una notificación directa al usuario "
                        + valueOrDefault(user.getFirstName(), "")
                        + " "
                        + valueOrDefault(user.getLastName(), "")
                        + " ("
                        + user.getEmail()
                        + ")"
        );

        return Map.of(
                "success", true,
                "message", "Notificación enviada correctamente"
        );
    }

    @PostMapping("/psychologists/{psychologistId}")
    public Map<String, Object> sendNotificationToPsychologist(
            @PathVariable Long psychologistId,
            @RequestBody AdminUserNotificationRequestDto request
    ) {
        validateRequest(request);

        Psychologist psychologist = psychologistRepository.findById(psychologistId)
                .orElseThrow(() -> new ResourceNotFoundException("Psicólogo no encontrado"));

        if (Boolean.FALSE.equals(psychologist.getActive())) {
            throw new BusinessRuleException("No se puede enviar una notificación a un psicólogo inactivo");
        }

        if (!hasText(psychologist.getEmail())) {
            throw new BusinessRuleException("El psicólogo no tiene correo registrado");
        }

        User user = userRepository.findByEmail(psychologist.getEmail())
                .orElseThrow(() -> new BusinessRuleException(
                        "El psicólogo tiene correo registrado, pero no existe como usuario del sistema. "
                                + "Debe tener una cuenta de usuario para recibir notificaciones en la campana."
                ));

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new BusinessRuleException("El usuario asociado al psicólogo se encuentra inactivo");
        }

        notificationService.createForUser(
                request.getTitle().trim(),
                request.getMessage().trim(),
                "MENSAJE_ADMIN_PSICOLOGO",
                psychologist.getEmail()
        );

        auditLogService.record(
                "PSICÓLOGOS",
                "NOTIFICACIÓN DIRECTA",
                "Psychologist",
                psychologist.getId(),
                "Se envió una notificación directa al psicólogo "
                        + valueOrDefault(psychologist.getFirstName(), "")
                        + " "
                        + valueOrDefault(psychologist.getLastName(), "")
                        + " ("
                        + psychologist.getEmail()
                        + ")"
        );

        return Map.of(
                "success", true,
                "message", "Notificación enviada correctamente al psicólogo"
        );
    }

    private void validateRequest(AdminUserNotificationRequestDto request) {
        if (request == null) {
            throw new BusinessRuleException("La solicitud no puede estar vacía");
        }

        if (!hasText(request.getTitle())) {
            throw new BusinessRuleException("Debe ingresar el asunto de la notificación");
        }

        if (!hasText(request.getMessage())) {
            throw new BusinessRuleException("Debe ingresar el mensaje de la notificación");
        }

        if (request.getTitle().trim().length() < 4) {
            throw new BusinessRuleException("El asunto debe tener al menos 4 caracteres");
        }

        if (request.getMessage().trim().length() < 8) {
            throw new BusinessRuleException("El mensaje debe tener al menos 8 caracteres");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String valueOrDefault(String value, String defaultValue) {
        return hasText(value) ? value.trim() : defaultValue;
    }
}