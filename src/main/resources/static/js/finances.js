let financeMovementsData = [];
let expenseCategoryListVisible = false;
let currentFinanceMovementPage = 1;

const financeRowsPerPage = 10;

/* =========================================================
   INIT FINANZAS
========================================================= */

function initFinanceModule() {
    setDefaultFinanceDates();
    handleFinancePeriodChange();

    loadExpenseCategoryOptions();
    loadFinanceMovements();
}

function setDefaultFinanceDates() {
    const today = new Date();
    const todayText = formatDateInput(today);

    const yearInput = document.getElementById("financeYearFilter");
    const monthInput = document.getElementById("financeMonthFilter");
    const singleDateInput = document.getElementById("financeSingleDate");

    if (singleDateInput && !singleDateInput.value) {
        singleDateInput.value = todayText;
    }

    if (yearInput && !yearInput.value) {
        yearInput.value = today.getFullYear();
    }

    if (monthInput && !monthInput.value) {
        monthInput.value = today.getMonth() + 1;
    }

    const incomeDate = document.getElementById("incomeDate");
    const expenseDate = document.getElementById("expenseDate");

    if (incomeDate && !incomeDate.value) {
        incomeDate.value = todayText;
    }

    if (expenseDate && !expenseDate.value) {
        expenseDate.value = todayText;
    }
}

/* =========================================================
   MODALES
========================================================= */

function openIncomeModal() {
    const modal = document.getElementById("incomeModal");
    if (!modal) return;

    clearIncomeForm();
    setDefaultFinanceDates();
    modal.classList.remove("hidden");
}

function closeIncomeModal() {
    const modal = document.getElementById("incomeModal");
    if (!modal) return;

    modal.classList.add("hidden");
}

function openExpenseModal() {
    const modal = document.getElementById("expenseModal");
    if (!modal) return;

    clearExpenseForm();
    setDefaultFinanceDates();
    loadExpenseCategoryOptions();
    modal.classList.remove("hidden");
}

function closeExpenseModal() {
    const modal = document.getElementById("expenseModal");
    if (!modal) return;

    modal.classList.add("hidden");
}

function openExpenseCategoryModal() {
    const modal = document.getElementById("expenseCategoryModal");
    if (!modal) return;

    modal.classList.remove("hidden");
    loadExpenseCategories();
}

function closeExpenseCategoryModal() {
    const modal = document.getElementById("expenseCategoryModal");
    if (!modal) return;

    modal.classList.add("hidden");
}

function openFinanceDetailModal(type, id) {
    const movement = financeMovementsData.find(
        (item) => item.type === type && String(item.id) === String(id)
    );

    if (!movement) {
        Swal.fire("Aviso", "No se encontró el movimiento seleccionado.", "warning");
        return;
    }

    const modal = document.getElementById("financeDetailModal");
    const content = document.getElementById("financeDetailContent");

    if (!modal || !content) return;

    content.innerHTML = buildFinanceDetailHtml(movement);
    modal.classList.remove("hidden");
}

function closeFinanceDetailModal() {
    const modal = document.getElementById("financeDetailModal");
    if (!modal) return;

    modal.classList.add("hidden");
}

function openFinanceReviewModal(type, id) {
    const movement = financeMovementsData.find(
        (item) => item.type === type && String(item.id) === String(id)
    );

    if (!movement) {
        Swal.fire("Aviso", "No se encontró el movimiento seleccionado.", "warning");
        return;
    }

    document.getElementById("financeReviewType").value = type;
    document.getElementById("financeReviewId").value = id;
    document.getElementById("financeReviewStatus").value =
        movement.reviewStatus && movement.reviewStatus !== "PENDIENTE"
            ? movement.reviewStatus
            : "REVISADO";

    document.getElementById("financeReviewObservation").value =
        movement.reviewObservation || "";

    const modal = document.getElementById("financeReviewModal");
    if (!modal) return;

    modal.classList.remove("hidden");
}

function closeFinanceReviewModal() {
    const modal = document.getElementById("financeReviewModal");
    if (!modal) return;

    modal.classList.add("hidden");

    document.getElementById("financeReviewType").value = "";
    document.getElementById("financeReviewId").value = "";
    document.getElementById("financeReviewObservation").value = "";
}

/* =========================================================
   INGRESOS
========================================================= */

