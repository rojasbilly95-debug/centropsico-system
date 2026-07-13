package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findAllByOrderByIdDesc();

    @Query("""
            SELECT p
            FROM Promotion p
            WHERE p.active = true
              AND (p.startDate IS NULL OR p.startDate <= :today)
              AND (p.endDate IS NULL OR p.endDate >= :today)
            ORDER BY p.startDate DESC, p.id DESC
            """)
    List<Promotion> findPublicActivePromotions(@Param("today") LocalDate today);
}