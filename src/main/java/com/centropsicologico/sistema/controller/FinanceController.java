package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.dto.FinanceSummaryDto;
import com.centropsicologico.sistema.entity.Expense;
import com.centropsicologico.sistema.entity.ExpenseCategory;
import com.centropsicologico.sistema.entity.Income;
import com.centropsicologico.sistema.service.FinanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finances")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping("/incomes")
    public Income saveIncome(@RequestBody Income income) {
        return financeService.saveIncome(income);
    }

    @GetMapping("/incomes")
    public List<Income> findAllIncomes() {
        return financeService.findAllIncomes();
    }

    @PostMapping("/expense-categories")
    public ExpenseCategory saveExpenseCategory(@RequestBody ExpenseCategory category) {
        return financeService.saveExpenseCategory(category);
    }

    @GetMapping("/expense-categories")
    public List<ExpenseCategory> findAllExpenseCategories() {
        return financeService.findAllExpenseCategories();
    }

    @PostMapping("/expenses")
    public Expense saveExpense(@RequestBody Expense expense) {
        return financeService.saveExpense(expense);
    }

    @GetMapping("/expenses")
    public List<Expense> findAllExpenses() {
        return financeService.findAllExpenses();
    }

    @GetMapping("/summary")
    public FinanceSummaryDto getMonthlySummary(
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        return financeService.getMonthlySummary(year, month);
    }
}