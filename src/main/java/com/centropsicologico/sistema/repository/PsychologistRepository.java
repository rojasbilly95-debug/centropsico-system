package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.Psychologist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PsychologistRepository extends JpaRepository<Psychologist, Long> {

    List<Psychologist> findByActiveTrue();
}