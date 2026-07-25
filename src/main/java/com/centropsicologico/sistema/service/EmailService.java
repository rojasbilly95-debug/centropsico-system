package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.entity.Lead;
import com.centropsicologico.sistema.entity.User;

public interface EmailService {

    void notifyAdminsNewAppointment(
            Appointment appointment
    );

    void notifyAdminsNewAppointmentFromLead(
            Appointment appointment,
            Lead lead
    );

    void sendPasswordResetEmail(
            User user,
            String rawToken,
            long expirationMinutes
    );

    void sendPasswordChangedConfirmation(
            User user
    );
}