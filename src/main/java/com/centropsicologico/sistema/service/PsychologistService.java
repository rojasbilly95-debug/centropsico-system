package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.entity.Psychologist;

import java.util.List;

public interface PsychologistService {

    Psychologist save(Psychologist psychologist);

    List<Psychologist> findAll();

    List<Psychologist> findActivePsychologists();

    Psychologist findById(Long id);

    Psychologist update(Long id, Psychologist psychologist);

    Psychologist toggleActive(Long id);

    void delete(Long id);
}