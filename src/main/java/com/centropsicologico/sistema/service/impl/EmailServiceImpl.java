package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.entity.Lead;
import com.centropsicologico.sistema.entity.User;
import com.centropsicologico.sistema.repository.UserRepository;
import com.centropsicologico.sistema.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${mailpro.api.base-url:https://api.mailpro.com/v2}")
    private String mailproBaseUrl;

    @Value("${mailpro.api.username:}")
    private String mailproUsername;

    @Value("${mailpro.api.key:}")
    private String mailproApiKey;

    @Value("${app.mail.from:}")
    private String mailFrom;

    public EmailServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void notifyAdminsNewAppointment(Appointment appointment) {
        if (appointment == null) {
            System.err.println("EMAIL: No se envió correo porque la cita es null.");
            return;
        }

        System.out.println("EMAIL API: Preparando correo por nueva cita desde panel. ID cita: " + appointment.getId());

        String subject = "Nueva cita registrada - CentroPsico";

        String body = buildAppointmentEmailBody(
                appointment,
                "Se ha registrado una nueva cita desde el panel administrativo.",
                null
        );

        sendToAdmins(subject, body);
    }

    @Override
    public void notifyAdminsNewAppointmentFromLead(Appointment appointment, Lead lead) {
        if (appointment == null) {
            System.err.println("EMAIL API: No se envió correo porque la cita desde pre-reserva es null.");
            return;
        }

        System.out.println("EMAIL API: Preparando correo por cita creada desde pre-reserva. ID cita: " + appointment.getId());

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

    private void sendToAdmins(String subject, String body) {
        System.out.println("====================================");
        System.out.println("INICIANDO ENVÍO DE CORREO POR MAILPRO API");
        System.out.println("MAILPRO_USERNAME CONFIGURADO: " + mailproUsername);
        System.out.println("MAIL_FROM CONFIGURADO: " + mailFrom);

        if (mailproUsername == null || mailproUsername.isBlank()) {
            System.err.println("EMAIL API ERROR: mailpro.api.username está vacío. Revisa MAILPRO_USERNAME en Render.");
            System.out.println("====================================");
            return;
        }

        if (mailproApiKey == null || mailproApiKey.isBlank()) {
            System.err.println("EMAIL API ERROR: mailpro.api.key está vacío. Revisa MAILPRO_API_KEY en Render.");
            System.out.println("====================================");
            return;
        }

        if (mailFrom == null || mailFrom.isBlank()) {
            System.err.println("EMAIL API ERROR: app.mail.from está vacío. Revisa MAIL_FROM en Render.");
            System.out.println("====================================");
            return;
        }

        List<User> admins = userRepository.findByRoleAndActiveTrue("ADMIN");

        System.out.println("ADMINISTRADORES ENCONTRADOS: " + (admins != null ? admins.size() : 0));

        if (admins == null || admins.isEmpty()) {
            System.err.println("EMAIL API ERROR: No se encontraron administradores activos para enviar correo.");
            System.out.println("====================================");
            return;
        }

        String token = getMailproAccessToken();

        if (token == null || token.isBlank()) {
            System.err.println("EMAIL API ERROR: No se pudo obtener token de Mailpro.");
            System.out.println("====================================");
            return;
        }

        for (User admin : admins) {
            if (admin == null || admin.getEmail() == null || admin.getEmail().isBlank()) {
                System.err.println("EMAIL API WARNING: Admin sin correo, se omite.");
                continue;
            }

            sendEmailByApi(
                    token,
                    admin.getEmail().trim(),
                    subject,
                    body
            );
        }

        System.out.println("FIN DEL PROCESO DE ENVÍO POR MAILPRO API");
        System.out.println("====================================");
    }

    private String getMailproAccessToken() {
        try {
            System.out.println("EMAIL API: Solicitando token a Mailpro...");

            String url = mailproBaseUrl + "/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "password");
            form.add("username", mailproUsername);
            form.add("password", mailproApiKey);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                System.err.println("EMAIL API ERROR: Respuesta inválida al obtener token. Status: " + response.getStatusCode());
                return null;
            }

            Object accessToken = response.getBody().get("access_token");

            if (accessToken == null) {
                System.err.println("EMAIL API ERROR: Mailpro no devolvió access_token. Respuesta: " + response.getBody());
                return null;
            }

            System.out.println("EMAIL API OK: Token obtenido correctamente.");
            return accessToken.toString();

        } catch (Exception exception) {
            System.err.println("EMAIL API ERROR: No se pudo obtener token de Mailpro.");
            System.err.println("EMAIL API ERROR MESSAGE: " + exception.getMessage());
            exception.printStackTrace();
            return null;
        }
    }

    private void sendEmailByApi(
            String token,
            String to,
            String subject,
            String body
    ) {
        try {
            System.out.println("EMAIL API: Intentando enviar correo a: " + to);

            String url = mailproBaseUrl + "/email/send";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            Map<String, Object> payload = new HashMap<>();
            payload.put("to", to);
            payload.put("subject", subject);
            payload.put("text", body);
            payload.put("html", buildHtmlBody(body));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("EMAIL API OK: Correo enviado correctamente a: " + to);
                System.out.println("EMAIL API RESPONSE: " + response.getBody());
            } else {
                System.err.println("EMAIL API ERROR: No se pudo enviar correo a " + to);
                System.err.println("EMAIL API STATUS: " + response.getStatusCode());
                System.err.println("EMAIL API RESPONSE: " + response.getBody());
            }

        } catch (Exception exception) {
            System.err.println("EMAIL API ERROR: Falló el envío por API a " + to);
            System.err.println("EMAIL API ERROR MESSAGE: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private String buildHtmlBody(String body) {
        String escaped = body == null ? "" : body
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");

        return "<html><body style='font-family: Arial, sans-serif; font-size: 14px; color: #1f2937;'>"
                + escaped
                + "</body></html>";
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