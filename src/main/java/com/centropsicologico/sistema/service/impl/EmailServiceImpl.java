package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.entity.Lead;
import com.centropsicologico.sistema.entity.User;
import com.centropsicologico.sistema.repository.UserRepository;
import com.centropsicologico.sistema.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
            System.err.println("EMAIL: No se envió correo porque la cita es null.");
            return;
        }

        System.out.println("EMAIL: Preparando correo por nueva cita desde panel. ID cita: " + appointment.getId());

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
            System.err.println("EMAIL: No se envió correo porque la cita desde pre-reserva es null.");
            return;
        }

        System.out.println("EMAIL: Preparando correo por cita creada desde pre-reserva. ID cita: " + appointment.getId());

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
        System.out.println("====================================");
        System.out.println("INICIANDO ENVÍO DE CORREO A ADMIN");
        System.out.println("MAIL_FROM CONFIGURADO: " + mailFrom);

        if (mailFrom == null || mailFrom.isBlank()) {
            System.err.println("EMAIL ERROR: spring.mail.username está vacío. Revisa MAIL_USERNAME en Render.");
            System.out.println("====================================");
            return;
        }

        List<User> admins = userRepository.findByRoleAndActiveTrue("ADMIN");

        System.out.println("ADMINISTRADORES ENCONTRADOS: " + (admins != null ? admins.size() : 0));

        if (admins == null || admins.isEmpty()) {
            System.err.println("EMAIL ERROR: No se encontraron administradores activos para enviar correo.");
            System.out.println("====================================");
            return;
        }

        for (User admin : admins) {
            if (admin == null) {
                continue;
            }

            System.out.println("ADMIN DETECTADO: "
                    + safe(admin.getFirstName())
                    + " "
                    + safe(admin.getLastName())
                    + " | correo: "
                    + admin.getEmail()
            );

            if (admin.getEmail() == null || admin.getEmail().isBlank()) {
                System.err.println("EMAIL WARNING: Admin sin correo, se omite.");
                continue;
            }

            sendEmail(
                    admin.getEmail().trim(),
                    subject,
                    body
            );
        }

        System.out.println("FIN DEL PROCESO DE ENVÍO DE CORREO");
        System.out.println("====================================");
    }

    private void sendEmail(
            String to,
            String subject,
            String body
    ) {
        try {
            System.out.println("EMAIL: Intentando enviar correo a: " + to);

            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(mailFrom);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            System.out.println("EMAIL OK: Correo enviado correctamente a: " + to);

        } catch (MailException exception) {
            System.err.println("EMAIL ERROR: No se pudo enviar correo a " + to);
            System.err.println("EMAIL ERROR MESSAGE: " + exception.getMessage());
            exception.printStackTrace();
        } catch (Exception exception) {
            System.err.println("EMAIL ERROR GENERAL: Falló el envío de correo a " + to);
            System.err.println("EMAIL ERROR MESSAGE: " + exception.getMessage());
            exception.printStackTrace();
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