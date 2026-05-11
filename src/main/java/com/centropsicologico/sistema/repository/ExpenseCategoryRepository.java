package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {

    boolean existsByName(String name);
}