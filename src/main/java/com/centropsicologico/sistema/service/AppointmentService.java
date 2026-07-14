package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.dto.PaymentRequestDto;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    Appointment save(Appointment appointment);

    Appointment updateStatus(Long id, String status);

    Appointment updateStatusForPsychologist(Long id, String status, String psychologistEmail);

    List<Appointment> findAll();

    Appointment findById(Long id);

    Appointment update(Long id, Appointment appointment);

    void delete(Long id);

    List<Appointment> findByDate(LocalDate date);

    Appointment payAppointment(Long id, PaymentRequestDto paymentRequest);

    List<Appointment> findByPsychologistEmail(String email);

    List<Appointment> findByPsychologistEmailAndDate(String email, LocalDate date);
}