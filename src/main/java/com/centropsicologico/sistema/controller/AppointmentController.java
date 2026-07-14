package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.dto.PaymentRequestDto;
import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.service.AppointmentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public Appointment save(@RequestBody Appointment appointment) {
        return appointmentService.save(appointment);
    }

    @GetMapping
    public List<Appointment> findAll() {
        return appointmentService.findAll();
    }

    @GetMapping("/{id}")
    public Appointment findById(@PathVariable Long id) {
        return appointmentService.findById(id);
    }

    @GetMapping("/by-date")
    public List<Appointment> getByDate(@RequestParam String date) {
        return appointmentService.findByDate(LocalDate.parse(date));
    }

    @PutMapping("/{id}")
    public Appointment update(@PathVariable Long id, @RequestBody Appointment appointment) {
        return appointmentService.update(id, appointment);
    }

    @PutMapping("/{id}/pay")
    public Appointment payAppointment(
            @PathVariable Long id,
            @RequestBody PaymentRequestDto paymentRequest
    ) {
        return appointmentService.payAppointment(id, paymentRequest);
    }

    @PutMapping("/{id}/status")
    public Appointment updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Principal principal,
            Authentication authentication
    ) {
        if (hasAuthority(authentication, "PSICOLOGO")
                && !hasAuthority(authentication, "ADMIN")
                && !hasAuthority(authentication, "RECEPCIONISTA")) {

            return appointmentService.updateStatusForPsychologist(
                    id,
                    status,
                    principal.getName()
            );
        }

        return appointmentService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        appointmentService.delete(id);
    }

    @GetMapping("/my")
    public List<Appointment> findMyAppointments(Principal principal) {
        return appointmentService.findByPsychologistEmail(principal.getName());
    }

    @GetMapping("/my/by-date")
    public List<Appointment> findMyAppointmentsByDate(
            @RequestParam String date,
            Principal principal
    ) {
        return appointmentService.findByPsychologistEmailAndDate(
                principal.getName(),
                LocalDate.parse(date)
        );
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication != null
                && authentication.getAuthorities() != null
                && authentication.getAuthorities()
                        .stream()
                        .anyMatch(item -> authority.equals(item.getAuthority()));
    }
}