async function createIncome() {
    const resultBox = document.getElementById("incomeResult");

    const data = {
        description: getValue("incomeDescription"),
        amount: parseFloat(getValue("incomeAmount")),
        date: getValue("incomeDate"),
        paymentMethod: getValue("incomePaymentMethod"),
        origin: getValue("incomeOrigin") || "MANUAL",
        reference: getValue("incomeReference"),
        reviewStatus: "PENDIENTE",
        active: true,
    };

    if (!data.description) {
        showFinanceMessage(resultBox, "Ingresa la descripción del ingreso.", "error");
        return;
    }

    if (!data.amount || data.amount <= 0) {
        showFinanceMessage(resultBox, "Ingresa un monto válido mayor a cero.", "error");
        return;
    }

    if (!data.date) {
        showFinanceMessage(resultBox, "Selecciona la fecha del ingreso.", "error");
        return;
    }

    if (!data.paymentMethod) {
        showFinanceMessage(resultBox, "Selecciona el método de pago.", "error");
        return;
    }

    try {
        const response = await authFetch(`${baseUrl}/finances/incomes`, {
            method: "POST",
            body: JSON.stringify(data),
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            showFinanceMessage(
                resultBox,
                result.message || "Error al guardar ingreso.",
                "error"
            );
            return;
        }

        showFinanceMessage(resultBox, "Ingreso guardado correctamente.", "success");

        clearIncomeForm();
        closeIncomeModal();

        await loadFinanceMovements();

        if (typeof loadDashboard === "function") {
            await loadDashboard();
        }

        if (typeof loadNotifications === "function") {
            await loadNotifications();
        }

        Swal.fire({
            icon: "success",
            title: "Ingreso registrado",
            text: "El ingreso quedó pendiente de revisión.",
            timer: 1700,
            showConfirmButton: false,
        });
    } catch (error) {
        console.error(error);
        showFinanceMessage(resultBox, "Error de conexión con el servidor.", "error");
    }
}

function clearIncomeForm() {
    setValue("incomeDescription", "");
    setValue("incomeAmount", "");
    setValue("incomeDate", formatDateInput(new Date()));
    setValue("incomePaymentMethod", "EFECTIVO");
    setValue("incomeOrigin", "MANUAL");
    setValue("incomeReference", "");

    const resultBox = document.getElementById("incomeResult");
    if (resultBox) {
        resultBox.textContent = "Complete los datos del ingreso.";
    }
}

/* =========================================================
   GASTOS
========================================================= */

async function createExpense() {
    const resultBox = document.getElementById("expenseResult");

    const categoryId = getValue("expenseCategoryId");

    const data = {
        category: categoryId ? { id: parseInt(categoryId) } : null,
        description: getValue("expenseDescription"),
        amount: parseFloat(getValue("expenseAmount")),
        date: getValue("expenseDate"),
        responsible: getValue("expenseResponsible"),
        origin: getValue("expenseOrigin") || "MANUAL",
        reference: getValue("expenseReference"),
        reviewStatus: "PENDIENTE",
        active: true,
    };

    if (!data.category) {
        showFinanceMessage(resultBox, "Selecciona una categoría de gasto.", "error");
        return;
    }

    if (!data.description) {
        showFinanceMessage(resultBox, "Ingresa la descripción del gasto.", "error");
        return;
    }

    if (!data.amount || data.amount <= 0) {
        showFinanceMessage(resultBox, "Ingresa un monto válido mayor a cero.", "error");
        return;
    }

    if (!data.date) {
        showFinanceMessage(resultBox, "Selecciona la fecha del gasto.", "error");
        return;
    }

    try {
        const response = await authFetch(`${baseUrl}/finances/expenses`, {
            method: "POST",
            body: JSON.stringify(data),
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            showFinanceMessage(
                resultBox,
                result.message || "Error al guardar gasto.",
                "error"
            );
            return;
        }

        showFinanceMessage(resultBox, "Gasto guardado correctamente.", "success");

        clearExpenseForm();
        closeExpenseModal();

        await loadFinanceMovements();

        if (typeof loadDashboard === "function") {
            await loadDashboard();
        }

        if (typeof loadNotifications === "function") {
            await loadNotifications();
        }

        Swal.fire({
            icon: "success",
            title: "Gasto registrado",
            text: "El gasto quedó pendiente de revisión.",
            timer: 1700,
            showConfirmButton: false,
        });
    } catch (error) {
        console.error(error);
        showFinanceMessage(resultBox, "Error de conexión con el servidor.", "error");
    }
}

function clearExpenseForm() {
    setValue("expenseCategoryId", "");
    setValue("expenseDescription", "");
    setValue("expenseAmount", "");
    setValue("expenseDate", formatDateInput(new Date()));
    setValue("expenseResponsible", "");
    setValue("expenseOrigin", "MANUAL");
    setValue("expenseReference", "");

    const resultBox = document.getElementById("expenseResult");
    if (resultBox) {
        resultBox.textContent = "Complete los datos del gasto.";
    }
}

/* =========================================================
   CATEGORÍAS
========================================================= */

async function createExpenseCategory() {
    const resultBox = document.getElementById("expenseCategoryResult");

    const data = {
        name: getValue("expenseCategoryName"),
        description: getValue("expenseCategoryDescription"),
        active: true,
    };

    if (!data.name) {
        showFinanceMessage(resultBox, "Ingresa el nombre de la categoría.", "error");
        return;
    }

    try {
        const response = await authFetch(`${baseUrl}/finances/expense-categories`, {
            method: "POST",
            body: JSON.stringify(data),
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            showFinanceMessage(
                resultBox,
                result.message || "Error al guardar categoría.",
                "error"
            );
            return;
        }

        showFinanceMessage(resultBox, "Categoría guardada correctamente.", "success");

        clearExpenseCategoryForm();
        await loadExpenseCategoryOptions();
        await loadExpenseCategories();
    } catch (error) {
        console.error(error);
        showFinanceMessage(resultBox, "Error de conexión con el servidor.", "error");
    }
}

async function loadExpenseCategories() {
    const resultBox = document.getElementById("expenseCategoryResult");
    if (!resultBox) return;

    try {
        const response = await authFetch(`${baseUrl}/finances/expense-categories`);
        if (!response) return;

        if (!response.ok) {
            showFinanceMessage(resultBox, "Error al listar categorías.", "error");
            return;
        }

        const data = await response.json();

        if (!data || data.length === 0) {
            resultBox.innerHTML = `
                <div class="finance-inline-message">
                    No hay categorías registradas.
                </div>
            `;
            return;
        }

        let html = `<div class="category-list">`;

        data.forEach((category) => {
            html += `
                <div class="category-item">
                    <strong>${escapeHtml(category.name ?? "Sin nombre")}</strong>
                    <span>${escapeHtml(category.description ?? "Sin descripción")}</span>
                </div>
            `;
        });

        html += `</div>`;
        resultBox.innerHTML = html;
    } catch (error) {
        console.error(error);
        showFinanceMessage(resultBox, "Error al listar categorías.", "error");
    }
}

async function showExpenseCategories() {
    expenseCategoryListVisible = !expenseCategoryListVisible;

    const resultBox = document.getElementById("expenseCategoryResult");
    const button = document.getElementById("expenseCategoryToggleBtn");

    if (!resultBox) return;

    if (!expenseCategoryListVisible) {
        resultBox.innerHTML = "";
        if (button) button.textContent = "Listar categorías";
        return;
    }

    if (button) button.textContent = "Ocultar categorías";

    await loadExpenseCategories();
}

async function loadExpenseCategoryOptions() {
    try {
        const response = await authFetch(`${baseUrl}/finances/expense-categories`);
        if (!response) return;

        const data = await response.json();

        const expenseSelect = document.getElementById("expenseCategoryId");

        if (expenseSelect) {
            expenseSelect.innerHTML = `<option value="">Seleccione categoría</option>`;

            data.forEach((category) => {
                if (category.active === false) return;

                expenseSelect.innerHTML += `
                    <option value="${category.id}">
                        ${escapeHtml(category.name)}
                    </option>
                `;
            });
        }
    } catch (error) {
        console.error("Error cargando categorías:", error);
    }
}

function clearExpenseCategoryForm() {
    setValue("expenseCategoryName", "");
    setValue("expenseCategoryDescription", "");
}

/* =========================================================
   FILTROS PRINCIPALES
========================================================= */

function handleFinancePeriodChange() {
    const period = getValue("financePeriodFilter");

    const singleDate = document.getElementById("financeSingleDate");
    const year = document.getElementById("financeYearFilter");
    const month = document.getElementById("financeMonthFilter");
    const customFields = document.querySelectorAll(".finance-custom-range");

    customFields.forEach((field) => field.classList.add("hidden"));

    if (singleDate) singleDate.disabled = true;
    if (year) year.disabled = true;
    if (month) month.disabled = true;

    if (period === "day" || period === "week") {
        if (singleDate) singleDate.disabled = false;
    }

    if (period === "month") {
        if (year) year.disabled = false;
        if (month) month.disabled = false;
    }

    if (period === "custom") {
        customFields.forEach((field) => field.classList.remove("hidden"));
    }
}

function clearFinanceFilters() {
    setValue("financePeriodFilter", "month");
    setValue("financeTypeFilter", "");
    setValue("financeReviewStatusFilter", "");
    setValue("financeSearchFilter", "");

    setDefaultFinanceDates();
    handleFinancePeriodChange();
    loadFinanceMovements();
}

function buildFinanceFilterParams() {
    const period = getValue("financePeriodFilter");
    const params = new URLSearchParams();

    const reviewStatus = getValue("financeReviewStatusFilter");
    const search = getValue("financeSearchFilter");

    if (reviewStatus) {
        params.append("reviewStatus", reviewStatus);
    }

    if (search) {
        params.append("search", search);
    }

    if (period === "day") {
        const date = getValue("financeSingleDate");

        if (date) {
            params.append("startDate", date);
            params.append("endDate", date);
        }
    }

    if (period === "week") {
        const dateValue = getValue("financeSingleDate");

        if (dateValue) {
            const range = getWeekRange(dateValue);
            params.append("startDate", range.startDate);
            params.append("endDate", range.endDate);
        }
    }

    if (period === "month") {
        const year = getValue("financeYearFilter");
        const month = getValue("financeMonthFilter");

        if (year) {
            params.append("year", year);
        }

        if (month) {
            params.append("month", month);
        }
    }

if (period === "custom") {
    const startDate = getValue("financeStartDateFilter");
    const endDate = getValue("financeEndDateFilter");

    if (!startDate || !endDate) {
        Swal.fire(
            "Rango incompleto",
            "Para filtrar por rango personalizado debes seleccionar la fecha Desde y Hasta.",
            "warning"
        );

        throw new Error("Rango personalizado incompleto");
    }

    params.append("startDate", startDate);
    params.append("endDate", endDate);
}

    return params;
}

/* =========================================================
   MOVIMIENTOS FINANCIEROS
========================================================= */

async function loadFinanceMovements() {
    const resultBox = document.getElementById("financeMainResult");
    const tbody = document.getElementById("financeMovementTableBody");

    if (resultBox) {
        resultBox.textContent = "Cargando movimientos financieros...";
    }

    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="10">Cargando movimientos...</td>
            </tr>
        `;
    }

    try {
        const typeFilter = getValue("financeTypeFilter");
        const params = buildFinanceFilterParams();

        const requests = [];

        if (!typeFilter || typeFilter === "INGRESO") {
            requests.push(fetchFinanceIncomes(params));
        }

        if (!typeFilter || typeFilter === "GASTO") {
            requests.push(fetchFinanceExpenses(params));
        }

        const results = await Promise.all(requests);

        financeMovementsData = results
            .flat()
            .sort((a, b) => {
                const dateCompare = new Date(b.date) - new Date(a.date);
                if (dateCompare !== 0) return dateCompare;
                return Number(b.id || 0) - Number(a.id || 0);
            });

        currentFinanceMovementPage = 1;

        renderFinanceMovementTable();
        updateFinanceSummaryCards();

        if (resultBox) {
            resultBox.textContent =
                financeMovementsData.length > 0
                    ? "Movimientos cargados correctamente."
                    : "No se encontraron movimientos con los filtros seleccionados.";
        }
    } catch (error) {
        console.error(error);

        if (resultBox) {
            resultBox.textContent = "Error al cargar movimientos financieros.";
        }

        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="10">Error al cargar movimientos financieros.</td>
                </tr>
            `;
        }
    }
}

async function fetchFinanceIncomes(params) {
    const response = await authFetch(
        `${baseUrl}/finances/incomes/filter?${params.toString()}`
    );

    if (!response) return [];

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Error al cargar ingresos.");
    }

    return data.map((income) => ({
        id: income.id,
        type: "INGRESO",
        description: income.description,
        amount: Number(income.amount || 0),
        date: income.date,
        categoryOrMethod: income.paymentMethod || "Sin método",
        origin: income.origin || "MANUAL",
        reference: income.reference || "",
        active: income.active,
        reviewStatus: income.reviewStatus || "PENDIENTE",
        reviewedBy: income.reviewedBy || "",
        reviewedAt: income.reviewedAt || "",
        reviewObservation: income.reviewObservation || "",
        raw: income,
    }));
}

