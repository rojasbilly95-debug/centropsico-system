package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.dto.PublicAvailabilityDto;
import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.entity.Psychologist;
import com.centropsicologico.sistema.entity.PsychologistAvailability;
import com.centropsicologico.sistema.entity.ServiceEntity;
import com.centropsicologico.sistema.repository.AppointmentRepository;
import com.centropsicologico.sistema.repository.PsychologistAvailabilityRepository;
import com.centropsicologico.sistema.repository.ServiceRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicAvailabilityController {

    private final PsychologistAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;

    public PublicAvailabilityController(
            PsychologistAvailabilityRepository availabilityRepository,
            AppointmentRepository appointmentRepository,
            ServiceRepository serviceRepository) {
        this.availabilityRepository = availabilityRepository;
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
    }

    @GetMapping("/availability")
    public List<PublicAvailabilityDto> getAvailability(
            @RequestParam Long serviceId,
            @RequestParam String date) {

        LocalDate selectedDate = LocalDate.parse(date);

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        int duration = service.getDurationMinutes() != null
                ? service.getDurationMinutes()
                : 60;

        List<PsychologistAvailability> availabilities =
                availabilityRepository.findByDayOfWeekAndActiveTrue(selectedDate.getDayOfWeek());

        List<PublicAvailabilityDto> response = new ArrayList<>();

        for (PsychologistAvailability availability : availabilities) {
            Psychologist psychologist = availability.getPsychologist();

            if (psychologist == null || Boolean.FALSE.equals(psychologist.getActive())) {
                continue;
            }

            List<Appointment> appointments =
                    appointmentRepository.findByPsychologistIdAndDate(psychologist.getId(), selectedDate);

            List<String> slots = generateAvailableSlots(
                    availability.getStartTime(),
                    availability.getEndTime(),
                    duration,
                    appointments
            );

            if (!slots.isEmpty()) {
                response.add(new PublicAvailabilityDto(
                        psychologist.getId(),
                        psychologist.getFirstName() + " " + psychologist.getLastName(),
                        psychologist.getSpecialty(),
                        selectedDate.toString(),
                        slots
                ));
            }
        }

        return response;
    }

    private List<String> generateAvailableSlots(
            LocalTime availabilityStart,
            LocalTime availabilityEnd,
            int durationMinutes,
            List<Appointment> appointments) {

        List<String> slots = new ArrayList<>();

        LocalTime current = availabilityStart;

        while (!current.plusMinutes(durationMinutes).isAfter(availabilityEnd)) {
            LocalTime slotStart = current;
            LocalTime slotEnd = current.plusMinutes(durationMinutes);

            boolean occupied = appointments.stream().anyMatch(appointment ->
                    appointment.getStartTime().isBefore(slotEnd)
                            && appointment.getEndTime().isAfter(slotStart)
            );

            if (!occupied) {
                slots.add(slotStart.toString());
            }

            current = current.plusMinutes(durationMinutes);
        }

        return slots;
    }
}