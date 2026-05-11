package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.dto.FinanceSummaryDto;
import com.centropsicologico.sistema.entity.Expense;
import com.centropsicologico.sistema.entity.ExpenseCategory;
import com.centropsicologico.sistema.entity.Income;
import com.centropsicologico.sistema.exception.BusinessRuleException;
import com.centropsicologico.sistema.exception.ResourceNotFoundException;
import com.centropsicologico.sistema.repository.ExpenseCategoryRepository;
import com.centropsicologico.sistema.repository.ExpenseRepository;
import com.centropsicologico.sistema.repository.IncomeRepository;
import com.centropsicologico.sistema.service.FinanceService;
import org.springframework.stereotype.Service;
import com.centropsicologico.sistema.service.NotificationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class FinanceServiceImpl implements FinanceService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final NotificationService notificationService;

    public FinanceServiceImpl(
            IncomeRepository incomeRepository,
            ExpenseRepository expenseRepository,
            ExpenseCategoryRepository expenseCategoryRepository,
            NotificationService notificationService) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.expenseCategoryRepository = expenseCategoryRepository;
        this.notificationService = notificationService;
    }

@Override
public Income saveIncome(Income income) {

    if (income.getDescription() == null || income.getDescription().trim().isEmpty()) {
        throw new BusinessRuleException("La descripción del ingreso es obligatoria");
    }

    if (income.getAmount() == null || income.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new BusinessRuleException("El monto del ingreso debe ser mayor a cero");
    }

    if (income.getDate() == null) {
        throw new BusinessRuleException("La fecha del ingreso es obligatoria");
    }

    if (income.getPaymentMethod() == null || income.getPaymentMethod().trim().isEmpty()) {
        throw new BusinessRuleException("El método de pago es obligatorio");
    }

    income.setActive(true);

    Income savedIncome = incomeRepository.save(income);

    notificationService.createForRole(
            "Ingreso registrado",
            "Se registró un ingreso de S/ " + savedIncome.getAmount()
                    + " por " + savedIncome.getDescription(),
            "INGRESO_REGISTRADO",
            "ADMIN"
    );

    return savedIncome;
}

    @Override
    public List<Income> findAllIncomes() {
        return incomeRepository.findAll();
    }

    @Override
    public ExpenseCategory saveExpenseCategory(ExpenseCategory category) {
        if (expenseCategoryRepository.existsByName(category.getName())) {
            throw new BusinessRuleException("Ya existe una categoría con ese nombre");
        }

        return expenseCategoryRepository.save(category);
    }

    @Override
    public List<ExpenseCategory> findAllExpenseCategories() {
        return expenseCategoryRepository.findAll();
    }

@Override
public Expense saveExpense(Expense expense) {

    if (expense.getDescription() == null || expense.getDescription().trim().isEmpty()) {
        throw new BusinessRuleException("La descripción del gasto es obligatoria");
    }

    if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new BusinessRuleException("El monto del gasto debe ser mayor a cero");
    }

    if (expense.getDate() == null) {
        throw new BusinessRuleException("La fecha del gasto es obligatoria");
    }

    if (expense.getCategory() == null || expense.getCategory().getId() == null) {
        throw new BusinessRuleException("Debe seleccionar una categoría de gasto");
    }

    ExpenseCategory category = expenseCategoryRepository.findById(expense.getCategory().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Categoría de gasto no encontrada"));

    expense.setCategory(category);
    expense.setActive(true);

    Expense savedExpense = expenseRepository.save(expense);

    notificationService.createForRole(
            "Gasto registrado",
            "Se registró un gasto de S/ " + savedExpense.getAmount()
                    + " en " + category.getName()
                    + " por " + savedExpense.getDescription(),
            "GASTO_REGISTRADO",
            "ADMIN"
    );

    if (savedExpense.getAmount().compareTo(new BigDecimal("500")) >= 0) {
        notificationService.createForRole(
                "Alerta de gasto alto",
                "Se registró un gasto alto de S/ " + savedExpense.getAmount()
                        + " en " + category.getName(),
                "GASTO_ALTO",
                "ADMIN"
        );
    }

    return savedExpense;
}

    @Override
    public List<Expense> findAllExpenses() {
        return expenseRepository.findAll();
    }

    @Override
    public FinanceSummaryDto getMonthlySummary(Integer year, Integer month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Income> incomes = incomeRepository.findByDateBetweenAndActiveTrue(startDate, endDate);
        List<Expense> expenses = expenseRepository.findByDateBetweenAndActiveTrue(startDate, endDate);

        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal profit = totalIncome.subtract(totalExpense);

        String result = profit.compareTo(BigDecimal.ZERO) >= 0 ? "GANANCIA" : "PÉRDIDA";

        return new FinanceSummaryDto(totalIncome, totalExpense, profit, result);
    }
}