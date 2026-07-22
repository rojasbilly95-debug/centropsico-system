package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.dto.LeadConvertToAppointmentDto;
import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.entity.Income;
import com.centropsicologico.sistema.entity.Lead;
import com.centropsicologico.sistema.entity.Patient;
import com.centropsicologico.sistema.entity.Psychologist;
import com.centropsicologico.sistema.entity.ServiceEntity;
import com.centropsicologico.sistema.enums.AppointmentStatus;
import com.centropsicologico.sistema.repository.AppointmentRepository;
import com.centropsicologico.sistema.repository.IncomeRepository;
import com.centropsicologico.sistema.repository.LeadRepository;
import com.centropsicologico.sistema.repository.PatientRepository;
import com.centropsicologico.sistema.repository.PsychologistRepository;
import com.centropsicologico.sistema.repository.ServiceRepository;
import com.centropsicologico.sistema.service.NotificationService;
import com.centropsicologico.sistema.service.AuditLogService;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import com.centropsicologico.sistema.service.EmailService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

        private final LeadRepository leadRepository;
        private final PatientRepository patientRepository;
        private final PsychologistRepository psychologistRepository;
        private final ServiceRepository serviceRepository;
        private final AppointmentRepository appointmentRepository;
        private final IncomeRepository incomeRepository;
        private final NotificationService notificationService;
        private final AuditLogService auditLogService;
        private final EmailService emailService;

        public LeadController(
                        LeadRepository leadRepository,
                        PatientRepository patientRepository,
                        PsychologistRepository psychologistRepository,
                        ServiceRepository serviceRepository,
                        AppointmentRepository appointmentRepository,
                        IncomeRepository incomeRepository,
                        NotificationService notificationService,
                        AuditLogService auditLogService,
                        EmailService emailService) {

                this.leadRepository = leadRepository;
                this.patientRepository = patientRepository;
                this.psychologistRepository = psychologistRepository;
                this.serviceRepository = serviceRepository;
                this.appointmentRepository = appointmentRepository;
                this.incomeRepository = incomeRepository;
                this.notificationService = notificationService;
                this.auditLogService = auditLogService;
                this.emailService = emailService;
        }

        @GetMapping
        public List<Lead> findAll() {
                return leadRepository.findAllByOrderByCreatedAtDesc();
        }

        @GetMapping("/{id}")
        public Lead findById(@PathVariable Long id) {
                return leadRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Pre-reserva no encontrada"));
        }

        @PutMapping("/{id}/status")
        public Lead updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
                Lead lead = leadRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Pre-reserva no encontrada"));

                String status = request.get("status");

                if (status == null || status.trim().isEmpty()) {
                        throw new RuntimeException("Debe ingresar un estado");
                }

                status = status.trim().toUpperCase();

                if (!List.of(
                                "NUEVO",
                                "PAGO_EN_REVISION",
                                "CONTACTADO",
                                "PRE_RESERVADO",
                                "AGENDADO",
                                "DESCARTADO").contains(status)) {
                        throw new RuntimeException("Estado de pre-reserva no válido");
                }

                lead.setStatus(status);

                Lead updatedLead = leadRepository.save(lead);

                notifyRoles(
                                "Estado de pre-reserva actualizado",
                                "La pre-reserva de " + updatedLead.getFullName()
                                                + " fue actualizada al estado " + updatedLead.getStatus() + ".",
                                "PRE_RESERVA_ESTADO");

                auditLogService.record(
                                "PRE-RESERVAS",
                                "CAMBIO DE ESTADO DE PRE-RESERVA",
                                "Lead",
                                updatedLead.getId(),
                                "La pre-reserva de "
                                                + updatedLead.getFullName()
                                                + " fue actualizada al estado "
                                                + updatedLead.getStatus());

                return updatedLead;
        }

        @PutMapping("/{id}/payment/validate")
        public Lead validatePayment(@PathVariable Long id) {
                Lead lead = leadRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Pre-reserva no encontrada"));

                if ("AGENDADO".equals(lead.getStatus())) {
                        throw new RuntimeException(
                                        "No se puede validar el pago de una pre-reserva que ya fue convertida en cita");
                }

                if ("PRE_RESERVADO".equals(lead.getStatus()) || "PAGO_VALIDADO".equals(lead.getPaymentStatus())) {
                        throw new RuntimeException("El adelanto de esta pre-reserva ya fue validado");
                }

                if (lead.getAdvanceAmount() == null || lead.getAdvanceAmount() <= 0) {
                        throw new RuntimeException("La pre-reserva no tiene adelanto registrado");
                }

                if (lead.getOperationCode() == null || lead.getOperationCode().trim().isEmpty()) {
                        throw new RuntimeException("La pre-reserva no tiene código de operación");
                }

                lead.setPaymentStatus("PAGO_VALIDADO");
                lead.setStatus("PRE_RESERVADO");

                Lead updatedLead = leadRepository.save(lead);

                notifyRoles(
                                "Adelanto validado",
                                "Se validó el adelanto de la pre-reserva de "
                                                + updatedLead.getFullName()
                                                + ". Monto: S/ "
                                                + formatAmount(updatedLead.getAdvanceAmount())
                                                + ". Código: "
                                                + updatedLead.getOperationCode()
                                                + ".",
                                "PRE_RESERVA_PAGO_VALIDADO");

                auditLogService.record(
                                "PRE-RESERVAS",
                                "VALIDACIÓN DE ADELANTO",
                                "Lead",
                                updatedLead.getId(),
                                "Se validó el adelanto de la pre-reserva de "
                                                + updatedLead.getFullName()
                                                + ". Monto: S/ "
                                                + formatAmount(updatedLead.getAdvanceAmount())
                                                + ". Código: "
                                                + updatedLead.getOperationCode());

                return updatedLead;
        }

        @PutMapping("/{id}/payment/reject")
        public Lead rejectPayment(@PathVariable Long id) {
                Lead lead = leadRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Pre-reserva no encontrada"));

                if ("AGENDADO".equals(lead.getStatus())) {
                        throw new RuntimeException(
                                        "No se puede rechazar el pago de una pre-reserva que ya fue convertida en cita");
                }

                if ("PRE_RESERVADO".equals(lead.getStatus()) || "PAGO_VALIDADO".equals(lead.getPaymentStatus())) {
                        throw new RuntimeException("No se puede rechazar un adelanto que ya fue validado");
                }

                lead.setPaymentStatus("PAGO_RECHAZADO");
                lead.setStatus("CONTACTADO");

                Lead updatedLead = leadRepository.save(lead);

                notifyRoles(
                                "Adelanto rechazado",
                                "Se rechazó el adelanto de la pre-reserva de "
                                                + updatedLead.getFullName()
                                                + ". La pre-reserva volvió al estado CONTACTADO.",
                                "PRE_RESERVA_PAGO_RECHAZADO");

                auditLogService.record(
                                "PRE-RESERVAS",
                                "RECHAZO DE ADELANTO",
                                "Lead",
                                updatedLead.getId(),
                                "Se rechazó el adelanto de la pre-reserva de "
                                                + updatedLead.getFullName()
                                                + ". La pre-reserva volvió al estado CONTACTADO");

                return updatedLead;
        }

        @Transactional
        @PostMapping("/{id}/convert-to-appointment")
        public Map<String, Object> convertToAppointment(
                        @PathVariable Long id,
                        @RequestBody LeadConvertToAppointmentDto request) {
                Lead lead = leadRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Pre-reserva no encontrada"));

                if ("AGENDADO".equals(lead.getStatus())) {
                        throw new RuntimeException("Esta pre-reserva ya fue convertida en cita");
                }

                if (!"PRE_RESERVADO".equals(lead.getStatus())) {
                        throw new RuntimeException("Solo las pre-reservas validadas pueden convertirse en cita");
                }

                if (request.getPatientId() == null) {
                        throw new RuntimeException(
                                        "Debe seleccionar un paciente para convertir la pre-reserva en cita");
                }

                if (lead.getServiceId() == null) {
                        throw new RuntimeException("La pre-reserva no tiene servicio asociado");
                }

                if (lead.getPsychologistId() == null) {
                        throw new RuntimeException("La pre-reserva no tiene psicólogo asociado");
                }

                if (!hasText(lead.getPreferredDate())) {
                        throw new RuntimeException("La pre-reserva no tiene fecha seleccionada");
                }

                if (!hasText(lead.getPreferredTime())) {
                        throw new RuntimeException("La pre-reserva no tiene hora seleccionada");
                }

                Patient patient = patientRepository.findById(request.getPatientId())
                                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

                Psychologist psychologist = psychologistRepository
                                .findById(lead.getPsychologistId())
                                .orElseThrow(() -> new RuntimeException("Psicólogo no encontrado"));

                ServiceEntity service = serviceRepository
                                .findById(lead.getServiceId())
                                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

                validateActiveEntities(patient, psychologist, service);

                LocalDate appointmentDate = parseLeadDate(lead.getPreferredDate());
                LocalTime startTime = parseLeadTime(lead.getPreferredTime());

                int duration = service.getDurationMinutes() != null
                                ? service.getDurationMinutes()
                                : 60;

                LocalTime endTime = startTime.plusMinutes(duration);

                boolean existsOverlap = appointmentRepository
                                .existsByPsychologistAndDateAndStartTimeLessThanAndEndTimeGreaterThan(
                                                psychologist,
                                                appointmentDate,
                                                endTime,
                                                startTime);

                if (existsOverlap) {
                        throw new RuntimeException("El psicólogo ya tiene una cita en ese rango horario");
                }

                Appointment appointment = new Appointment();

                appointment.setPatient(patient);
                appointment.setPsychologist(psychologist);
                appointment.setService(service);
                appointment.setDate(appointmentDate);
                appointment.setStartTime(startTime);
                appointment.setEndTime(endTime);
                appointment.setStatus(AppointmentStatus.PROGRAMADA);

                appointment.setReason(
                                hasText(request.getReason())
                                                ? request.getReason().trim()
                                                : "Pre-reserva desde portal público");

                appointment.setObservation(request.getObservation());

                BigDecimal totalAmount = BigDecimal.valueOf(
                                lead.getServicePrice() != null
                                                ? lead.getServicePrice()
                                                : 0);

                BigDecimal paidAmount = BigDecimal.valueOf(
                                lead.getAdvanceAmount() != null
                                                ? lead.getAdvanceAmount()
                                                : 0);

                if (paidAmount.compareTo(totalAmount) > 0) {
                        paidAmount = totalAmount;
                }

                BigDecimal pendingAmount = totalAmount.subtract(paidAmount);

                if (pendingAmount.compareTo(BigDecimal.ZERO) < 0) {
                        pendingAmount = BigDecimal.ZERO;
                }

                appointment.setTotalAmount(totalAmount);
                appointment.setPaidAmount(paidAmount);
                appointment.setPendingAmount(pendingAmount);

                appointment.setPaymentMethod(lead.getPaymentMethod());
                appointment.setOperationCode(lead.getOperationCode());

                if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                        appointment.setPaymentDate(LocalDate.now());
                        appointment.setPaymentDateTime(LocalDateTime.now());
                        appointment.setPaymentObservation("Adelanto validado desde pre-reserva");
                        appointment.setPaymentRegisteredBy(
                                        hasText(request.getRegisteredBy())
                                                        ? request.getRegisteredBy().trim()
                                                        : "Sistema");
                }

                if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                        if (pendingAmount.compareTo(BigDecimal.ZERO) == 0) {
                                appointment.setPaid(true);
                                appointment.setPaymentStatus("PAGADO");
                        } else {
                                appointment.setPaid(false);
                                appointment.setPaymentStatus("PARCIAL");
                        }
                } else {
                        appointment.setPaid(false);
                        appointment.setPaymentStatus("PENDIENTE");
                }

                Appointment savedAppointment = appointmentRepository.save(appointment);

                if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                        Income income = new Income();

                        income.setDescription(
                                        "Adelanto pre-reserva convertida a cita #"
                                                        + savedAppointment.getId()
                                                        + " - "
                                                        + patient.getFirstName()
                                                        + " "
                                                        + patient.getLastName());

                        income.setAmount(paidAmount);
                        income.setDate(LocalDate.now());
                        income.setPaymentMethod(lead.getPaymentMethod());
                        income.setActive(true);

                        incomeRepository.save(income);
                }

                lead.setStatus("AGENDADO");
                lead.setAppointmentId(savedAppointment.getId());

                leadRepository.save(lead);

                notifyAppointmentCreatedFromLead(lead, patient, psychologist, savedAppointment, paidAmount);

                emailService.notifyAdminsNewAppointmentFromLead(savedAppointment, lead);
                auditLogService.record(
                                "PRE-RESERVAS",
                                "CONVERSIÓN A CITA",
                                "Lead",
                                lead.getId(),
                                "La pre-reserva de "
                                                + lead.getFullName()
                                                + " fue convertida en la cita #"
                                                + savedAppointment.getId());

                return Map.of(
                                "success", true,
                                "appointmentId", savedAppointment.getId(),
                                "message", "Pre-reserva convertida en cita correctamente");
        }

        private void notifyRoles(String title, String message, String type) {
                notificationService.createForRole(
                                title,
                                message,
                                type,
                                "ADMIN");

                notificationService.createForRole(
                                title,
                                message,
                                type,
                                "RECEPCIONISTA");
        }

        private void notifyAppointmentCreatedFromLead(
                        Lead lead,
                        Patient patient,
                        Psychologist psychologist,
                        Appointment appointment,
                        BigDecimal paidAmount) {

                String patientName = patient.getFirstName() + " " + patient.getLastName();
                String psychologistName = psychologist.getFirstName() + " " + psychologist.getLastName();

                String appointmentMessage = "La pre-reserva de "
                                + lead.getFullName()
                                + " fue convertida en cita para el paciente "
                                + patientName
                                + " con "
                                + psychologistName
                                + " el "
                                + appointment.getDate()
                                + " de "
                                + appointment.getStartTime()
                                + " a "
                                + appointment.getEndTime()
                                + ".";

                notifyRoles(
                                "Pre-reserva convertida en cita",
                                appointmentMessage,
                                "PRE_RESERVA_AGENDADA");

                if (paidAmount != null && paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                        notifyRoles(
                                        "Adelanto registrado como ingreso",
                                        "El adelanto de S/ "
                                                        + paidAmount
                                                        + " de la pre-reserva de "
                                                        + lead.getFullName()
                                                        + " fue registrado como ingreso de la cita #"
                                                        + appointment.getId()
                                                        + ".",
                                        "ADELANTO_INGRESO");
                }

                if (psychologist.getEmail() != null && !psychologist.getEmail().isBlank()) {
                        notificationService.createForUser(
                                        "Nueva cita asignada desde pre-reserva",
                                        appointmentMessage,
                                        "CITA_ASIGNADA",
                                        psychologist.getEmail());
                }
        }

        private String formatAmount(Double amount) {
                if (amount == null)
                        return "0.00";
                return String.format("%.2f", amount);
        }

        private boolean hasText(String value) {
                return value != null && !value.trim().isEmpty();
        }

        private LocalDate parseLeadDate(String value) {
                try {
                        return LocalDate.parse(value);
                } catch (Exception e) {
                        throw new RuntimeException("La fecha de la pre-reserva no tiene un formato válido");
                }
        }

        private LocalTime parseLeadTime(String value) {
                try {
                        return LocalTime.parse(value);
                } catch (Exception e) {
                        throw new RuntimeException("La hora de la pre-reserva no tiene un formato válido");
                }
        }

        private void validateActiveEntities(
                        Patient patient,
                        Psychologist psychologist,
                        ServiceEntity service) {
                if (Boolean.FALSE.equals(patient.getActive())) {
                        throw new RuntimeException("No se puede convertir la pre-reserva con un paciente inactivo");
                }

                if (Boolean.FALSE.equals(psychologist.getActive())) {
                        throw new RuntimeException("No se puede convertir la pre-reserva con un psicólogo inactivo");
                }

                if (Boolean.FALSE.equals(service.getActive())) {
                        throw new RuntimeException("No se puede convertir la pre-reserva con un servicio inactivo");
                }
        }
}