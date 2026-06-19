package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.entity.AuditLog;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AuditLogService {

    AuditLog record(
            String module,
            String action,
            String entityName,
            Long entityId,
            String description
    );

    AuditLog recordWithSeverity(
            String module,
            String action,
            String entityName,
            Long entityId,
            String description,
            String severity
    );

    AuditLog recordAndNotifyAdmin(
            String module,
            String action,
            String entityName,
            Long entityId,
            String description
    );

    default AuditLog recordSecurity(
            String module,
            String action,
            String entityName,
            Long entityId,
            String description,
            String userEmail,
            String userRole,
            boolean notifyAdmin
    ) {
        return recordSecurity(
                module,
                action,
                entityName,
                entityId,
                description,
                userEmail,
                userRole,
                notifyAdmin ? "CRITICAL" : "WARNING",
                notifyAdmin
        );
    }

    AuditLog recordSecurity(
            String module,
            String action,
            String entityName,
            Long entityId,
            String description,
            String userEmail,
            String userRole,
            String severity,
            boolean notifyAdmin
    );

    List<AuditLog> findRecent();

    List<AuditLog> findAll();

    List<AuditLog> findByModule(String module);

    Page<AuditLog> search(
            String module,
            String severity,
            Boolean reviewed,
            String search,
            int page,
            int size
    );

    AuditLog markAsReviewed(Long id);

    AuditLog markAsPending(Long id);
}