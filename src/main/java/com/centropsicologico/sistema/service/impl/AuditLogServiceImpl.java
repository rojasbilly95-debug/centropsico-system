package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.AuditLog;
import com.centropsicologico.sistema.exception.ResourceNotFoundException;
import com.centropsicologico.sistema.repository.AuditLogRepository;
import com.centropsicologico.sistema.service.AuditLogService;
import com.centropsicologico.sistema.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;

    public AuditLogServiceImpl(
            AuditLogRepository auditLogRepository,
            NotificationService notificationService
    ) {
        this.auditLogRepository = auditLogRepository;
        this.notificationService = notificationService;
    }

    @Override
    public AuditLog record(
            String module,
            String action,
            String entityName,
            Long entityId,
            String description
    ) {
        return recordWithSeverity(module, action, entityName, entityId, description, "INFO");
    }

    @Override
    public AuditLog recordWithSeverity(
            String module,
            String action,
            String entityName,
            Long entityId,
            String description,
            String severity
    ) {
        AuditLog auditLog = buildAuditLog(
                module,
                action,
                entityName,
                entityId,
                description,
                getCurrentUserEmail(),
                getCurrentUserRole(),
                normalizeSeverity(severity),
                false,
                isImportantSeverity(severity)
        );

        return auditLogRepository.save(auditLog);
    }

    @Override
    public AuditLog recordAndNotifyAdmin(
            String module,
            String action,
            String entityName,
            Long entityId,
            String description
    ) {
        AuditLog auditLog = buildAuditLog(
                module,
                action,
                entityName,
                entityId,
                description,
                getCurrentUserEmail(),
                getCurrentUserRole(),
                "CRITICAL",
                false,
                true
        );

        AuditLog saved = auditLogRepository.save(auditLog);

        return notifyAdmin(saved);
    }

    @Override
    public AuditLog recordSecurity(
            String module,
            String action,
            String entityName,
            Long entityId,
            String description,
            String userEmail,
            String userRole,
            String severity,
            boolean notifyAdmin
    ) {
        String normalizedSeverity = normalizeSeverity(severity);

        AuditLog auditLog = buildAuditLog(
                module,
                action,
                entityName,
                entityId,
                description,
                userEmail,
                userRole,
                normalizedSeverity,
                false,
                isImportantSeverity(normalizedSeverity)
        );

        AuditLog saved = auditLogRepository.save(auditLog);

        if (notifyAdmin && "CRITICAL".equals(normalizedSeverity)) {
            return notifyAdmin(saved);
        }

        return saved;
    }

    @Override
    public List<AuditLog> findRecent() {
        return auditLogRepository.findTop50ByActiveTrueOrderByCreatedAtDesc();
    }

    @Override
    public List<AuditLog> findAll() {
        return auditLogRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    @Override
    public List<AuditLog> findByModule(String module) {
        return auditLogRepository.findByModuleAndActiveTrueOrderByCreatedAtDesc(module);
    }

    @Override
    public Page<AuditLog> search(
            String module,
            String severity,
            Boolean reviewed,
            String search,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : Math.min(size, 100);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return auditLogRepository.searchAuditLogs(
                clean(module),
                clean(severity),
                reviewed,
                clean(search),
                pageable
        );
    }

    @Override
    public AuditLog markAsReviewed(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con id: " + id));

        auditLog.setReviewed(true);
        return auditLogRepository.save(auditLog);
    }

    @Override
    public AuditLog markAsPending(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con id: " + id));

        auditLog.setReviewed(false);
        return auditLogRepository.save(auditLog);
    }

    private AuditLog buildAuditLog(
            String module,
            String action,
            String entityName,
            Long entityId,
            String description,
            String userEmail,
            String userRole,
            String severity,
            Boolean adminNotified,
            Boolean important
    ) {
        AuditLog auditLog = new AuditLog();

        auditLog.setModule(limit(safe(module, "GENERAL"), 80));
        auditLog.setAction(limit(safe(action, "MOVIMIENTO"), 120));
        auditLog.setEntityName(limit(safe(entityName, "SIN_ENTIDAD"), 120));
        auditLog.setEntityId(entityId);
        auditLog.setDescription(limit(safe(description, "Movimiento registrado en el sistema."), 700));
        auditLog.setUserEmail(limit(safe(userEmail, "sistema"), 150));
        auditLog.setUserRole(limit(safe(userRole, "SISTEMA"), 60));
        auditLog.setSeverity(normalizeSeverity(severity));
        auditLog.setReviewed(false);
        auditLog.setAdminNotified(Boolean.TRUE.equals(adminNotified));
        auditLog.setImportant(Boolean.TRUE.equals(important));
        auditLog.setActive(true);
        auditLog.setCreatedAt(LocalDateTime.now());

        return auditLog;
    }

    private AuditLog notifyAdmin(AuditLog auditLog) {
        try {
            notificationService.createForRole(
                    "Movimiento crítico",
                    buildNotificationMessage(auditLog),
                    "AUDITORIA_" + auditLog.getSeverity(),
                    "ADMIN"
            );

            auditLog.setAdminNotified(true);
            return auditLogRepository.save(auditLog);

        } catch (Exception exception) {
            System.err.println("No se pudo notificar auditoría al ADMIN: " + exception.getMessage());
            return auditLog;
        }
    }

    private String buildNotificationMessage(AuditLog auditLog) {
        return auditLog.getUserEmail()
                + " realizó: "
                + auditLog.getAction()
                + " en "
                + auditLog.getModule()
                + ". "
                + auditLog.getDescription();
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return "sistema";
        }

        return authentication.getName();
    }

    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getAuthorities() == null) {
            return "SISTEMA";
        }

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .findFirst()
                .orElse("SISTEMA");
    }

    private boolean isImportantSeverity(String severity) {
        String normalized = normalizeSeverity(severity);
        return "WARNING".equals(normalized) || "CRITICAL".equals(normalized);
    }

    private String normalizeSeverity(String severity) {
        if (severity == null || severity.isBlank()) {
            return "INFO";
        }

        String value = severity.trim().toUpperCase();

        if (!List.of("INFO", "WARNING", "CRITICAL").contains(value)) {
            return "INFO";
        }

        return value;
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String safe(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim();
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength - 3) + "...";
    }
}