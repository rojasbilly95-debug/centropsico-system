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
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
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

    public EmailServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
    }

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

    private void sendToAdmins(String subject, String body) {

        System.out.println("====================================");
        System.out.println(
                "EMAIL BREVO: INICIANDO ENVÍO POR API HTTPS"
        );

        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            System.err.println(
                    "EMAIL BREVO ERROR: BREVO_API_KEY está vacío."
            );
            System.out.println("====================================");
            return;
        }

        if (mailFrom == null || mailFrom.isBlank()) {
            System.err.println(
                    "EMAIL BREVO ERROR: MAIL_FROM está vacío."
            );
            System.out.println("====================================");
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
            System.out.println("====================================");
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

            sendEmailByBrevo(
                    admin.getEmail().trim(),
                    admin.getFirstName(),
                    subject,
                    body
            );
        }

        System.out.println(
                "EMAIL BREVO: FINALIZÓ EL PROCESO DE ENVÍO"
        );
        System.out.println("====================================");
    }

    private void sendEmailByBrevo(
            String to,
            String recipientName,
            String subject,
            String body
    ) {

        try {
            System.out.println(
                    "EMAIL BREVO: Enviando correo a: " + to
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.set("api-key", brevoApiKey.trim());

            Map<String, Object> sender = new LinkedHashMap<>();
            sender.put("name", mailFromName.trim());
            sender.put("email", mailFrom.trim());

            Map<String, Object> recipient = new LinkedHashMap<>();
            recipient.put("email", to.trim());

            if (recipientName != null
                    && !recipientName.isBlank()) {
                recipient.put("name", recipientName.trim());
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sender", sender);
            payload.put("to", List.of(recipient));
            payload.put("subject", subject);
            payload.put("htmlContent", buildHtmlBody(body));
            payload.put("textContent", body);

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(payload, headers);

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
                        "EMAIL BREVO OK: Correo registrado correctamente."
                );

                System.out.println(
                        "EMAIL BREVO DESTINATARIO: " + to
                );

                System.out.println(
                        "EMAIL BREVO RESPONSE: " + responseBody
                );

            } else {
                System.err.println(
                        "EMAIL BREVO ERROR: Brevo respondió, "
                                + "pero no devolvió messageId."
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