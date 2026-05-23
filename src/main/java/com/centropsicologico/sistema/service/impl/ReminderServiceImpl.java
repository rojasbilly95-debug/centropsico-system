package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.dto.ReminderDTO;
import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.entity.User;
import com.centropsicologico.sistema.repository.AppointmentRepository;
import com.centropsicologico.sistema.repository.LeadRepository;
import com.centropsicologico.sistema.repository.UserRepository;
import com.centropsicologico.sistema.service.ReminderService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReminderServiceImpl implements ReminderService {

    private final AppointmentRepository appointmentRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;

    public ReminderServiceImpl(
            AppointmentRepository appointmentRepository,
            LeadRepository leadRepository,
            UserRepository userRepository) {

        this.appointmentRepository = appointmentRepository;
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ReminderDTO> getCurrentUserReminders(String email) {

        List<ReminderDTO> reminders = new ArrayList<>();

        User currentUser = userRepository.findByEmail(email)
                .orElse(null);

        String role = currentUser != null ? currentUser.getRole() : "";

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Appointment> appointmentsTomorrow = appointmentRepository.findAll()
                .stream()
                .filter(appointment -> appointment.getDate() != null)
                .filter(appointment -> appointment.getDate().equals(tomorrow))
                .toList();

        for (Appointment appointment : appointmentsTomorrow) {

            String patientName = appointment.getPatient() != null
                    ? appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName()
                    : "paciente no especificado";

            String psychologistName = appointment.getPsychologist() != null
                    ? appointment.getPsychologist().getFirstName() + " " + appointment.getPsychologist().getLastName()
                    : "psicólogo no especificado";

            reminders.add(new ReminderDTO(
                    "Cita programada para mañana",
                    "Recuerda que mañana tienes una cita a las "
                            + appointment.getStartTime()
                            + " con el psicólogo "
                            + psychologistName
                            + ". Paciente: "
                            + patientName
                            + ".",
                    "appointment"
            ));
        }

        if ("ADMIN".equals(role) || "RECEPCIONISTA".equals(role)) {

            long pendingLeads = leadRepository.findAll()
                    .stream()
                    .filter(lead -> "PAGO_EN_REVISION".equalsIgnoreCase(lead.getStatus()))
                    .count();

            if (pendingLeads > 0) {
                reminders.add(new ReminderDTO(
                        "Pre-reservas pendientes",
                        "Hay " + pendingLeads + " pre-reservas esperando validación de pago.",
                        "payment"
                ));
            }
        }

        return reminders;
    }
}