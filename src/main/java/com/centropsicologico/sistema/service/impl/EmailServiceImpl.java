package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.entity.Lead;
import com.centropsicologico.sistema.entity.User;
import com.centropsicologico.sistema.repository.UserRepository;
import com.centropsicologico.sistema.service.EmailService;

import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;

    @Value("${app.mail.from:}")
    private String mailFrom;

    public EmailServiceImpl(
            UserRepository userRepository,
            JavaMailSender javaMailSender
    ) {
        this.userRepository = userRepository;
        this.javaMailSender = javaMailSender;
    }

    /*
     * =========================================================
     * NOTIFICAR NUEVA CITA DESDE EL PANEL
     * =========================================================
     */

    @Override
    public void notifyAdminsNewAppointment(Appointment appointment) {

        if (appointment == null) {
            System.err.println(
                    "EMAIL SMTP ERROR: La cita recibida es null."
            );
            return;
        }

        System.out.println(
                "EMAIL SMTP: Preparando correo de la cita #"
                        + appointment.getId()
        );

        String subject =
                "Nueva cita registrada - CentroPsico";

        String body = buildAppointmentEmailBody(
                appointment,
                "Se ha registrado una nueva cita desde el panel administrativo.",
                null
        );

        sendToAdmins(subject, body);
    }

    /*
     * =========================================================
     * NOTIFICAR CITA DESDE PRE-RESERVA
     * =========================================================
     */

    @Override
    public void notifyAdminsNewAppointmentFromLead(
            Appointment appointment,
            Lead lead
    ) {

        if (appointment == null) {
            System.err.println(
                    "EMAIL SMTP ERROR: La cita de la pre-reserva es null."
            );
            return;
        }

        System.out.println(
                "EMAIL SMTP: Preparando correo de pre-reserva. Cita #"
                        + appointment.getId()
        );

        String subject =
                "Nueva cita desde pre-reserva - CentroPsico";

        String extra = "";

        if (lead != null) {
            extra =
                    "\nDATOS DE LA PRE-RESERVA\n"
                            + "Interesado: "
                            + safe(lead.getFullName())
                            + "\n"

                            + "Correo: "
                            + safe(lead.getEmail())
                            + "\n"

                            + "Teléfono: "
                            + safe(lead.getPhone())
                            + "\n"

                            + "Modalidad: "
                            + safe(lead.getModality())
                            + "\n"

                            + "Mensaje: "
                            + safe(lead.getMessage())
                            + "\n";
        }

        String body = buildAppointmentEmailBody(
                appointment,
                "Una pre-reserva del portal público fue convertida en cita.",
                extra
        );

        sendToAdmins(subject, body);
    }

    /*
     * =========================================================
     * BUSCAR ADMINISTRADORES ACTIVOS
     * =========================================================
     */

    private void sendToAdmins(
            String subject,
            String body
    ) {

        System.out.println("====================================");
        System.out.println(
                "EMAIL SMTP: INICIANDO ENVÍO CON GMAIL"
        );

        if (mailFrom == null || mailFrom.isBlank()) {
            System.err.println(
                    "EMAIL SMTP ERROR: MAIL_FROM está vacío."
            );
            System.out.println("====================================");
            return;
        }

        List<User> admins =
                userRepository.findByRoleAndActiveTrue("ADMIN");

        System.out.println(
                "EMAIL SMTP: Administradores encontrados: "
                        + (admins != null ? admins.size() : 0)
        );

        if (admins == null || admins.isEmpty()) {
            System.err.println(
                    "EMAIL SMTP ERROR: No existen administradores activos."
            );
            System.out.println("====================================");
            return;
        }

        for (User admin : admins) {

            if (admin == null
                    || admin.getEmail() == null
                    || admin.getEmail().isBlank()) {

                System.err.println(
                        "EMAIL SMTP WARNING: Administrador sin correo."
                );
                continue;
            }

            sendEmailByGmail(
                    admin.getEmail().trim(),
                    subject,
                    body
            );
        }

        System.out.println(
                "EMAIL SMTP: FINALIZÓ EL PROCESO DE ENVÍO"
        );
        System.out.println("====================================");
    }

    /*
     * =========================================================
     * ENVÍO MEDIANTE GMAIL SMTP
     * =========================================================
     */

    private void sendEmailByGmail(
            String to,
            String subject,
            String body
    ) {

        try {

            if (to == null || to.isBlank()) {
                System.err.println(
                        "EMAIL SMTP ERROR: Destinatario vacío."
                );
                return;
            }

            System.out.println(
                    "EMAIL SMTP: Enviando correo a: " + to
            );

            MimeMessage message =
                    javaMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            false,
                            StandardCharsets.UTF_8.name()
                    );

            helper.setFrom(
                    mailFrom.trim(),
                    "CentroPsico"
            );

            helper.setTo(to.trim());
            helper.setSubject(subject);

            helper.setText(
                    buildHtmlBody(body),
                    true
            );

            javaMailSender.send(message);

            System.out.println(
                    "EMAIL SMTP OK: Correo enviado correctamente a: "
                            + to
            );

        } catch (Exception exception) {

            System.err.println(
                    "EMAIL SMTP ERROR: No se pudo enviar el correo a: "
                            + to
            );

            System.err.println(
                    "EMAIL SMTP ERROR MESSAGE: "
                            + exception.getMessage()
            );

            exception.printStackTrace();
        }
    }

    /*
     * =========================================================
     * CONVERTIR CONTENIDO A HTML
     * =========================================================
     */

    private String buildHtmlBody(String body) {

        String escaped = body == null
                ? ""
                : body
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("\n", "<br>");

        return """
                <html>
                    <body style="
                        font-family: Arial, sans-serif;
                        background-color: #f4f7fb;
                        padding: 24px;
                        color: #1f2937;
                    ">
                        <div style="
                            max-width: 650px;
                            margin: auto;
                            background-color: #ffffff;
                            padding: 28px;
                            border-radius: 12px;
                            border: 1px solid #e5e7eb;
                        ">
                            <h2 style="
                                color: #17466f;
                                margin-top: 0;
                            ">
                                CentroPsico
                            </h2>

                            <div style="
                                font-size: 14px;
                                line-height: 1.7;
                            ">
                """
                + escaped
                + """
                            </div>

                            <p style="
                                margin-top: 24px;
                                color: #6b7280;
                                font-size: 12px;
                            ">
                                Mensaje generado automáticamente
                                por el sistema CentroPsico.
                            </p>
                        </div>
                    </body>
                </html>
                """;
    }

    /*
     * =========================================================
     * CONSTRUIR INFORMACIÓN DE LA CITA
     * =========================================================
     */

    private String buildAppointmentEmailBody(
            Appointment appointment,
            String intro,
            String extra
    ) {

        String patientName =
                appointment.getPatient() != null
                        ? safe(
                                appointment
                                        .getPatient()
                                        .getFirstName()
                        )
                                + " "
                                + safe(
                                        appointment
                                                .getPatient()
                                                .getLastName()
                                )
                        : "Paciente no definido";

        String psychologistName =
                appointment.getPsychologist() != null
                        ? safe(
                                appointment
                                        .getPsychologist()
                                        .getFirstName()
                        )
                                + " "
                                + safe(
                                        appointment
                                                .getPsychologist()
                                                .getLastName()
                                )
                        : "Psicólogo no definido";

        String serviceName =
                appointment.getService() != null
                        ? safe(
                                appointment
                                        .getService()
                                        .getName()
                        )
                        : "Servicio no definido";

        String date =
                appointment.getDate() != null
                        ? appointment
                                .getDate()
                                .format(
                                        DateTimeFormatter.ofPattern(
                                                "dd/MM/yyyy"
                                        )
                                )
                        : "-";

        String startTime =
                appointment.getStartTime() != null
                        ? appointment
                                .getStartTime()
                                .format(
                                        DateTimeFormatter.ofPattern(
                                                "HH:mm"
                                        )
                                )
                        : "-";

        String endTime =
                appointment.getEndTime() != null
                        ? appointment
                                .getEndTime()
                                .format(
                                        DateTimeFormatter.ofPattern(
                                                "HH:mm"
                                        )
                                )
                        : "-";

        BigDecimal total =
                appointment.getTotalAmount() != null
                        ? appointment.getTotalAmount()
                        : BigDecimal.ZERO;

        BigDecimal paid =
                appointment.getPaidAmount() != null
                        ? appointment.getPaidAmount()
                        : BigDecimal.ZERO;

        BigDecimal pending =
                appointment.getPendingAmount() != null
                        ? appointment.getPendingAmount()
                        : BigDecimal.ZERO;

        return intro
                + "\n\n"

                + "DATOS DE LA CITA\n"
                + "ID de cita: "
                + appointment.getId()
                + "\n"

                + "Paciente: "
                + patientName.trim()
                + "\n"

                + "Psicólogo: "
                + psychologistName.trim()
                + "\n"

                + "Servicio: "
                + serviceName
                + "\n"

                + "Fecha: "
                + date
                + "\n"

                + "Hora: "
                + startTime
                + " - "
                + endTime
                + "\n"

                + "Estado: "
                + appointment.getStatus()
                + "\n"

                + "Motivo: "
                + safe(appointment.getReason())
                + "\n"

                + "Observación: "
                + safe(appointment.getObservation())
                + "\n\n"

                + "DATOS DE PAGO\n"
                + "Total: S/ "
                + total
                + "\n"

                + "Pagado: S/ "
                + paid
                + "\n"

                + "Saldo pendiente: S/ "
                + pending
                + "\n"

                + "Estado de pago: "
                + safe(appointment.getPaymentStatus())
                + "\n"

                + "Método de pago: "
                + safe(appointment.getPaymentMethod())
                + "\n"

                + "Código de operación: "
                + safe(appointment.getOperationCode())
                + "\n"

                + (extra != null ? extra : "");
    }

    private String safe(String value) {

        return value == null || value.isBlank()
                ? "-"
                : value.trim();
    }
}