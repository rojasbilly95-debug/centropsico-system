package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByActiveTrueOrderByCreatedAtDesc();

    List<AuditLog> findTop50ByActiveTrueOrderByCreatedAtDesc();

    List<AuditLog> findByModuleAndActiveTrueOrderByCreatedAtDesc(String module);

    List<AuditLog> findByUserEmailAndActiveTrueOrderByCreatedAtDesc(String userEmail);

    Page<AuditLog> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
            SELECT a
            FROM AuditLog a
            WHERE a.active = true
              AND (:module IS NULL OR :module = '' OR a.module = :module)
              AND (:severity IS NULL OR :severity = '' OR a.severity = :severity)
              AND (:reviewed IS NULL OR a.reviewed = :reviewed)
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(a.module) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(a.action) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(a.description) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(a.userEmail) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(a.userRole) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(a.entityName) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> searchAuditLogs(
            String module,
            String severity,
            Boolean reviewed,
            String search,
            Pageable pageable
    );

    Long countByActiveTrueAndReviewedFalse();

    Long countByActiveTrueAndReviewedFalseAndSeverity(String severity);
}