package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.entity.Lead;
import com.centropsicologico.sistema.entity.User;
import com.centropsicologico.sistema.repository.UserRepository;
import com.centropsicologico.sistema.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            UserRepository userRepository
    ) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    @Override
    public void notifyAdminsNewAppointment(Appointment appointment) {
        if (appointment == null) {
            return;
        }

        String subject = "Nueva cita registrada - CentroPsico";

        String body = buildAppointmentEmailBody(
                appointment,
                "Se ha registrado una nueva cita desde el panel administrativo.",
                null
        );

        sendToAdmins(subject, body);
    }

    @Override
    public void notifyAdminsNewAppointmentFromLead(
            Appointment appointment,
            Lead lead
    ) {
        if (appointment == null) {
            return;
        }

        String subject = "Nueva cita desde pre-reserva - CentroPsico";

        String extra = "";

        if (lead != null) {
            extra =
                    "\nDATOS DE LA PRE-RESERVA\n"
                            + "Interesado: " + safe(lead.getFullName()) + "\n"
                            + "Correo: " + safe(lead.getEmail()) + "\n"
                            + "Teléfono: " + safe(lead.getPhone()) + "\n"
                            + "Modalidad: " + safe(lead.getModality()) + "\n"
                            + "Mensaje: " + safe(lead.getMessage()) + "\n";
        }

        String body = buildAppointmentEmailBody(
                appointment,
                "Una pre-reserva del portal público fue convertida en cita.",
                extra
        );

        sendToAdmins(subject, body);
    }

    private void sendToAdmins(
            String subject,
            String body
    ) {
        List<User> admins =
                userRepository.findByRoleAndActiveTrue("ADMIN");

        if (admins == null || admins.isEmpty()) {
            System.err.println("No se encontraron administradores activos para enviar correo.");
            return;
        }

        for (User admin : admins) {
            if (admin.getEmail() == null || admin.getEmail().isBlank()) {
                continue;
            }

            sendEmail(
                    admin.getEmail(),
                    subject,
                    body
            );
        }
    }

    private void sendEmail(
            String to,
            String subject,
            String body
    ) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            if (mailFrom != null && !mailFrom.isBlank()) {
                message.setFrom(mailFrom);
            }

            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

        } catch (MailException exception) {
            System.err.println(
                    "No se pudo enviar correo a "
                            + to
                            + ": "
                            + exception.getMessage()
            );
        }
    }

    private String buildAppointmentEmailBody(
            Appointment appointment,
            String intro,
            String extra
    ) {
        String patientName = appointment.getPatient() != null
                ? safe(appointment.getPatient().getFirstName())
                + " "
                + safe(appointment.getPatient().getLastName())
                : "Paciente no definido";

        String psychologistName = appointment.getPsychologist() != null
                ? safe(appointment.getPsychologist().getFirstName())
                + " "
                + safe(appointment.getPsychologist().getLastName())
                : "Psicólogo no definido";

        String serviceName = appointment.getService() != null
                ? safe(appointment.getService().getName())
                : "Servicio no definido";

        String date = appointment.getDate() != null
                ? appointment.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "-";

        String startTime = appointment.getStartTime() != null
                ? appointment.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                : "-";

        String endTime = appointment.getEndTime() != null
                ? appointment.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                : "-";

        BigDecimal total = appointment.getTotalAmount() != null
                ? appointment.getTotalAmount()
                : BigDecimal.ZERO;

        BigDecimal paid = appointment.getPaidAmount() != null
                ? appointment.getPaidAmount()
                : BigDecimal.ZERO;

        BigDecimal pending = appointment.getPendingAmount() != null
                ? appointment.getPendingAmount()
                : BigDecimal.ZERO;

        return ""
                + "CentroPsico - Notificación de cita\n"
                + "=================================\n\n"
                + intro
                + "\n\n"
                + "DATOS DE LA CITA\n"
                + "ID de cita: " + appointment.getId() + "\n"
                + "Paciente: " + patientName.trim() + "\n"
                + "Psicólogo: " + psychologistName.trim() + "\n"
                + "Servicio: " + serviceName + "\n"
                + "Fecha: " + date + "\n"
                + "Hora: " + startTime + " - " + endTime + "\n"
                + "Estado: " + appointment.getStatus() + "\n"
                + "Motivo: " + safe(appointment.getReason()) + "\n"
                + "Observación: " + safe(appointment.getObservation()) + "\n\n"
                + "DATOS DE PAGO\n"
                + "Total: S/ " + total + "\n"
                + "Pagado: S/ " + paid + "\n"
                + "Saldo pendiente: S/ " + pending + "\n"
                + "Estado de pago: " + safe(appointment.getPaymentStatus()) + "\n"
                + "Método de pago: " + safe(appointment.getPaymentMethod()) + "\n"
                + "Código de operación: " + safe(appointment.getOperationCode()) + "\n"
                + (extra != null ? extra : "")
                + "\nEste correo fue generado automáticamente por el sistema CentroPsico.";
    }

    private String safe(String value) {
        return value == null || value.isBlank()
                ? "-"
                : value.trim();
    }
}