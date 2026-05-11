package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByDateBetweenAndActiveTrue(LocalDate startDate, LocalDate endDate);
}