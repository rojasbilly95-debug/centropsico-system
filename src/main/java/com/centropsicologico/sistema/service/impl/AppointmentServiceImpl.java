package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.dto.PaymentRequestDto;
import com.centropsicologico.sistema.entity.*;
import com.centropsicologico.sistema.enums.AppointmentStatus;
import com.centropsicologico.sistema.exception.BusinessRuleException;
import com.centropsicologico.sistema.exception.ResourceNotFoundException;
import com.centropsicologico.sistema.repository.*;
import com.centropsicologico.sistema.service.AppointmentService;
import com.centropsicologico.sistema.service.NotificationService;
import org.springframework.stereotype.Service;
import com.centropsicologico.sistema.service.AuditLogService;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final PsychologistRepository psychologistRepository;
    private final ServiceRepository serviceRepository;
    private final IncomeRepository incomeRepository;
    private final PsychologistAvailabilityRepository availabilityRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

public AppointmentServiceImpl(
        AppointmentRepository appointmentRepository,
        PatientRepository patientRepository,
        PsychologistRepository psychologistRepository,
        ServiceRepository serviceRepository,
        IncomeRepository incomeRepository,
        PsychologistAvailabilityRepository availabilityRepository,
        NotificationService notificationService,
        AuditLogService auditLogService) {
    this.appointmentRepository = appointmentRepository;
    this.patientRepository = patientRepository;
    this.psychologistRepository = psychologistRepository;
    this.serviceRepository = serviceRepository;
    this.incomeRepository = incomeRepository;
    this.availabilityRepository = availabilityRepository;
    this.notificationService = notificationService;
    this.auditLogService = auditLogService;
}

    @Override
    public Appointment save(Appointment appointment) {
        validateAppointmentData(appointment);

        Patient patient = patientRepository.findById(appointment.getPatient().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        Psychologist psychologist = psychologistRepository.findById(appointment.getPsychologist().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Psicólogo no encontrado"));

        ServiceEntity serviceEntity = serviceRepository.findById(appointment.getService().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        validateActiveEntities(patient, psychologist, serviceEntity);
        validatePsychologistAvailability(psychologist, appointment);

        boolean existsOverlap = appointmentRepository
                .existsByPsychologistAndDateAndStartTimeLessThanAndEndTimeGreaterThan(
                        psychologist,
                        appointment.getDate(),
                        appointment.getEndTime(),
                        appointment.getStartTime());

        if (existsOverlap) {
            throw new BusinessRuleException("El psicólogo ya tiene una cita en ese rango horario");
        }

        appointment.setPatient(patient);
        appointment.setPsychologist(psychologist);
        appointment.setService(serviceEntity);

        if (appointment.getStatus() == null) {
            appointment.setStatus(AppointmentStatus.PROGRAMADA);
        }

        BigDecimal servicePrice = serviceEntity.getPrice() != null
                ? BigDecimal.valueOf(serviceEntity.getPrice())
                : BigDecimal.ZERO;

        appointment.setTotalAmount(servicePrice);
        appointment.setPaidAmount(BigDecimal.ZERO);
        appointment.setPendingAmount(servicePrice);
        appointment.setPaid(false);
        appointment.setPaymentStatus("PENDIENTE");

        Appointment savedAppointment = appointmentRepository.save(appointment);

        createAppointmentCreatedNotifications(savedAppointment);

        auditLogService.record(
        "CITAS",
        "REGISTRO DE CITA",
        "Appointment",
        savedAppointment.getId(),
        "Se registró una cita para "
                + getPatientFullName(savedAppointment)
                + " con "
                + getPsychologistFullName(savedAppointment)
                + " el "
                + savedAppointment.getDate()
                + " de "
                + savedAppointment.getStartTime()
                + " a "
                + savedAppointment.getEndTime()
);

        return savedAppointment;
    }

    @Override
    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    @Override
    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con id: " + id));
    }

    @Override
    public Appointment update(Long id, Appointment appointment) {
        validateAppointmentData(appointment);

        Appointment currentAppointment = findById(id);

        Patient patient = patientRepository.findById(appointment.getPatient().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        Psychologist psychologist = psychologistRepository.findById(appointment.getPsychologist().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Psicólogo no encontrado"));

        ServiceEntity serviceEntity = serviceRepository.findById(appointment.getService().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        validateActiveEntities(patient, psychologist, serviceEntity);
        validatePsychologistAvailability(psychologist, appointment);

        boolean existsOverlap = appointmentRepository
                .existsByPsychologistAndDateAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
                        psychologist,
                        appointment.getDate(),
                        appointment.getEndTime(),
                        appointment.getStartTime(),
                        id);

        if (existsOverlap) {
            throw new BusinessRuleException("El psicólogo ya tiene otra cita en ese rango horario");
        }

        currentAppointment.setPatient(patient);
        currentAppointment.setPsychologist(psychologist);
        currentAppointment.setService(serviceEntity);
        currentAppointment.setDate(appointment.getDate());
        currentAppointment.setStartTime(appointment.getStartTime());
        currentAppointment.setEndTime(appointment.getEndTime());
        currentAppointment.setStatus(appointment.getStatus());
        currentAppointment.setReason(appointment.getReason());
        currentAppointment.setObservation(appointment.getObservation());

        BigDecimal servicePrice = serviceEntity.getPrice() != null
                ? BigDecimal.valueOf(serviceEntity.getPrice())
                : BigDecimal.ZERO;

        BigDecimal paidAmount = currentAppointment.getPaidAmount() != null
                ? currentAppointment.getPaidAmount()
                : BigDecimal.ZERO;

        BigDecimal pendingAmount = servicePrice.subtract(paidAmount);

        if (pendingAmount.compareTo(BigDecimal.ZERO) < 0) {
            pendingAmount = BigDecimal.ZERO;
        }

        currentAppointment.setTotalAmount(servicePrice);
        currentAppointment.setPendingAmount(pendingAmount);

        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            currentAppointment.setPaid(false);
            currentAppointment.setPaymentStatus("PENDIENTE");
        } else if (pendingAmount.compareTo(BigDecimal.ZERO) == 0) {
            currentAppointment.setPaid(true);
            currentAppointment.setPaymentStatus("PAGADO");
        } else {
            currentAppointment.setPaid(false);
            currentAppointment.setPaymentStatus("PARCIAL");
        }

        Appointment updatedAppointment = appointmentRepository.save(currentAppointment);

        createAppointmentUpdatedNotifications(updatedAppointment);

        auditLogService.record(
        "CITAS",
        "ACTUALIZACIÓN DE CITA",
        "Appointment",
        updatedAppointment.getId(),
        "Se actualizó la cita #"
                + updatedAppointment.getId()
                + " de "
                + getPatientFullName(updatedAppointment)
                + " con "
                + getPsychologistFullName(updatedAppointment)
);

        return updatedAppointment;
    }

    @Override
    public void delete(Long id) {
        Appointment appointment = findById(id);
        appointment.setStatus(AppointmentStatus.CANCELADA);

        Appointment cancelledAppointment = appointmentRepository.save(appointment);

        createAppointmentStatusNotification(cancelledAppointment, AppointmentStatus.CANCELADA);

        auditLogService.record(
        "CITAS",
        "CANCELACIÓN DE CITA",
        "Appointment",
        cancelledAppointment.getId(),
        "Se canceló la cita #"
                + cancelledAppointment.getId()
                + " de "
                + getPatientFullName(cancelledAppointment)
);
    }

    @Override
    public List<Appointment> findByDate(LocalDate date) {
        return appointmentRepository.findByDate(date);
    }

    @Override
    public Appointment payAppointment(Long id, PaymentRequestDto paymentRequest) {
        Appointment appointment = findById(id);

        BigDecimal amount = paymentRequest.getAmount();
        String method = paymentRequest.getMethod();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("El monto pagado debe ser mayor a cero");
        }

        if (method == null || method.trim().isEmpty()) {
            throw new BusinessRuleException("Debe ingresar el método de pago");
        }

        BigDecimal totalAmount = appointment.getTotalAmount();

        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            totalAmount = appointment.getService() != null && appointment.getService().getPrice() != null
                    ? BigDecimal.valueOf(appointment.getService().getPrice())
                    : BigDecimal.ZERO;

            appointment.setTotalAmount(totalAmount);
        }

        BigDecimal previousPaid = appointment.getPaidAmount() != null
                ? appointment.getPaidAmount()
                : BigDecimal.ZERO;

        BigDecimal newPaidAmount = previousPaid.add(amount);

        if (newPaidAmount.compareTo(totalAmount) > 0) {
            throw new BusinessRuleException("El monto ingresado supera el saldo pendiente de la cita");
        }

        BigDecimal pendingAmount = totalAmount.subtract(newPaidAmount);

        appointment.setPaidAmount(newPaidAmount);
        appointment.setPendingAmount(pendingAmount);
        appointment.setPaymentMethod(method);
        appointment.setPaymentDate(LocalDate.now());
        appointment.setPaymentDateTime(LocalDateTime.now());
        appointment.setOperationCode(paymentRequest.getOperationCode());
        appointment.setPaymentObservation(paymentRequest.getObservation());
        appointment.setPaymentRegisteredBy(paymentRequest.getRegisteredBy());

        if (pendingAmount.compareTo(BigDecimal.ZERO) == 0) {
            appointment.setPaid(true);
            appointment.setPaymentStatus("PAGADO");
        } else {
            appointment.setPaid(false);
            appointment.setPaymentStatus("PARCIAL");
        }

        Appointment paidAppointment = appointmentRepository.save(appointment);

        Income income = new Income();
        income.setDescription(
                "Pago de cita #" + appointment.getId() +
                        " - " + appointment.getPatient().getFirstName() + " " +
                        appointment.getPatient().getLastName() +
                        " | Método: " + method +
                        " | Estado pago: " + appointment.getPaymentStatus() +
                        (paymentRequest.getOperationCode() != null && !paymentRequest.getOperationCode().isBlank()
                                ? " | Op: " + paymentRequest.getOperationCode()
                                : "")
        );
        income.setAmount(amount);
        income.setDate(LocalDate.now());
        income.setPaymentMethod(method);
        income.setActive(true);

        incomeRepository.save(income);

        createPaymentNotification(paidAppointment, amount);
        auditLogService.record(
        "PAGOS",
        "REGISTRO DE PAGO",
        "Appointment",
        paidAppointment.getId(),
        "Se registró un pago de S/ "
                + amount
                + " para la cita #"
                + paidAppointment.getId()
                + " del paciente "
                + getPatientFullName(paidAppointment)
                + ". Estado: "
                + paidAppointment.getPaymentStatus()
                + ". Saldo pendiente: S/ "
                + paidAppointment.getPendingAmount()
);

        return paidAppointment;
    }

    @Override
    public Appointment updateStatus(Long id, String status) {
        Appointment appointment = findById(id);

        AppointmentStatus newStatus;

        try {
            newStatus = AppointmentStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Estado de cita no válido");
        }

        appointment.setStatus(newStatus);

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        createAppointmentStatusNotification(updatedAppointment, newStatus);

        auditLogService.record(
        "CITAS",
        "CAMBIO DE ESTADO DE CITA",
        "Appointment",
        updatedAppointment.getId(),
        "La cita #"
                + updatedAppointment.getId()
                + " fue marcada como "
                + formatStatus(newStatus)
);

        return updatedAppointment;
    }

    @Override
    public List<Appointment> findByPsychologistEmail(String email) {
        return appointmentRepository.findByPsychologistEmail(email);
    }

    @Override
    public List<Appointment> findByPsychologistEmailAndDate(String email, LocalDate date) {
        return appointmentRepository.findByPsychologistEmailAndDate(email, date);
    }

    private void validateAppointmentData(Appointment appointment) {
        if (appointment.getPatient() == null || appointment.getPatient().getId() == null) {
            throw new BusinessRuleException("Debe seleccionar un paciente");
        }

        if (appointment.getPsychologist() == null || appointment.getPsychologist().getId() == null) {
            throw new BusinessRuleException("Debe seleccionar un psicólogo");
        }

        if (appointment.getService() == null || appointment.getService().getId() == null) {
            throw new BusinessRuleException("Debe seleccionar un servicio");
        }

        if (appointment.getDate() == null) {
            throw new BusinessRuleException("La fecha de la cita es obligatoria");
        }

        if (appointment.getStartTime() == null) {
            throw new BusinessRuleException("La hora de inicio es obligatoria");
        }

        if (appointment.getEndTime() == null) {
            throw new BusinessRuleException("La hora de fin es obligatoria");
        }

        if (!appointment.getStartTime().isBefore(appointment.getEndTime())) {
            throw new BusinessRuleException("La hora de inicio debe ser menor que la hora de fin");
        }
    }

    private void validateActiveEntities(Patient patient, Psychologist psychologist, ServiceEntity serviceEntity) {
        if (Boolean.FALSE.equals(patient.getActive())) {
            throw new BusinessRuleException("No se puede registrar una cita con un paciente inactivo");
        }

        if (Boolean.FALSE.equals(psychologist.getActive())) {
            throw new BusinessRuleException("No se puede registrar una cita con un psicólogo inactivo");
        }

        if (Boolean.FALSE.equals(serviceEntity.getActive())) {
            throw new BusinessRuleException("No se puede registrar una cita con un servicio inactivo");
        }
    }

    private void validatePsychologistAvailability(Psychologist psychologist, Appointment appointment) {
        DayOfWeek dayOfWeek = appointment.getDate().getDayOfWeek();

        boolean hasAvailability = availabilityRepository
                .existsByPsychologistAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThanAndActiveTrue(
                        psychologist,
                        dayOfWeek,
                        appointment.getEndTime(),
                        appointment.getStartTime()
                );

        if (!hasAvailability) {
            throw new BusinessRuleException(
                    "El psicólogo no tiene disponibilidad registrada para ese día y horario"
            );
        }
    }

    private void createAppointmentCreatedNotifications(Appointment appointment) {
        String patientName = getPatientFullName(appointment);
        String psychologistName = getPsychologistFullName(appointment);

        String message = "Nueva cita programada para " + patientName +
                " con " + psychologistName +
                " el " + appointment.getDate() +
                " de " + appointment.getStartTime() +
                " a " + appointment.getEndTime() + ".";

        notificationService.createForRole(
                "Nueva cita registrada",
                message,
                "CITA_CREADA",
                "ADMIN"
        );

        notificationService.createForRole(
                "Nueva cita registrada",
                message,
                "CITA_CREADA",
                "RECEPCIONISTA"
        );

        if (appointment.getPsychologist() != null && appointment.getPsychologist().getEmail() != null) {
            notificationService.createForUser(
                    "Nueva cita asignada",
                    message,
                    "CITA_ASIGNADA",
                    appointment.getPsychologist().getEmail()
            );
        }
    }

    private void createAppointmentUpdatedNotifications(Appointment appointment) {
        String patientName = getPatientFullName(appointment);
        String psychologistName = getPsychologistFullName(appointment);

        String message = "La cita #" + appointment.getId() +
                " de " + patientName +
                " con " + psychologistName +
                " fue actualizada para el " + appointment.getDate() +
                " de " + appointment.getStartTime() +
                " a " + appointment.getEndTime() + ".";

        notificationService.createForRole(
                "Cita actualizada",
                message,
                "CITA_ACTUALIZADA",
                "ADMIN"
        );

        notificationService.createForRole(
                "Cita actualizada",
                message,
                "CITA_ACTUALIZADA",
                "RECEPCIONISTA"
        );

        if (appointment.getPsychologist() != null && appointment.getPsychologist().getEmail() != null) {
            notificationService.createForUser(
                    "Cita actualizada",
                    message,
                    "CITA_ACTUALIZADA",
                    appointment.getPsychologist().getEmail()
            );
        }
    }

    private void createAppointmentStatusNotification(Appointment appointment, AppointmentStatus status) {
        String patientName = getPatientFullName(appointment);
        String psychologistName = getPsychologistFullName(appointment);

        String statusLabel = formatStatus(status);

        String message = "La cita #" + appointment.getId() +
                " de " + patientName +
                " con " + psychologistName +
                " fue marcada como " + statusLabel + ".";

        notificationService.createForRole(
                "Estado de cita actualizado",
                message,
                "CITA_ESTADO",
                "ADMIN"
        );

        notificationService.createForRole(
                "Estado de cita actualizado",
                message,
                "CITA_ESTADO",
                "RECEPCIONISTA"
        );

        if (appointment.getPsychologist() != null && appointment.getPsychologist().getEmail() != null) {
            notificationService.createForUser(
                    "Estado de cita actualizado",
                    message,
                    "CITA_ESTADO",
                    appointment.getPsychologist().getEmail()
            );
        }
    }

    private void createPaymentNotification(Appointment appointment, BigDecimal currentPaymentAmount) {
        String patientName = getPatientFullName(appointment);

        String message = "Se registró un pago de S/ " + currentPaymentAmount +
                " para la cita #" + appointment.getId() +
                " del paciente " + patientName +
                " mediante " + appointment.getPaymentMethod() +
                ". Estado: " + formatPaymentStatus(appointment.getPaymentStatus()) +
                ". Saldo pendiente: S/ " + appointment.getPendingAmount() + ".";

        notificationService.createForRole(
                "Pago registrado",
                message,
                "PAGO_REGISTRADO",
                "ADMIN"
        );

        notificationService.createForRole(
                "Pago registrado",
                message,
                "PAGO_REGISTRADO",
                "RECEPCIONISTA"
        );
    }

    private String getPatientFullName(Appointment appointment) {
        if (appointment.getPatient() == null) return "Paciente";

        return (appointment.getPatient().getFirstName() + " " +
                appointment.getPatient().getLastName()).trim();
    }

    private String getPsychologistFullName(Appointment appointment) {
        if (appointment.getPsychologist() == null) return "Psicólogo";

        return (appointment.getPsychologist().getFirstName() + " " +
                appointment.getPsychologist().getLastName()).trim();
    }

    private String formatStatus(AppointmentStatus status) {
        if (status == null) return "sin estado";

        return switch (status) {
            case PROGRAMADA -> "programada";
            case ATENDIDA -> "atendida";
            case CANCELADA -> "cancelada";
            case NO_ASISTIO -> "no asistió";
            case REPROGRAMADA -> "reprogramada";
        };
    }

    private String formatPaymentStatus(String paymentStatus) {
        if (paymentStatus == null) return "pendiente";

        return switch (paymentStatus) {
            case "PAGADO" -> "pagado";
            case "PARCIAL" -> "adelanto registrado";
            case "PENDIENTE" -> "pendiente";
            default -> paymentStatus.toLowerCase();
        };
    }
}