async function fetchFinanceExpenses(params) {
    const response = await authFetch(
        `${baseUrl}/finances/expenses/filter?${params.toString()}`
    );

    if (!response) return [];

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Error al cargar gastos.");
    }

    return data.map((expense) => ({
        id: expense.id,
        type: "GASTO",
        description: expense.description,
        amount: Number(expense.amount || 0),
        date: expense.date,
        categoryOrMethod: expense.category?.name || "Sin categoría",
        origin: expense.origin || "MANUAL",
        reference: expense.reference || "",
        responsible: expense.responsible || "",
        active: expense.active,
        reviewStatus: expense.reviewStatus || "PENDIENTE",
        reviewedBy: expense.reviewedBy || "",
        reviewedAt: expense.reviewedAt || "",
        reviewObservation: expense.reviewObservation || "",
        raw: expense,
    }));
}

function renderFinanceMovementTable() {
    const tbody = document.getElementById("financeMovementTableBody");
    if (!tbody) return;

    tbody.innerHTML = "";

    if (!financeMovementsData || financeMovementsData.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="10">No hay movimientos financieros para mostrar.</td>
            </tr>
        `;

        updateFinanceFooterTotals([]);
        updateFinancePagination(1);
        return;
    }

    const totalPages = Math.ceil(financeMovementsData.length / financeRowsPerPage) || 1;

    if (currentFinanceMovementPage > totalPages) {
        currentFinanceMovementPage = totalPages;
    }

    const start = (currentFinanceMovementPage - 1) * financeRowsPerPage;
    const end = start + financeRowsPerPage;
    const pageData = financeMovementsData.slice(start, end);

    updateFinanceFooterTotals(pageData);

    pageData.forEach((movement) => {
        const status = movement.reviewStatus || "PENDIENTE";
        const rowClass = getFinanceRowClass(status);
        const amountClass = movement.type === "INGRESO" ? "finance-amount-income" : "finance-amount-expense";
        const amountPrefix = movement.type === "INGRESO" ? "+" : "-";

        tbody.innerHTML += `
            <tr class="${rowClass}">
                <td>${escapeHtml(formatDisplayDate(movement.date))}</td>

                <td>
                    <span class="finance-badge ${movement.type === "INGRESO" ? "finance-type-income" : "finance-type-expense"}">
                        ${movement.type}
                    </span>
                </td>

                <td>${escapeHtml(movement.description || "")}</td>

                <td>${escapeHtml(movement.categoryOrMethod || "")}</td>

                <td>${escapeHtml(movement.origin || "")}</td>

                <td>${escapeHtml(movement.reference || "-")}</td>

                <td class="${amountClass}">
                    ${amountPrefix} S/ ${movement.amount.toFixed(2)}
                </td>

                <td>
                    ${buildReviewBadge(status)}
                </td>

                <td>${escapeHtml(movement.reviewedBy || "-")}</td>

                <td>
                    <div class="table-actions">
                        <button class="btn-table" type="button"
                            onclick="openFinanceDetailModal('${movement.type}', ${movement.id})">
                            Ver
                        </button>

                        <button class="btn-table" type="button"
                            onclick="openFinanceReviewModal('${movement.type}', ${movement.id})">
                            Revisar
                        </button>
                    </div>
                </td>
            </tr>
        `;
    });

    updateFinancePagination(totalPages);
}

function updateFinancePagination(totalPages) {
    const pageInfo = document.getElementById("financeMovementPageInfo");

    if (pageInfo) {
        pageInfo.textContent = `Página ${currentFinanceMovementPage} de ${totalPages}`;
    }
}

function updateFinanceFooterTotals(pageData = []) {
    let currentPageTotal = 0;
    let filteredTotal = 0;

    pageData.forEach((movement) => {
        if (movement.reviewStatus === "ANULADO" || movement.active === false) {
            return;
        }

        if (movement.type === "INGRESO") {
            currentPageTotal += movement.amount;
        }

        if (movement.type === "GASTO") {
            currentPageTotal -= movement.amount;
        }
    });

    financeMovementsData.forEach((movement) => {
        if (movement.reviewStatus === "ANULADO" || movement.active === false) {
            return;
        }

        if (movement.type === "INGRESO") {
            filteredTotal += movement.amount;
        }

        if (movement.type === "GASTO") {
            filteredTotal -= movement.amount;
        }
    });

    const currentPageTotalElement = document.getElementById("financeCurrentPageTotal");
    const filteredTotalElement = document.getElementById("financeFilteredTotal");

    if (currentPageTotalElement) {
        currentPageTotalElement.textContent = `S/ ${currentPageTotal.toFixed(2)}`;
        currentPageTotalElement.className =
            currentPageTotal >= 0 ? "finance-total-positive" : "finance-total-negative";
    }

    if (filteredTotalElement) {
        filteredTotalElement.textContent = `S/ ${filteredTotal.toFixed(2)}`;
        filteredTotalElement.className =
            filteredTotal >= 0 ? "finance-total-positive" : "finance-total-negative";
    }
}

function changeFinanceMovementPage(direction) {
    const totalPages = Math.ceil(financeMovementsData.length / financeRowsPerPage) || 1;

    currentFinanceMovementPage += direction;

    if (currentFinanceMovementPage < 1) {
        currentFinanceMovementPage = 1;
    }

    if (currentFinanceMovementPage > totalPages) {
        currentFinanceMovementPage = totalPages;
    }

    renderFinanceMovementTable();
}

/* =========================================================
   REVISIÓN DE MOVIMIENTOS
========================================================= */

async function submitFinanceReview() {
    const type = getValue("financeReviewType");
    const id = getValue("financeReviewId");
    const reviewStatus = getValue("financeReviewStatus");
    const observation = getValue("financeReviewObservation");

    if (!type || !id) {
        Swal.fire("Aviso", "No se encontró el movimiento a revisar.", "warning");
        return;
    }

    if (!reviewStatus) {
        Swal.fire("Aviso", "Selecciona un estado de revisión.", "warning");
        return;
    }

    const endpoint =
        type === "INGRESO"
            ? `${baseUrl}/finances/incomes/${id}/review`
            : `${baseUrl}/finances/expenses/${id}/review`;

    try {
        const response = await authFetch(endpoint, {
            method: "PUT",
            body: JSON.stringify({
                reviewStatus,
                observation,
            }),
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            Swal.fire(
                "Error",
                result.message || "No se pudo guardar la revisión.",
                "error"
            );
            return;
        }

        closeFinanceReviewModal();

        await loadFinanceMovements();

        if (typeof loadDashboard === "function") {
            await loadDashboard();
        }

        if (typeof loadNotifications === "function") {
            await loadNotifications();
        }

        Swal.fire({
            icon: "success",
            title: "Movimiento actualizado",
            text: `El movimiento fue marcado como ${reviewStatus}.`,
            timer: 1700,
            showConfirmButton: false,
        });
    } catch (error) {
        console.error(error);
        Swal.fire("Error", "Error de conexión con el servidor.", "error");
    }
}

/* =========================================================
   RESUMEN VISUAL
========================================================= */

function updateFinanceSummaryCards() {
    let totalIncome = 0;
    let totalExpense = 0;

    let pendingCount = 0;
    let reviewedCount = 0;
    let countedCount = 0;
    let observedCount = 0;
    let cancelledCount = 0;

    financeMovementsData.forEach((movement) => {
        const status = movement.reviewStatus || "PENDIENTE";

        if (status === "PENDIENTE") pendingCount++;
        if (status === "REVISADO") reviewedCount++;
        if (status === "CONTABILIZADO") countedCount++;
        if (status === "OBSERVADO") observedCount++;

        if (status === "ANULADO" || movement.active === false) {
            cancelledCount++;
            return;
        }

        if (movement.type === "INGRESO") {
            totalIncome += movement.amount;
        }

        if (movement.type === "GASTO") {
            totalExpense += movement.amount;
        }
    });

    const profit = totalIncome - totalExpense;

    setText("financePeriodIncome", `S/ ${totalIncome.toFixed(2)}`);
    setText("financePeriodExpense", `S/ ${totalExpense.toFixed(2)}`);
    setText("financePeriodProfit", `S/ ${profit.toFixed(2)}`);

    const profitStatus = document.getElementById("financePeriodProfitStatus");

    if (profitStatus) {
        profitStatus.className = "";

        if (profit > 0) {
            profitStatus.textContent = "Ganancia del filtro aplicado";
            profitStatus.classList.add("finance-profit-positive");
        } else if (profit < 0) {
            profitStatus.textContent = "Pérdida del filtro aplicado";
            profitStatus.classList.add("finance-profit-negative");
        } else {
            profitStatus.textContent = "Sin ganancia ni pérdida";
            profitStatus.classList.add("finance-profit-neutral");
        }
    }

    const compactSummary = document.getElementById("financeCompactSummary");

    if (compactSummary) {
        compactSummary.textContent =
            `${financeMovementsData.length} movimientos encontrados · ` +
            `${pendingCount} pendientes · ` +
            `${reviewedCount} revisados · ` +
            `${countedCount} contabilizados · ` +
            `${observedCount} observados · ` +
            `${cancelledCount} anulados`;
    }
}

/* =========================================================
   DETALLE
========================================================= */

function buildFinanceDetailHtml(movement) {
    const status = movement.reviewStatus || "PENDIENTE";
    const isIncome = movement.type === "INGRESO";
    const amountSign = isIncome ? "+" : "-";
    const amountClass = isIncome ? "detail-income" : "detail-expense";

    return `
        <div class="finance-detail-header-card ${amountClass}">
            <div>
                <span class="finance-detail-label">Movimiento financiero</span>

                <h2>
                    ${escapeHtml(movement.type)}
                </h2>

                <p>
                    ${escapeHtml(movement.description || "Sin descripción")}
                </p>
            </div>

            <div class="finance-detail-amount-box">
                <small>Monto</small>
                <strong>${amountSign} S/ ${movement.amount.toFixed(2)}</strong>
                ${buildReviewBadge(status)}
            </div>
        </div>

        <div class="finance-detail-grid">

            <div class="finance-detail-section">
                <h4>Información principal</h4>

                <div class="finance-detail-item">
                    <span>Fecha</span>
                    <strong>${escapeHtml(formatDisplayDate(movement.date))}</strong>
                </div>

                <div class="finance-detail-item">
                    <span>Tipo</span>
                    <strong>${escapeHtml(movement.type)}</strong>
                </div>

                <div class="finance-detail-item">
                    <span>${movement.type === "INGRESO" ? "Método de pago" : "Categoría"}</span>
                    <strong>${escapeHtml(movement.categoryOrMethod || "-")}</strong>
                </div>

                ${
                    movement.type === "GASTO"
                        ? `
                            <div class="finance-detail-item">
                                <span>Responsable</span>
                                <strong>${escapeHtml(movement.responsible || "-")}</strong>
                            </div>
                        `
                        : ""
                }
            </div>

            <div class="finance-detail-section">
                <h4>Trazabilidad</h4>

                <div class="finance-detail-item">
                    <span>Origen</span>
                    <strong>${escapeHtml(movement.origin || "-")}</strong>
                </div>

                <div class="finance-detail-item">
                    <span>Referencia</span>
                    <strong>${escapeHtml(movement.reference || "-")}</strong>
                </div>

                <div class="finance-detail-item">
                    <span>Estado del registro</span>
                    <strong>
                        ${movement.active === false ? "Inactivo" : "Activo"}
                    </strong>
                </div>
            </div>

            <div class="finance-detail-section finance-detail-section-full">
                <h4>Revisión financiera</h4>

                <div class="finance-review-timeline">

                    <div class="finance-review-step ${status === "PENDIENTE" ? "current" : "done"}">
                        <span>1</span>
                        <div>
                            <strong>Pendiente</strong>
                            <small>Movimiento registrado, aún sin revisar.</small>
                        </div>
                    </div>

                    <div class="finance-review-step ${status !== "PENDIENTE" ? "done" : ""}">
                        <span>2</span>
                        <div>
                            <strong>${escapeHtml(status)}</strong>
                            <small>
                                ${
                                    status === "PENDIENTE"
                                        ? "Esperando validación administrativa."
                                        : "Movimiento validado o actualizado por administración."
                                }
                            </small>
                        </div>
                    </div>

                </div>

                <div class="finance-detail-review-box">
                    <div>
                        <span>Revisado por</span>
                        <strong>${escapeHtml(movement.reviewedBy || "Aún no revisado")}</strong>
                    </div>

                    <div>
                        <span>Fecha de revisión</span>
                        <strong>${escapeHtml(formatDateTime(movement.reviewedAt) || "Pendiente")}</strong>
                    </div>
                </div>

                <div class="finance-detail-observation">
                    <span>Observación</span>
                    <p>${escapeHtml(movement.reviewObservation || "No se registró observación.")}</p>
                </div>
            </div>

        </div>
    `;
}

/* =========================================================
   COMPATIBILIDAD CON FUNCIONES ANTERIORES
========================================================= */

async function loadIncomes() {
    await loadFinanceMovements();
}

async function loadExpenses() {
    await loadFinanceMovements();
}

function toggleIncomeList() {
    loadFinanceMovements();
}

function closeIncomeList() {
    return;
}

function toggleExpenseList() {
    loadFinanceMovements();
}

function closeExpenseList() {
    return;
}

function filterIncomeTable() {
    return;
}

function filterExpenseTable() {
    return;
}

function changeIncomePage() {
    return;
}

function changeExpensePage() {
    return;
}

async function loadFinanceSummary() {
    await loadFinanceMovements();
}

async function viewMonthlyExpenses() {
    setValue("financePeriodFilter", "month");

    const year = getValue("monthlyExpenseYear");
    const month = getValue("monthlyExpenseMonth");

    if (year) setValue("financeYearFilter", year);
    if (month) setValue("financeMonthFilter", month);

    handleFinancePeriodChange();
    await loadFinanceMovements();
}

function filterExpensesByAdvancedFilters() {
    loadFinanceMovements();
}

function clearExpenseFilters() {
    clearFinanceFilters();
}

/* =========================================================
   HELPERS VISUALES
========================================================= */

function getFinanceRowClass(status) {
    const cleanStatus = (status || "PENDIENTE").toLowerCase();

    return `finance-row-${cleanStatus}`;
}

function buildReviewBadge(status) {
    const cleanStatus = status || "PENDIENTE";
    const className = `finance-badge-${cleanStatus.toLowerCase()}`;

    return `
        <span class="finance-badge ${className}">
            ${escapeHtml(cleanStatus)}
        </span>
    `;
}

function showFinanceMessage(element, message, type = "info") {
    if (!element) return;

    element.innerHTML = `
        <div class="finance-inline-message ${type}">
            ${escapeHtml(message)}
        </div>
    `;
}

function setText(id, value) {
    const element = document.getElementById(id);
    if (element) {
        element.textContent = value;
    }
}

function getValue(id) {
    const element = document.getElementById(id);
    return element ? element.value.trim() : "";
}

function setValue(id, value) {
    const element = document.getElementById(id);
    if (element) {
        element.value = value;
    }
}

function formatDateInput(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
}

function formatDisplayDate(dateText) {
    if (!dateText) return "-";

    const parts = dateText.split("-");
    if (parts.length !== 3) return dateText;

    return `${parts[2]}/${parts[1]}/${parts[0]}`;
}

function formatDateTime(value) {
    if (!value) return "";

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString("es-PE", {
        dateStyle: "short",
        timeStyle: "short",
    });
}

function getWeekRange(dateText) {
    const date = new Date(`${dateText}T00:00:00`);

    const day = date.getDay();
    const diffToMonday = day === 0 ? -6 : 1 - day;

    const monday = new Date(date);
    monday.setDate(date.getDate() + diffToMonday);

    const sunday = new Date(monday);
    sunday.setDate(monday.getDate() + 6);

    return {
        startDate: formatDateInput(monday),
        endDate: formatDateInput(sunday),
    };
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

/* =========================================================
   AUTO INIT
========================================================= */

document.addEventListener("DOMContentLoaded", () => {
    setTimeout(() => {
        const financeSection = document.getElementById("finances");

        if (financeSection) {
            initFinanceModule();
        }
    }, 400);
});