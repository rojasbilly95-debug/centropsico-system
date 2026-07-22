package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findByActiveTrueOrderByDateDescIdDesc();

    List<Income> findByDateBetweenAndActiveTrue(LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT i
            FROM Income i
            WHERE (:startDate IS NULL OR i.date >= :startDate)
              AND (:endDate IS NULL OR i.date <= :endDate)
              AND (:active IS NULL OR i.active = :active)
              AND (:reviewStatus IS NULL OR i.reviewStatus = :reviewStatus)
              AND (
                    :paymentMethod IS NULL
                    OR LOWER(COALESCE(i.paymentMethod, '')) = LOWER(:paymentMethod)
                  )
              AND (
                    :search IS NULL
                    OR LOWER(COALESCE(i.description, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(COALESCE(i.origin, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(COALESCE(i.reference, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            ORDER BY i.date DESC, i.id DESC
            """)
    List<Income> filterIncomes(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("active") Boolean active,
            @Param("reviewStatus") String reviewStatus,
            @Param("paymentMethod") String paymentMethod,
            @Param("search") String search
    );
}