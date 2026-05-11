package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.entity.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    Appointment save(Appointment appointment);

    Appointment updateStatus(Long id, String status);

    List<Appointment> findAll();

    Appointment findById(Long id);

    Appointment update(Long id, Appointment appointment);

    void delete(Long id);

    List<Appointment> findByDate(LocalDate date);

    Appointment payAppointment(Long id, com.centropsicologico.sistema.dto.PaymentRequestDto paymentRequest);

    List<Appointment> findByPsychologistEmail(String email);

    List<Appointment> findByPsychologistEmailAndDate(String email, LocalDate date);
}