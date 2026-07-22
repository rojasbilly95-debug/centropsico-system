package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.entity.Lead;

public interface EmailService {

    void notifyAdminsNewAppointment(Appointment appointment);

    void notifyAdminsNewAppointmentFromLead(
            Appointment appointment,
            Lead lead
    );
}