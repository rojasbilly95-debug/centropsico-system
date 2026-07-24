package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.entity.Lead;
import com.centropsicologico.sistema.entity.User;
import com.centropsicologico.sistema.repository.UserRepository;
import com.centropsicologico.sistema.service.EmailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${brevo.api.base-url:https://api.brevo.com/v3}")
    private String brevoBaseUrl;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Value("${app.mail.from-name:CentroPsico}")
    private String mailFromName;

    @Value("${app.web.url:https://centropsico-system.onrender.com}")
    private String appWebUrl;

    public EmailServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
    }

    /*
     * =========================================================
     * CITA REGISTRADA DESDE EL PANEL
     * =========================================================
     */

    @Override
    public void notifyAdminsNewAppointment(Appointment appointment) {

        if (appointment == null) {
            System.err.println(
                    "EMAIL BREVO ERROR: La cita recibida es null."
            );
            return;
        }

        System.out.println(
                "EMAIL BREVO: Preparando correo de la cita #"
                        + appointment.getId()
        );

        String subject = "Nueva cita registrada - CentroPsico";

        String introduction =
                "Se ha registrado una nueva cita desde el panel administrativo.";

        String textContent = buildAppointmentText(
                appointment,
                introduction,
                null
        );

        String htmlContent = buildAppointmentHtml(
                appointment,
                introduction,
                null
        );

        sendToAdmins(
                subject,
                textContent,
                htmlContent
        );
    }

    /*
     * =========================================================
     * CITA CREADA DESDE UNA PRE-RESERVA
     * =========================================================
     */

    @Override
    public void notifyAdminsNewAppointmentFromLead(
            Appointment appointment,
            Lead lead
    ) {

        if (appointment == null) {
            System.err.println(
                    "EMAIL BREVO ERROR: La cita de la pre-reserva es null."
            );
            return;
        }

        System.out.println(
                "EMAIL BREVO: Preparando correo de pre-reserva. Cita #"
                        + appointment.getId()
        );

        String subject =
                "Nueva cita desde pre-reserva - CentroPsico";

        String introduction =
                "Una pre-reserva del portal público fue convertida en cita.";

        String textContent = buildAppointmentText(
                appointment,
                introduction,
                lead
        );

        String htmlContent = buildAppointmentHtml(
                appointment,
                introduction,
                lead
        );

        sendToAdmins(
                subject,
                textContent,
                htmlContent
        );
    }

    /*
     * =========================================================
     * BUSCAR ADMINISTRADORES ACTIVOS
     * =========================================================
     */

    private void sendToAdmins(
            String subject,
            String textContent,
            String htmlContent
    ) {

        System.out.println("====================================");
        System.out.println(
                "EMAIL BREVO: INICIANDO ENVÍO POR API HTTPS"
        );

        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            System.err.println(
                    "EMAIL BREVO ERROR: BREVO_API_KEY está vacío."
            );
            return;
        }

        if (mailFrom == null || mailFrom.isBlank()) {
            System.err.println(
                    "EMAIL BREVO ERROR: MAIL_FROM está vacío."
            );
            return;
        }

        List<User> admins =
                userRepository.findByRoleAndActiveTrue("ADMIN");

        System.out.println(
                "EMAIL BREVO: Administradores encontrados: "
                        + (admins != null ? admins.size() : 0)
        );

        if (admins == null || admins.isEmpty()) {
            System.err.println(
                    "EMAIL BREVO ERROR: No existen administradores activos."
            );
            return;
        }

        for (User admin : admins) {

            if (admin == null
                    || admin.getEmail() == null
                    || admin.getEmail().isBlank()) {

                System.err.println(
                        "EMAIL BREVO WARNING: Administrador sin correo."
                );
                continue;
            }

            String recipientName = admin.getFirstName() != null
                    ? admin.getFirstName().trim()
                    : "Administrador";

            sendEmailByBrevo(
                    admin.getEmail().trim(),
                    recipientName,
                    subject,
                    textContent,
                    htmlContent
            );
        }

        System.out.println(
                "EMAIL BREVO: FINALIZÓ EL PROCESO DE ENVÍO"
        );
        System.out.println("====================================");
    }

    /*
     * =========================================================
     * ENVÍO MEDIANTE BREVO API
     * =========================================================
     */

    private void sendEmailByBrevo(
            String to,
            String recipientName,
            String subject,
            String textContent,
            String htmlContent
    ) {

        try {

            System.out.println(
                    "EMAIL BREVO: Enviando correo a: " + to
            );

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            headers.setAccept(
                    List.of(MediaType.APPLICATION_JSON)
            );

            headers.set(
                    "api-key",
                    brevoApiKey.trim()
            );

            Map<String, Object> sender =
                    new LinkedHashMap<>();

            sender.put(
                    "name",
                    safe(mailFromName)
            );

            sender.put(
                    "email",
                    mailFrom.trim()
            );

            Map<String, Object> recipient =
                    new LinkedHashMap<>();

            recipient.put(
                    "email",
                    to.trim()
            );

            recipient.put(
                    "name",
                    safe(recipientName)
            );

            Map<String, Object> payload =
                    new LinkedHashMap<>();

            payload.put(
                    "sender",
                    sender
            );

            payload.put(
                    "to",
                    List.of(recipient)
            );

            payload.put(
                    "subject",
                    subject
            );

            payload.put(
                    "htmlContent",
                    htmlContent
            );

            payload.put(
                    "textContent",
                    textContent
            );

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(
                            payload,
                            headers
                    );

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            brevoBaseUrl + "/smtp/email",
                            request,
                            String.class
                    );

            String responseBody = response.getBody();

            System.out.println(
                    "EMAIL BREVO HTTP STATUS: "
                            + response.getStatusCode()
            );

            if (response.getStatusCode().is2xxSuccessful()
                    && responseBody != null
                    && responseBody.contains("messageId")) {

                System.out.println(
                        "EMAIL BREVO OK: Correo enviado correctamente."
                );

                System.out.println(
                        "EMAIL BREVO DESTINATARIO: " + to
                );

                System.out.println(
                        "EMAIL BREVO RESPONSE: " + responseBody
                );

            } else {

                System.err.println(
                        "EMAIL BREVO ERROR: Brevo no devolvió messageId."
                );

                System.err.println(
                        "EMAIL BREVO RESPONSE: " + responseBody
                );
            }

        } catch (HttpStatusCodeException exception) {

            System.err.println(
                    "EMAIL BREVO ERROR HTTP: "
                            + exception.getStatusCode()
            );

            System.err.println(
                    "EMAIL BREVO RESPONSE BODY: "
                            + exception.getResponseBodyAsString()
            );

        } catch (Exception exception) {

            System.err.println(
                    "EMAIL BREVO ERROR GENERAL: "
                            + exception.getMessage()
            );

            exception.printStackTrace();
        }
    }

    /*
     * =========================================================
     * DISEÑO HTML PROFESIONAL
     * =========================================================
     */

    private String buildAppointmentHtml(
            Appointment appointment,
            String introduction,
            Lead lead
    ) {

        String appointmentId =
                value(appointment.getId());

        String patientName =
                appointment.getPatient() != null
                        ? safe(appointment.getPatient().getFirstName())
                                + " "
                                + safe(appointment.getPatient().getLastName())
                        : "Paciente no definido";

        String psychologistName =
                appointment.getPsychologist() != null
                        ? safe(appointment.getPsychologist().getFirstName())
                                + " "
                                + safe(appointment.getPsychologist().getLastName())
                        : "Psicólogo no definido";

        String serviceName =
                appointment.getService() != null
                        ? safe(appointment.getService().getName())
                        : "Servicio no definido";

        String date =
                appointment.getDate() != null
                        ? appointment.getDate().format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        )
                        : "-";

        String startTime =
                appointment.getStartTime() != null
                        ? appointment.getStartTime().format(
                                DateTimeFormatter.ofPattern("HH:mm")
                        )
                        : "-";

        String endTime =
                appointment.getEndTime() != null
                        ? appointment.getEndTime().format(
                                DateTimeFormatter.ofPattern("HH:mm")
                        )
                        : "-";

        String status =
                value(appointment.getStatus());

        String paymentStatus =
                value(appointment.getPaymentStatus());

        String statusBackground =
                getStatusBackground(status);

        String statusColor =
                getStatusColor(status);

        String paymentBackground =
                getPaymentBackground(paymentStatus);

        String paymentColor =
                getPaymentColor(paymentStatus);

        StringBuilder html = new StringBuilder();

        html.append("""
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                    <title>CentroPsico</title>
                </head>

                <body style="
                    margin: 0;
                    padding: 0;
                    background-color: #eef3f8;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #1f2937;
                ">

                <table width="100%"
                       cellpadding="0"
                       cellspacing="0"
                       role="presentation"
                       style="
                           width: 100%;
                           background-color: #eef3f8;
                           padding: 32px 12px;
                       ">

                    <tr>
                        <td align="center">

                            <table width="680"
                                   cellpadding="0"
                                   cellspacing="0"
                                   role="presentation"
                                   style="
                                       width: 100%;
                                       max-width: 680px;
                                       background-color: #ffffff;
                                       border-radius: 16px;
                                       overflow: hidden;
                                       box-shadow: 0 8px 24px rgba(15, 76, 129, 0.10);
                                   ">
                """);

        /*
         * CABECERA
         */
        html.append("""
                <tr>
                    <td style="
                        background-color: #0f4c81;
                        padding: 30px 36px;
                        color: #ffffff;
                    ">

                        <div style="
                            font-size: 26px;
                            font-weight: bold;
                            letter-spacing: 0.3px;
                        ">
                            CentroPsico
                        </div>

                        <div style="
                            margin-top: 6px;
                            font-size: 14px;
                            color: #dbeafe;
                        ">
                            Sistema de gestión de citas psicológicas
                        </div>

                    </td>
                </tr>
                """);

        /*
         * CONTENIDO PRINCIPAL
         */
        html.append("""
                <tr>
                    <td style="padding: 34px 36px;">

                        <div style="
                            display: inline-block;
                            padding: 7px 13px;
                            background-color: #e8f1fa;
                            color: #0f4c81;
                            border-radius: 20px;
                            font-size: 12px;
                            font-weight: bold;
                            letter-spacing: 0.4px;
                        ">
                """);

        html.append("CITA #")
                .append(escapeHtml(appointmentId));

        html.append("""
                        </div>

                        <h1 style="
                            margin: 18px 0 10px;
                            color: #123b5d;
                            font-size: 25px;
                            line-height: 1.3;
                        ">
                            Nueva cita registrada
                        </h1>

                        <p style="
                            margin: 0 0 26px;
                            color: #526273;
                            font-size: 15px;
                            line-height: 1.7;
                        ">
                """);

        html.append(escapeHtml(introduction));

        html.append("""
                        </p>

                        <table width="100%"
                               cellpadding="0"
                               cellspacing="8"
                               role="presentation"
                               style="margin-bottom: 28px;">

                            <tr>
                """);

        html.append(summaryCard(
                "Fecha",
                date
        ));

        html.append(summaryCard(
                "Horario",
                startTime + " - " + endTime
        ));

        html.append(summaryCard(
                "Servicio",
                serviceName
        ));

        html.append("""
                            </tr>
                        </table>

                        <div style="
                            margin-bottom: 30px;
                            padding: 16px 18px;
                            background-color: #f8fafc;
                            border: 1px solid #e2e8f0;
                            border-radius: 12px;
                        ">

                            <span style="
                                font-size: 13px;
                                color: #64748b;
                                margin-right: 8px;
                            ">
                                Estado de la cita:
                            </span>
                """);

        html.append(statusBadge(
                status,
                statusBackground,
                statusColor
        ));

        html.append("""
                        </div>

                        <h2 style="
                            margin: 0 0 14px;
                            color: #123b5d;
                            font-size: 18px;
                        ">
                            Datos de la cita
                        </h2>

                        <table width="100%"
                               cellpadding="0"
                               cellspacing="0"
                               role="presentation"
                               style="
                                   border: 1px solid #e2e8f0;
                                   border-radius: 12px;
                                   overflow: hidden;
                                   margin-bottom: 30px;
                               ">
                """);

        html.append(detailRow(
                "Paciente",
                patientName
        ));

        html.append(detailRow(
                "Psicólogo",
                psychologistName
        ));

        html.append(detailRow(
                "Servicio",
                serviceName
        ));

        html.append(detailRow(
                "Fecha",
                date
        ));

        html.append(detailRow(
                "Horario",
                startTime + " - " + endTime
        ));

        html.append(detailRow(
                "Motivo",
                safe(appointment.getReason())
        ));

        html.append(detailRow(
                "Observación",
                safe(appointment.getObservation())
        ));

        html.append("""
                        </table>

                        <h2 style="
                            margin: 0 0 14px;
                            color: #123b5d;
                            font-size: 18px;
                        ">
                            Información de pago
                        </h2>

                        <table width="100%"
                               cellpadding="0"
                               cellspacing="0"
                               role="presentation"
                               style="
                                   border: 1px solid #e2e8f0;
                                   border-radius: 12px;
                                   overflow: hidden;
                                   margin-bottom: 18px;
                               ">
                """);

        html.append(detailRow(
                "Importe total",
                formatMoney(appointment.getTotalAmount())
        ));

        html.append(detailRow(
                "Importe pagado",
                formatMoney(appointment.getPaidAmount())
        ));

        html.append(detailRow(
                "Saldo pendiente",
                formatMoney(appointment.getPendingAmount())
        ));

        html.append(detailRow(
                "Método de pago",
                value(appointment.getPaymentMethod())
        ));

        html.append(detailRow(
                "Código de operación",
                value(appointment.getOperationCode())
        ));

        html.append("""
                        </table>

                        <div style="
                            margin-bottom: 30px;
                            padding: 16px 18px;
                            background-color: #f8fafc;
                            border: 1px solid #e2e8f0;
                            border-radius: 12px;
                        ">

                            <span style="
                                font-size: 13px;
                                color: #64748b;
                                margin-right: 8px;
                            ">
                                Estado del pago:
                            </span>
                """);

        html.append(statusBadge(
                paymentStatus,
                paymentBackground,
                paymentColor
        ));

        html.append("</div>");

        /*
         * INFORMACIÓN DE PRE-RESERVA
         */
        if (lead != null) {

            html.append("""
                    <h2 style="
                        margin: 0 0 14px;
                        color: #123b5d;
                        font-size: 18px;
                    ">
                        Datos de la pre-reserva
                    </h2>

                    <table width="100%"
                           cellpadding="0"
                           cellspacing="0"
                           role="presentation"
                           style="
                               border: 1px solid #e2e8f0;
                               border-radius: 12px;
                               overflow: hidden;
                               margin-bottom: 30px;
                           ">
                    """);

            html.append(detailRow(
                    "Interesado",
                    safe(lead.getFullName())
            ));

            html.append(detailRow(
                    "Correo",
                    safe(lead.getEmail())
            ));

            html.append(detailRow(
                    "Teléfono",
                    safe(lead.getPhone())
            ));

            html.append(detailRow(
                    "Modalidad",
                    safe(lead.getModality())
            ));

            html.append(detailRow(
                    "Mensaje",
                    safe(lead.getMessage())
            ));

            html.append("</table>");
        }

        /*
         * BOTÓN
         */
        html.append("""
                <table width="100%"
                       cellpadding="0"
                       cellspacing="0"
                       role="presentation">

                    <tr>
                        <td align="center"
                            style="padding-top: 8px;">

                            <a href="
                """);

        html.append(escapeHtml(appWebUrl));

        html.append("""
                            "
                               target="_blank"
                               style="
                                   display: inline-block;
                                   background-color: #0f4c81;
                                   color: #ffffff;
                                   text-decoration: none;
                                   font-weight: bold;
                                   font-size: 14px;
                                   padding: 14px 28px;
                                   border-radius: 9px;
                               ">
                                Abrir CentroPsico
                            </a>

                        </td>
                    </tr>

                </table>

                <div style="
                    margin-top: 28px;
                    padding: 14px 16px;
                    background-color: #fefce8;
                    border-left: 4px solid #eab308;
                    color: #713f12;
                    font-size: 13px;
                    line-height: 1.6;
                ">
                    Este mensaje fue generado automáticamente.
                    No compartas información clínica o credenciales
                    respondiendo a este correo.
                </div>

                    </td>
                </tr>
                """);

        /*
         * PIE
         */
        html.append("""
                <tr>
                    <td style="
                        background-color: #f8fafc;
                        padding: 22px 36px;
                        text-align: center;
                        border-top: 1px solid #e2e8f0;
                        color: #64748b;
                        font-size: 12px;
                        line-height: 1.6;
                    ">
                        CentroPsico · Gestión de atención psicológica
                        <br>
                        Notificación automática del sistema
                    </td>
                </tr>

                            </table>

                        </td>
                    </tr>

                </table>

                </body>
                </html>
                """);

        return html.toString();
    }

    /*
     * =========================================================
     * CONTENIDO ALTERNATIVO EN TEXTO
     * =========================================================
     */

    private String buildAppointmentText(
            Appointment appointment,
            String introduction,
            Lead lead
    ) {

        String patientName =
                appointment.getPatient() != null
                        ? safe(appointment.getPatient().getFirstName())
                                + " "
                                + safe(appointment.getPatient().getLastName())
                        : "Paciente no definido";

        String psychologistName =
                appointment.getPsychologist() != null
                        ? safe(appointment.getPsychologist().getFirstName())
                                + " "
                                + safe(appointment.getPsychologist().getLastName())
                        : "Psicólogo no definido";

        String serviceName =
                appointment.getService() != null
                        ? safe(appointment.getService().getName())
                        : "Servicio no definido";

        String date =
                appointment.getDate() != null
                        ? appointment.getDate().format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        )
                        : "-";

        String startTime =
                appointment.getStartTime() != null
                        ? appointment.getStartTime().format(
                                DateTimeFormatter.ofPattern("HH:mm")
                        )
                        : "-";

        String endTime =
                appointment.getEndTime() != null
                        ? appointment.getEndTime().format(
                                DateTimeFormatter.ofPattern("HH:mm")
                        )
                        : "-";

        StringBuilder text = new StringBuilder();

        text.append("CENTROPSICO\n")
                .append("Nueva cita registrada\n\n")
                .append(introduction)
                .append("\n\n")

                .append("DATOS DE LA CITA\n")
                .append("ID: ")
                .append(value(appointment.getId()))
                .append("\n")

                .append("Paciente: ")
                .append(patientName.trim())
                .append("\n")

                .append("Psicólogo: ")
                .append(psychologistName.trim())
                .append("\n")

                .append("Servicio: ")
                .append(serviceName)
                .append("\n")

                .append("Fecha: ")
                .append(date)
                .append("\n")

                .append("Horario: ")
                .append(startTime)
                .append(" - ")
                .append(endTime)
                .append("\n")

                .append("Estado: ")
                .append(value(appointment.getStatus()))
                .append("\n")

                .append("Motivo: ")
                .append(safe(appointment.getReason()))
                .append("\n")

                .append("Observación: ")
                .append(safe(appointment.getObservation()))
                .append("\n\n")

                .append("DATOS DE PAGO\n")
                .append("Total: ")
                .append(formatMoney(appointment.getTotalAmount()))
                .append("\n")

                .append("Pagado: ")
                .append(formatMoney(appointment.getPaidAmount()))
                .append("\n")

                .append("Saldo pendiente: ")
                .append(formatMoney(appointment.getPendingAmount()))
                .append("\n")

                .append("Estado de pago: ")
                .append(value(appointment.getPaymentStatus()))
                .append("\n")

                .append("Método de pago: ")
                .append(value(appointment.getPaymentMethod()))
                .append("\n")

                .append("Código de operación: ")
                .append(value(appointment.getOperationCode()))
                .append("\n");

        if (lead != null) {

            text.append("\nDATOS DE LA PRE-RESERVA\n")
                    .append("Interesado: ")
                    .append(safe(lead.getFullName()))
                    .append("\n")

                    .append("Correo: ")
                    .append(safe(lead.getEmail()))
                    .append("\n")

                    .append("Teléfono: ")
                    .append(safe(lead.getPhone()))
                    .append("\n")

                    .append("Modalidad: ")
                    .append(safe(lead.getModality()))
                    .append("\n")

                    .append("Mensaje: ")
                    .append(safe(lead.getMessage()))
                    .append("\n");
        }

        return text.toString();
    }

    /*
     * =========================================================
     * COMPONENTES DEL DISEÑO
     * =========================================================
     */

    private String summaryCard(
            String label,
            String value
    ) {

        return """
                <td width="33.33%"
                    valign="top"
                    style="padding: 4px;">

                    <div style="
                        min-height: 78px;
                        padding: 14px;
                        background-color: #f8fafc;
                        border: 1px solid #e2e8f0;
                        border-radius: 10px;
                    ">

                        <div style="
                            margin-bottom: 7px;
                            color: #64748b;
                            font-size: 11px;
                            font-weight: bold;
                            text-transform: uppercase;
                            letter-spacing: 0.4px;
                        ">
                """
                + escapeHtml(label)
                + """
                        </div>

                        <div style="
                            color: #1e3a5f;
                            font-size: 14px;
                            font-weight: bold;
                            line-height: 1.4;
                        ">
                """
                + escapeHtml(value)
                + """
                        </div>

                    </div>

                </td>
                """;
    }

    private String detailRow(
            String label,
            String value
    ) {

        return """
                <tr>
                    <td width="38%"
                        style="
                            padding: 13px 16px;
                            background-color: #f8fafc;
                            border-bottom: 1px solid #e2e8f0;
                            color: #64748b;
                            font-size: 13px;
                            font-weight: bold;
                        ">
                """
                + escapeHtml(label)
                + """
                    </td>

                    <td style="
                        padding: 13px 16px;
                        border-bottom: 1px solid #e2e8f0;
                        color: #1f2937;
                        font-size: 14px;
                    ">
                """
                + escapeHtml(value)
                + """
                    </td>
                </tr>
                """;
    }

    private String statusBadge(
            String value,
            String background,
            String color
    ) {

        return """
                <span style="
                    display: inline-block;
                    padding: 6px 11px;
                    border-radius: 20px;
                    font-size: 12px;
                    font-weight: bold;
                    background-color:
                """
                + background
                + "; color:"
                + color
                + """
                ;">
                """
                + escapeHtml(value)
                + "</span>";
    }

    /*
     * =========================================================
     * COLORES DE ESTADOS
     * =========================================================
     */

    private String getStatusBackground(String status) {

        String normalized = status.toUpperCase();

        if (normalized.contains("CANCEL")) {
            return "#fee2e2";
        }

        if (normalized.contains("ATEND")
                || normalized.contains("COMPLET")) {
            return "#dcfce7";
        }

        if (normalized.contains("CONFIRM")) {
            return "#d1fae5";
        }

        if (normalized.contains("REPROGRAM")) {
            return "#fef3c7";
        }

        return "#dbeafe";
    }

    private String getStatusColor(String status) {

        String normalized = status.toUpperCase();

        if (normalized.contains("CANCEL")) {
            return "#991b1b";
        }

        if (normalized.contains("ATEND")
                || normalized.contains("COMPLET")) {
            return "#166534";
        }

        if (normalized.contains("CONFIRM")) {
            return "#065f46";
        }

        if (normalized.contains("REPROGRAM")) {
            return "#92400e";
        }

        return "#1d4ed8";
    }

    private String getPaymentBackground(String status) {

        String normalized = status.toUpperCase();

        if (normalized.contains("PAGADO")
                || normalized.contains("COMPLET")) {
            return "#dcfce7";
        }

        if (normalized.contains("PARCIAL")) {
            return "#fef3c7";
        }

        return "#fee2e2";
    }

    private String getPaymentColor(String status) {

        String normalized = status.toUpperCase();

        if (normalized.contains("PAGADO")
                || normalized.contains("COMPLET")) {
            return "#166534";
        }

        if (normalized.contains("PARCIAL")) {
            return "#92400e";
        }

        return "#991b1b";
    }

    /*
     * =========================================================
     * UTILIDADES
     * =========================================================
     */

    private String formatMoney(BigDecimal amount) {

        BigDecimal safeAmount =
                amount != null
                        ? amount
                        : BigDecimal.ZERO;

        NumberFormat formatter =
                NumberFormat.getCurrencyInstance(
                        Locale.forLanguageTag("es-PE")
                );

        return formatter.format(safeAmount);
    }

    private String value(Object value) {

        if (value == null) {
            return "-";
        }

        String result =
                String.valueOf(value).trim();

        return result.isBlank()
                ? "-"
                : result;
    }

    private String safe(String value) {

        return value == null || value.isBlank()
                ? "-"
                : value.trim();
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}