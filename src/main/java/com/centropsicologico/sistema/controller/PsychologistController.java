package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.entity.Psychologist;
import com.centropsicologico.sistema.service.PsychologistService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/psychologists")
public class PsychologistController {

    private final PsychologistService psychologistService;

    public PsychologistController(PsychologistService psychologistService) {
        this.psychologistService = psychologistService;
    }

    @PostMapping
    public Psychologist save(@RequestBody Psychologist psychologist) {
        return psychologistService.save(psychologist);
    }

    @GetMapping
    public List<Psychologist> findAll() {
        return psychologistService.findAll();
    }

    @GetMapping("/active")
    public List<Psychologist> findActivePsychologists() {
        return psychologistService.findActivePsychologists();
    }

    @GetMapping("/{id}")
    public Psychologist findById(@PathVariable Long id) {
        return psychologistService.findById(id);
    }

    @PutMapping("/{id}")
    public Psychologist update(@PathVariable Long id, @RequestBody Psychologist psychologist) {
        return psychologistService.update(id, psychologist);
    }

    @PatchMapping("/{id}/toggle-active")
    public Psychologist toggleActive(@PathVariable Long id) {
        return psychologistService.toggleActive(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        psychologistService.delete(id);
    }
}