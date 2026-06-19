package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.dto.LeadRequestDto;
import com.centropsicologico.sistema.entity.Lead;
import com.centropsicologico.sistema.entity.ServiceEntity;
import com.centropsicologico.sistema.repository.LeadRepository;
import com.centropsicologico.sistema.repository.ServiceRepository;
import com.centropsicologico.sistema.service.AuditLogService;
import com.centropsicologico.sistema.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicPortalController {

    private static final String CONSENT_VERSION = "PORTAL_PRIVACIDAD_V1";
    private static final double DEFAULT_ADVANCE_PERCENT = 20.0;

    private final LeadRepository leadRepository;
    private final NotificationService notificationService;
    private final ServiceRepository serviceRepository;
    private final AuditLogService auditLogService;

    public PublicPortalController(
            LeadRepository leadRepository,
            NotificationService notificationService,
            ServiceRepository serviceRepository,
            AuditLogService auditLogService
    ) {
        this.leadRepository = leadRepository;
        this.notificationService = notificationService;
        this.serviceRepository = serviceRepository;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/leads")
    public Map<String, Object> registerLead(@RequestBody LeadRequestDto request) {

        validateLead(request);

        Lead lead = new Lead();

        lead.setFullName(clean(request.getFullName()));
        lead.setEmail(clean(request.getEmail()));
        lead.setPhone(clean(request.getPhone()));

        lead.setServiceId(request.getServiceId());
        lead.setServiceInterest(clean(request.getServiceInterest()));

        if (request.getServiceId() != null) {
            ServiceEntity service = serviceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new RuntimeException("El servicio seleccionado no existe"));

            if (!Boolean.TRUE.equals(service.getActive())) {
                throw new RuntimeException("El servicio seleccionado no está disponible");
            }

            lead.setServiceId(service.getId());
            lead.setServiceInterest(service.getName());
            lead.setServicePrice(service.getPrice());

            double advancePercent = request.getAdvancePercent() != null
                    ? request.getAdvancePercent()
                    : DEFAULT_ADVANCE_PERCENT;

            lead.setAdvancePercent(advancePercent);

            double price = service.getPrice() != null ? service.getPrice() : 0.0;
            lead.setAdvanceAmount(roundAmount(price * (advancePercent / 100.0)));

        } else {
            lead.setServicePrice(request.getServicePrice());
            lead.setAdvancePercent(request.getAdvancePercent());
            lead.setAdvanceAmount(request.getAdvanceAmount());
        }

        lead.setModality(clean(request.getModality()));

        lead.setPsychologistName(clean(request.getPsychologistName()));
        lead.setPsychologistId(request.getPsychologistId());

        lead.setPreferredDate(clean(request.getPreferredDate()));
        lead.setPreferredTime(clean(request.getPreferredTime()));

        lead.setPaymentMethod(clean(request.getPaymentMethod()));
        lead.setOperationCode(clean(request.getOperationCode()));

        if (hasText(lead.getPaymentMethod()) && hasText(lead.getOperationCode())) {
            lead.setPaymentStatus("PAGO_EN_REVISION");
            lead.setStatus("PAGO_EN_REVISION");
        } else {
            lead.setPaymentStatus("PENDIENTE");
            lead.setStatus("NUEVO");
        }

        lead.setMessage(clean(request.getMessage()));
        lead.setCreatedAt(LocalDateTime.now());

        lead.setConsentAccepted(true);
        lead.setConsentDate(LocalDateTime.now());
        lead.setConsentVersion(
                hasText(request.getConsentVersion())
                        ? clean(request.getConsentVersion())
                        : CONSENT_VERSION
        );

        Lead saved = leadRepository.save(lead);

        notifyRoles(
                "Nueva pre-reserva de atención",
                "Se registró una nueva pre-reserva de "
                        + saved.getFullName()
                        + " para "
                        + saved.getServiceInterest()
                        + ". Fecha: "
                        + valueOrDefault(saved.getPreferredDate(), "por coordinar")
                        + ", hora: "
                        + valueOrDefault(saved.getPreferredTime(), "por coordinar")
                        + ". Adelanto: S/ "
                        + formatAmount(saved.getAdvanceAmount())
                        + "."
        );

        auditLogService.recordSecurity(
                "PRE-RESERVAS",
                "REGISTRO DESDE PORTAL",
                "Lead",
                saved.getId(),
                "Se registró una pre-reserva desde el portal público para "
                        + saved.getFullName()
                        + ". Consentimiento: "
                        + saved.getConsentVersion(),
                saved.getEmail(),
                "VISITANTE",
                "INFO",
                false
        );

        return Map.of(
                "success", true,
                "message", "Pre-reserva registrada correctamente",
                "leadId", saved.getId()
        );
    }

    @GetMapping("/services")
    public List<Map<String, Object>> getPublicServices() {
        return serviceRepository.findByActiveTrue()
                .stream()
                .map(this::buildServiceResponse)
                .toList();
    }

    @GetMapping("/plans")
    public List<Map<String, Object>> getPlans() {
        return serviceRepository.findByActiveTrue()
                .stream()
                .map(service -> {
                    Map<String, Object> map = new LinkedHashMap<>();

                    map.put("id", service.getId());
                    map.put("name", service.getName());
                    map.put("description", valueOrDefault(service.getDescription(), "Servicio psicológico disponible."));
                    map.put("price", service.getPrice() != null ? service.getPrice() : 0.0);
                    map.put("durationMinutes", service.getDurationMinutes());
                    map.put("features", List.of(
                            "Atención profesional",
                            "Modalidad presencial o virtual",
                            "Orientación personalizada",
                            "Proceso confidencial"
                    ));

                    return map;
                })
                .toList();
    }

    private Map<String, Object> buildServiceResponse(ServiceEntity service) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", service.getId());
        map.put("name", service.getName());
        map.put("description", valueOrDefault(service.getDescription(), ""));
        map.put("price", service.getPrice() != null ? service.getPrice() : 0.0);
        map.put("durationMinutes", service.getDurationMinutes());
        map.put("active", service.getActive());

        return map;
    }

    private void validateLead(LeadRequestDto request) {
        if (!hasText(request.getFullName())) {
            throw new RuntimeException("Debe ingresar su nombre completo");
        }

        if (!hasText(request.getEmail())) {
            throw new RuntimeException("Debe ingresar su correo electrónico");
        }

        if (!request.getEmail().contains("@")) {
            throw new RuntimeException("Debe ingresar un correo electrónico válido");
        }

        if (!hasText(request.getPhone())) {
            throw new RuntimeException("Debe ingresar su teléfono o WhatsApp");
        }

        if (!hasText(request.getServiceInterest()) && request.getServiceId() == null) {
            throw new RuntimeException("Debe seleccionar el tipo de atención");
        }

        if (!hasText(request.getModality())) {
            throw new RuntimeException("Debe seleccionar la modalidad de atención");
        }

        if (!hasText(request.getPaymentMethod())) {
            throw new RuntimeException("Debe seleccionar el método de pago del adelanto");
        }

        if (!hasText(request.getOperationCode())) {
            throw new RuntimeException("Debe ingresar el código de operación del adelanto");
        }

        if (!Boolean.TRUE.equals(request.getConsentAccepted())) {
            throw new RuntimeException("Debe aceptar el tratamiento de datos personales para registrar la pre-reserva");
        }
    }

    private void notifyRoles(String title, String message) {
        notificationService.createForRole(
                title,
                message,
                "PRE_RESERVA",
                "ADMIN"
        );

        notificationService.createForRole(
                title,
                message,
                "PRE_RESERVA",
                "RECEPCIONISTA"
        );
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isEmpty() ? null : cleaned;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String valueOrDefault(String value, String defaultValue) {
        return hasText(value) ? value : defaultValue;
    }

    private Double roundAmount(Double amount) {
        if (amount == null) {
            return 0.0;
        }

        return Math.round(amount * 100.0) / 100.0;
    }

    private String formatAmount(Double amount) {
        if (amount == null) {
            return "0.00";
        }

        return String.format("%.2f", amount);
    }
}