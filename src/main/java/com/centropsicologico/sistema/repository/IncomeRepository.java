package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findByDateBetweenAndActiveTrue(LocalDate startDate, LocalDate endDate);
}