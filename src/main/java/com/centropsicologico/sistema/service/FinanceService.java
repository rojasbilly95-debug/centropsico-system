package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.dto.FinanceSummaryDto;
import com.centropsicologico.sistema.entity.Expense;
import com.centropsicologico.sistema.entity.ExpenseCategory;
import com.centropsicologico.sistema.entity.Income;

import java.util.List;

public interface FinanceService {

    Income saveIncome(Income income);

    List<Income> findAllIncomes();

    ExpenseCategory saveExpenseCategory(ExpenseCategory category);

    List<ExpenseCategory> findAllExpenseCategories();

    Expense saveExpense(Expense expense);

    List<Expense> findAllExpenses();

    List<Expense> filterExpenses(Integer year, Integer month, Long categoryId, Boolean active, String responsible);

    void deleteExpense(Long id);

    FinanceSummaryDto getMonthlySummary(Integer year, Integer month);
}