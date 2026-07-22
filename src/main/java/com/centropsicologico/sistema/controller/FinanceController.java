package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.dto.FinanceSummaryDto;
import com.centropsicologico.sistema.entity.Expense;
import com.centropsicologico.sistema.entity.ExpenseCategory;
import com.centropsicologico.sistema.entity.Income;
import com.centropsicologico.sistema.service.FinanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finances")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    /*
     * INGRESOS
     */
    @PostMapping("/incomes")
    public Income saveIncome(@RequestBody Income income) {
        return financeService.saveIncome(income);
    }

    @GetMapping("/incomes")
    public List<Income> findAllIncomes() {
        return financeService.findAllIncomes();
    }

    @GetMapping("/incomes/filter")
    public List<Income> filterIncomes(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String reviewStatus,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String search
    ) {
        return financeService.filterIncomes(
                year,
                month,
                startDate,
                endDate,
                active,
                reviewStatus,
                paymentMethod,
                search
        );
    }

    @PutMapping("/incomes/{id}/review")
    public Income reviewIncome(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Principal principal
    ) {
        String reviewStatus = request.get("reviewStatus");
        String observation = request.get("observation");

        String reviewedBy = principal != null && principal.getName() != null
                ? principal.getName()
                : "ADMIN";

        return financeService.reviewIncome(
                id,
                reviewStatus,
                observation,
                reviewedBy
        );
    }

    /*
     * CATEGORÍAS DE GASTO
     */
    @PostMapping("/expense-categories")
    public ExpenseCategory saveExpenseCategory(@RequestBody ExpenseCategory category) {
        return financeService.saveExpenseCategory(category);
    }

    @GetMapping("/expense-categories")
    public List<ExpenseCategory> findAllExpenseCategories() {
        return financeService.findAllExpenseCategories();
    }

    /*
     * GASTOS
     */
    @PostMapping("/expenses")
    public Expense saveExpense(@RequestBody Expense expense) {
        return financeService.saveExpense(expense);
    }

    @GetMapping("/expenses")
    public List<Expense> findAllExpenses() {
        return financeService.findAllExpenses();
    }

    @GetMapping("/expenses/filter")
    public List<Expense> filterExpenses(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String responsible,
            @RequestParam(required = false) String reviewStatus,
            @RequestParam(required = false) String search
    ) {
        return financeService.filterExpenses(
                year,
                month,
                startDate,
                endDate,
                categoryId,
                active,
                responsible,
                reviewStatus,
                search
        );
    }

    @PutMapping("/expenses/{id}/review")
    public Expense reviewExpense(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Principal principal
    ) {
        String reviewStatus = request.get("reviewStatus");
        String observation = request.get("observation");

        String reviewedBy = principal != null && principal.getName() != null
                ? principal.getName()
                : "ADMIN";

        return financeService.reviewExpense(
                id,
                reviewStatus,
                observation,
                reviewedBy
        );
    }

    @DeleteMapping("/expenses/{id}")
    public Map<String, Object> deleteExpense(@PathVariable Long id) {
        financeService.deleteExpense(id);

        return Map.of(
                "success", true,
                "message", "Gasto eliminado correctamente"
        );
    }

    /*
     * RESUMEN FINANCIERO
     */
    @GetMapping("/summary")
    public FinanceSummaryDto getMonthlySummary(
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        return financeService.getMonthlySummary(year, month);
    }
}