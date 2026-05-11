package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.dto.LeadRequestDto;
import com.centropsicologico.sistema.entity.Lead;
import com.centropsicologico.sistema.repository.LeadRepository;
import com.centropsicologico.sistema.repository.ServiceRepository;
import com.centropsicologico.sistema.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicPortalController {

    private final LeadRepository leadRepository;
    private final NotificationService notificationService;
    private final ServiceRepository serviceRepository;

    public PublicPortalController(
            LeadRepository leadRepository,
            NotificationService notificationService,
            ServiceRepository serviceRepository) {

        this.leadRepository = leadRepository;
        this.notificationService = notificationService;
        this.serviceRepository = serviceRepository;
    }

    @PostMapping("/leads")
    public Map<String, Object> registerLead(@RequestBody LeadRequestDto request) {

        validateLead(request);

        Lead lead = new Lead();

        lead.setFullName(clean(request.getFullName()));
        lead.setEmail(clean(request.getEmail()));
        lead.setPhone(clean(request.getPhone()));
        lead.setServiceInterest(clean(request.getServiceInterest()));
        lead.setServiceId(request.getServiceId());
        lead.setModality(clean(request.getModality()));
        lead.setPsychologistName(clean(request.getPsychologistName()));
        lead.setPsychologistId(request.getPsychologistId());
        lead.setPreferredDate(clean(request.getPreferredDate()));
        lead.setPreferredTime(clean(request.getPreferredTime()));
        lead.setServicePrice(request.getServicePrice());
        lead.setAdvancePercent(request.getAdvancePercent());
        lead.setAdvanceAmount(request.getAdvanceAmount());
        lead.setPaymentMethod(clean(request.getPaymentMethod()));
        lead.setOperationCode(clean(request.getOperationCode()));
        lead.setPaymentStatus("PAGO_EN_REVISION");
        lead.setMessage(clean(request.getMessage()));
        lead.setCreatedAt(LocalDateTime.now());
        lead.setStatus("PAGO_EN_REVISION");

        Lead saved = leadRepository.save(lead);

        notificationService.createForRole(
                "Nueva pre-reserva con adelanto",
                "Se registró una nueva pre-reserva de "
                        + saved.getFullName()
                        + " para "
                        + saved.getServiceInterest()
                        + ". Adelanto: S/ "
                        + formatAmount(saved.getAdvanceAmount())
                        + " mediante "
                        + (saved.getPaymentMethod() != null ? saved.getPaymentMethod() : "método no especificado")
                        + ". Código: "
                        + (saved.getOperationCode() != null ? saved.getOperationCode() : "sin código")
                        + ".",
                "PRE_RESERVA_PAGO",
                "ADMIN"
        );

        return Map.of(
                "success", true,
                "message", "Pre-reserva registrada correctamente. El adelanto será validado por recepción.",
                "leadId", saved.getId()
        );
    }

    @GetMapping("/plans")
    public List<Map<String, Object>> getPlans() {

        return List.of(

                Map.of(
                        "name", "Terapia individual",
                        "description", "Espacio personalizado para trabajar emociones, ansiedad, autoestima, estrés y bienestar emocional.",
                        "price", 80.0,
                        "features", List.of(
                                "Atención personalizada",
                                "Orientación emocional",
                                "Modalidad presencial o virtual",
                                "Acompañamiento profesional"
                        )
                ),

                Map.of(
                        "name", "Terapia de pareja",
                        "description", "Acompañamiento para fortalecer la comunicación y mejorar vínculos afectivos.",
                        "price", 120.0,
                        "features", List.of(
                                "Orientación profesional",
                                "Resolución de conflictos",
                                "Fortalecimiento emocional",
                                "Sesión especializada"
                        )
                ),

                Map.of(
                        "name", "Terapia familiar",
                        "description", "Orientación psicológica enfocada en mejorar la convivencia y dinámica familiar.",
                        "price", 130.0,
                        "features", List.of(
                                "Atención familiar",
                                "Comunicación y convivencia",
                                "Acompañamiento psicológico",
                                "Intervención especializada"
                        )
                ),

                Map.of(
                        "name", "Terapia de lenguaje",
                        "description", "Atención especializada para fortalecer habilidades de comunicación y lenguaje.",
                        "price", 100.0,
                        "features", List.of(
                                "Estimulación del lenguaje",
                                "Atención personalizada",
                                "Orientación infantil",
                                "Seguimiento especializado"
                        )
                ),

                Map.of(
                        "name", "Evaluación psicológica",
                        "description", "Proceso de evaluación mediante entrevistas y herramientas psicológicas.",
                        "price", 150.0,
                        "features", List.of(
                                "Entrevista inicial",
                                "Aplicación de pruebas",
                                "Orientación de resultados",
                                "Informe psicológico"
                        )
                )
        );
    }

    @GetMapping("/services")
    public List<Map<String, Object>> getPublicServices() {
        return serviceRepository.findAll()
                .stream()
                .filter(service -> Boolean.TRUE.equals(service.getActive()))
                .map(service -> Map.<String, Object>of(
                        "id", service.getId(),
                        "name", service.getName(),
                        "description", service.getDescription(),
                        "price", service.getPrice(),
                        "durationMinutes", service.getDurationMinutes()
                ))
                .toList();
    }

    private void validateLead(LeadRequestDto request) {
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Debe ingresar su nombre completo");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Debe ingresar un correo electrónico");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new RuntimeException("Debe ingresar un teléfono o WhatsApp");
        }
        if (request.getServiceInterest() == null || request.getServiceInterest().trim().isEmpty()) {
            throw new RuntimeException("Debe seleccionar el tipo de atención");
        }
        if (request.getModality() == null || request.getModality().trim().isEmpty()) {
            throw new RuntimeException("Debe seleccionar una modalidad");
        }
        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            throw new RuntimeException("Debe seleccionar el método de pago del adelanto");
        }
        if (request.getOperationCode() == null || request.getOperationCode().trim().isEmpty()) {
            throw new RuntimeException("Debe ingresar el código de operación del adelanto");
        }
        if (request.getAdvanceAmount() == null || request.getAdvanceAmount() <= 0) {
            throw new RuntimeException("No se pudo calcular el monto del adelanto");
        }
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private String formatAmount(Double amount) {
        if (amount == null) return "0.00";
        return String.format("%.2f", amount);
    }
}