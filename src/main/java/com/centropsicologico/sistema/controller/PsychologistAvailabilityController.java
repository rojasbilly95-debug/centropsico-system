package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.entity.PsychologistAvailability;
import com.centropsicologico.sistema.service.PsychologistAvailabilityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/psychologist-availabilities")
public class PsychologistAvailabilityController {

    private final PsychologistAvailabilityService availabilityService;

    public PsychologistAvailabilityController(PsychologistAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    public PsychologistAvailability save(@RequestBody PsychologistAvailability availability) {
        return availabilityService.save(availability);
    }

    @GetMapping
    public List<PsychologistAvailability> findAll() {
        return availabilityService.findAll();
    }

    @GetMapping("/active")
    public List<PsychologistAvailability> findActive() {
        return availabilityService.findActive();
    }

    @GetMapping("/psychologist/{psychologistId}")
    public List<PsychologistAvailability> findByPsychologist(@PathVariable Long psychologistId) {
        return availabilityService.findByPsychologist(psychologistId);
    }

    @GetMapping("/{id}")
    public PsychologistAvailability findById(@PathVariable Long id) {
        return availabilityService.findById(id);
    }

    @PutMapping("/{id}")
    public PsychologistAvailability update(
            @PathVariable Long id,
            @RequestBody PsychologistAvailability availability) {
        return availabilityService.update(id, availability);
    }

    @PatchMapping("/{id}/toggle-active")
    public PsychologistAvailability toggleActive(@PathVariable Long id) {
        return availabilityService.toggleActive(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        availabilityService.delete(id);
    }

    @GetMapping("/psychologist/{psychologistId}/count")
public long countByPsychologist(@PathVariable Long psychologistId) {
    return availabilityService.countActiveByPsychologist(psychologistId);
}
}