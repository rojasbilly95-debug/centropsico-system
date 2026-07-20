package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.Psychologist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PsychologistRepository
        extends JpaRepository<Psychologist, Long> {

    /*
     * Todos los psicólogos activos.
     */
    List<Psychologist> findByActiveTrue();

    /*
     * Psicólogos activos cuya especialidad coincida
     * con el servicio seleccionado.
     *
     * La comparación no distingue mayúsculas y minúsculas.
     */
    List<Psychologist>
    findByActiveTrueAndSpecialtyContainingIgnoreCaseOrderByFirstNameAscLastNameAsc(
            String specialty
    );
}