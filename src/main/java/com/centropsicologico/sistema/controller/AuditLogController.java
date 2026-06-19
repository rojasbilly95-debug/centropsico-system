package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.entity.AuditLog;
import com.centropsicologico.sistema.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/recent")
    public List<AuditLog> findRecent() {
        return auditLogService.findRecent();
    }

    @GetMapping
    public List<AuditLog> findAll() {
        return auditLogService.findAll();
    }

    @GetMapping("/page")
    public Page<AuditLog> search(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Boolean reviewed,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return auditLogService.search(module, severity, reviewed, search, page, size);
    }

    @GetMapping("/module/{module}")
    public List<AuditLog> findByModule(@PathVariable String module) {
        return auditLogService.findByModule(module);
    }

    @PatchMapping("/{id}/review")
    public AuditLog markAsReviewed(@PathVariable Long id) {
        return auditLogService.markAsReviewed(id);
    }

    @PatchMapping("/{id}/pending")
    public AuditLog markAsPending(@PathVariable Long id) {
        return auditLogService.markAsPending(id);
    }
}