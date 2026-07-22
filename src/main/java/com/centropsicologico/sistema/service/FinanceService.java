package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.dto.FinanceSummaryDto;
import com.centropsicologico.sistema.entity.Expense;
import com.centropsicologico.sistema.entity.ExpenseCategory;
import com.centropsicologico.sistema.entity.Income;

import java.time.LocalDate;
import java.util.List;

public interface FinanceService {

    Income saveIncome(Income income);

    List<Income> findAllIncomes();

    List<Income> filterIncomes(
            Integer year,
            Integer month,
            LocalDate startDate,
            LocalDate endDate,
            Boolean active,
            String reviewStatus,
            String paymentMethod,
            String search
    );

    Income reviewIncome(
            Long id,
            String reviewStatus,
            String observation,
            String reviewedBy
    );

    ExpenseCategory saveExpenseCategory(ExpenseCategory category);

    List<ExpenseCategory> findAllExpenseCategories();

    Expense saveExpense(Expense expense);

    List<Expense> findAllExpenses();

    List<Expense> filterExpenses(
            Integer year,
            Integer month,
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId,
            Boolean active,
            String responsible,
            String reviewStatus,
            String search
    );

    Expense reviewExpense(
            Long id,
            String reviewStatus,
            String observation,
            String reviewedBy
    );

    void deleteExpense(Long id);

    FinanceSummaryDto getMonthlySummary(Integer year, Integer month);
}