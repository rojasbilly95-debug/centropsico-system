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
import com.centropsicologico.sistema.service.AuditLogService;
import com.centropsicologico.sistema.service.FinanceService;
import com.centropsicologico.sistema.service.NotificationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FinanceServiceImpl implements FinanceService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public FinanceServiceImpl(
            IncomeRepository incomeRepository,
            ExpenseRepository expenseRepository,
            ExpenseCategoryRepository expenseCategoryRepository,
            NotificationService notificationService,
            AuditLogService auditLogService
    ) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.expenseCategoryRepository = expenseCategoryRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    /*
     * INGRESOS
     */
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

        income.setDescription(income.getDescription().trim());
        income.setPaymentMethod(income.getPaymentMethod().trim());
        income.setActive(true);

        if (income.getReviewStatus() == null || income.getReviewStatus().trim().isEmpty()) {
            income.setReviewStatus("PENDIENTE");
        } else {
            income.setReviewStatus(normalizeReviewStatus(income.getReviewStatus()));
        }

        if (income.getOrigin() == null || income.getOrigin().trim().isEmpty()) {
            income.setOrigin("MANUAL");
        } else {
            income.setOrigin(income.getOrigin().trim().toUpperCase());
        }

        if (income.getReference() != null) {
            income.setReference(income.getReference().trim());
        }

        Income savedIncome = incomeRepository.save(income);

        notificationService.createForRole(
                "Ingreso registrado",
                "Se registró un ingreso de S/ " + savedIncome.getAmount()
                        + " por " + savedIncome.getDescription(),
                "INGRESO_REGISTRADO",
                "ADMIN"
        );

        auditLogService.record(
                "FINANZAS",
                "REGISTRO DE INGRESO",
                "Income",
                savedIncome.getId(),
                "Se registró un ingreso de S/ "
                        + savedIncome.getAmount()
                        + " por "
                        + savedIncome.getDescription()
                        + ". Método de pago: "
                        + savedIncome.getPaymentMethod()
                        + ". Estado de revisión: "
                        + savedIncome.getReviewStatus()
        );

        return savedIncome;
    }

    @Override
    public List<Income> findAllIncomes() {
        return incomeRepository.findByActiveTrueOrderByDateDescIdDesc();
    }

    @Override
    public List<Income> filterIncomes(
            Integer year,
            Integer month,
            LocalDate startDate,
            LocalDate endDate,
            Boolean active,
            String reviewStatus,
            String paymentMethod,
            String search
    ) {
        LocalDate[] range = resolveDateRange(year, month, startDate, endDate);

        String cleanReviewStatus = normalizeNullable(reviewStatus);
        String cleanPaymentMethod = normalizeNullable(paymentMethod);
        String cleanSearch = cleanNullable(search);

        if (cleanReviewStatus != null) {
            cleanReviewStatus = normalizeReviewStatus(cleanReviewStatus);
        }

        return incomeRepository.filterIncomes(
                range[0],
                range[1],
                active,
                cleanReviewStatus,
                cleanPaymentMethod,
                cleanSearch
        );
    }

    @Override
    public Income reviewIncome(
            Long id,
            String reviewStatus,
            String observation,
            String reviewedBy
    ) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingreso no encontrado"));

        String status = normalizeReviewStatus(reviewStatus);

        income.setReviewStatus(status);
        income.setReviewedBy(cleanNullable(reviewedBy));
        income.setReviewedAt(LocalDateTime.now());
        income.setReviewObservation(cleanNullable(observation));

        if ("ANULADO".equals(status)) {
            income.setActive(false);
        }

        Income saved = incomeRepository.save(income);

        notificationService.createForRole(
                "Ingreso revisado",
                "El ingreso de S/ " + saved.getAmount()
                        + " fue marcado como " + saved.getReviewStatus(),
                "INGRESO_REVISADO",
                "ADMIN"
        );

        auditLogService.record(
                "FINANZAS",
                "REVISIÓN DE INGRESO",
                "Income",
                saved.getId(),
                "El ingreso de S/ "
                        + saved.getAmount()
                        + " fue marcado como "
                        + saved.getReviewStatus()
                        + ". Observación: "
                        + safe(saved.getReviewObservation())
        );

        return saved;
    }

    /*
     * CATEGORÍAS DE GASTO
     */
    @Override
    public ExpenseCategory saveExpenseCategory(ExpenseCategory category) {
        if (category == null || category.getName() == null || category.getName().trim().isEmpty()) {
            throw new BusinessRuleException("El nombre de la categoría es obligatorio");
        }

        category.setName(category.getName().trim());

        if (expenseCategoryRepository.existsByName(category.getName())) {
            throw new BusinessRuleException("Ya existe una categoría con ese nombre");
        }

        ExpenseCategory savedCategory = expenseCategoryRepository.save(category);

        auditLogService.recordAndNotifyAdmin(
                "FINANZAS",
                "REGISTRO DE CATEGORÍA DE GASTO",
                "ExpenseCategory",
                savedCategory.getId(),
                "Se registró la categoría de gasto "
                        + savedCategory.getName()
        );

        return savedCategory;
    }

    @Override
    public List<ExpenseCategory> findAllExpenseCategories() {
        return expenseCategoryRepository.findAll();
    }

    /*
     * GASTOS
     */
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

        expense.setDescription(expense.getDescription().trim());

        if (expense.getResponsible() != null) {
            expense.setResponsible(expense.getResponsible().trim());
        }

        expense.setCategory(category);
        expense.setActive(true);

        if (expense.getReviewStatus() == null || expense.getReviewStatus().trim().isEmpty()) {
            expense.setReviewStatus("PENDIENTE");
        } else {
            expense.setReviewStatus(normalizeReviewStatus(expense.getReviewStatus()));
        }

        if (expense.getOrigin() == null || expense.getOrigin().trim().isEmpty()) {
            expense.setOrigin("MANUAL");
        } else {
            expense.setOrigin(expense.getOrigin().trim().toUpperCase());
        }

        if (expense.getReference() != null) {
            expense.setReference(expense.getReference().trim());
        }

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

        auditLogService.record(
                "FINANZAS",
                "REGISTRO DE GASTO",
                "Expense",
                savedExpense.getId(),
                "Se registró un gasto de S/ "
                        + savedExpense.getAmount()
                        + " en "
                        + category.getName()
                        + " por "
                        + savedExpense.getDescription()
                        + ". Estado de revisión: "
                        + savedExpense.getReviewStatus()
        );

        return savedExpense;
    }

    @Override
    public List<Expense> findAllExpenses() {
        return expenseRepository.findByActiveTrue();
    }

    @Override
    public List<Expense> filterExpenses(
            Integer year,
            Integer month,
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId,
            Boolean active,
            String responsible,
            String reviewStatus,
            String search
    ) {
        LocalDate[] range = resolveDateRange(year, month, startDate, endDate);

        String responsibleFilter = cleanNullable(responsible);
        String cleanReviewStatus = normalizeNullable(reviewStatus);
        String cleanSearch = cleanNullable(search);

        if (cleanReviewStatus != null) {
            cleanReviewStatus = normalizeReviewStatus(cleanReviewStatus);
        }

        return expenseRepository.filterExpenses(
                range[0],
                range[1],
                categoryId,
                active,
                responsibleFilter,
                cleanReviewStatus,
                cleanSearch
        );
    }

    @Override
    public Expense reviewExpense(
            Long id,
            String reviewStatus,
            String observation,
            String reviewedBy
    ) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado"));

        String status = normalizeReviewStatus(reviewStatus);

        expense.setReviewStatus(status);
        expense.setReviewedBy(cleanNullable(reviewedBy));
        expense.setReviewedAt(LocalDateTime.now());
        expense.setReviewObservation(cleanNullable(observation));

        if ("ANULADO".equals(status)) {
            expense.setActive(false);
        }

        Expense saved = expenseRepository.save(expense);

        String categoryName = saved.getCategory() != null
                ? saved.getCategory().getName()
                : "Sin categoría";

        notificationService.createForRole(
                "Gasto revisado",
                "El gasto de S/ " + saved.getAmount()
                        + " en " + categoryName
                        + " fue marcado como " + saved.getReviewStatus(),
                "GASTO_REVISADO",
                "ADMIN"
        );

        auditLogService.record(
                "FINANZAS",
                "REVISIÓN DE GASTO",
                "Expense",
                saved.getId(),
                "El gasto de S/ "
                        + saved.getAmount()
                        + " en "
                        + categoryName
                        + " fue marcado como "
                        + saved.getReviewStatus()
                        + ". Observación: "
                        + safe(saved.getReviewObservation())
        );

        return saved;
    }

    @Override
    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado"));

        if (Boolean.FALSE.equals(expense.getActive())) {
            throw new BusinessRuleException("El gasto ya se encuentra eliminado");
        }

        String categoryName = expense.getCategory() != null
                ? expense.getCategory().getName()
                : "Sin categoría";

        expense.setActive(false);
        expense.setReviewStatus("ANULADO");
        expense.setReviewedAt(LocalDateTime.now());
        expense.setReviewObservation("Gasto anulado mediante eliminación lógica.");

        Expense deletedExpense = expenseRepository.save(expense);

        notificationService.createForRole(
                "Gasto eliminado",
                "Se eliminó lógicamente el gasto de S/ "
                        + deletedExpense.getAmount()
                        + " en "
                        + categoryName
                        + " por "
                        + deletedExpense.getDescription(),
                "GASTO_ELIMINADO",
                "ADMIN"
        );

        auditLogService.record(
                "FINANZAS",
                "ELIMINACIÓN LÓGICA DE GASTO",
                "Expense",
                deletedExpense.getId(),
                "Se eliminó lógicamente el gasto de S/ "
                        + deletedExpense.getAmount()
                        + " en "
                        + categoryName
                        + " por "
                        + deletedExpense.getDescription()
                        + ". El registro permanece en la base de datos con estado inactivo."
        );
    }

    /*
     * RESUMEN FINANCIERO MENSUAL
     */
    @Override
    public FinanceSummaryDto getMonthlySummary(Integer year, Integer month) {
        if (year == null) {
            throw new BusinessRuleException("Debe ingresar el año");
        }

        if (month == null) {
            throw new BusinessRuleException("Debe ingresar el mes");
        }

        if (month < 1 || month > 12) {
            throw new BusinessRuleException("El mes debe estar entre 1 y 12");
        }

        if (year < 2000 || year > 2100) {
            throw new BusinessRuleException("El año ingresado no es válido");
        }

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

    /*
     * MÉTODOS AUXILIARES
     */
    private LocalDate[] resolveDateRange(
            Integer year,
            Integer month,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (month != null && year == null) {
            throw new BusinessRuleException("Debe ingresar el año para filtrar por mes");
        }

        if (month != null && (month < 1 || month > 12)) {
            throw new BusinessRuleException("El mes debe estar entre 1 y 12");
        }

        if (year != null && (year < 2000 || year > 2100)) {
            throw new BusinessRuleException("El año ingresado no es válido");
        }

        if (startDate != null || endDate != null) {
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new BusinessRuleException("La fecha de inicio no puede ser mayor que la fecha final");
            }

            return new LocalDate[]{startDate, endDate};
        }

        if (year != null && month != null) {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
            return new LocalDate[]{start, end};
        }

        if (year != null) {
            return new LocalDate[]{
                    LocalDate.of(year, 1, 1),
                    LocalDate.of(year, 12, 31)
            };
        }

        return new LocalDate[]{null, null};
    }

    private String normalizeReviewStatus(String reviewStatus) {
        if (reviewStatus == null || reviewStatus.trim().isEmpty()) {
            throw new BusinessRuleException("Debe indicar el estado de revisión");
        }

        String status = reviewStatus.trim().toUpperCase();

        return switch (status) {
            case "PENDIENTE", "REVISADO", "CONTABILIZADO", "OBSERVADO", "ANULADO" -> status;
            default -> throw new BusinessRuleException(
                    "Estado de revisión no válido. Use PENDIENTE, REVISADO, CONTABILIZADO, OBSERVADO o ANULADO"
            );
        };
    }

    private String normalizeNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim().toUpperCase();
    }

    private String cleanNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}