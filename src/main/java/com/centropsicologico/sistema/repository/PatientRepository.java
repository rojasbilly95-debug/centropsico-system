package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByDni(String dni);

    boolean existsByDni(String dni);

    List<Patient> findByActiveTrue();
}