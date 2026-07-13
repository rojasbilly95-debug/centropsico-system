package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByActiveTrue();

    List<Expense> findByDateBetweenAndActiveTrue(LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT e
            FROM Expense e
            LEFT JOIN e.category c
            WHERE (:startDate IS NULL OR e.date >= :startDate)
              AND (:endDate IS NULL OR e.date <= :endDate)
              AND (:categoryId IS NULL OR c.id = :categoryId)
              AND (:active IS NULL OR e.active = :active)
              AND (
                    :responsible IS NULL
                    OR LOWER(COALESCE(e.responsible, '')) LIKE LOWER(CONCAT('%', :responsible, '%'))
                  )
            ORDER BY e.date DESC, e.id DESC
            """)
    List<Expense> filterExpenses(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId,
            @Param("active") Boolean active,
            @Param("responsible") String responsible
    );
}