let incomesData = [];
let expensesData = [];
let monthlyExpensesData = [];
let currentIncomePage = 1;
let currentExpensePage = 1;
let expenseCategoryListVisible = false;
let expenseAdvancedFilterActive = false;

const financeRowsPerPage = 10;

async function createIncome() {
    const data = {
        description: document.getElementById("incomeDescription").value.trim(),
        amount: parseFloat(document.getElementById("incomeAmount").value),
        date: document.getElementById("incomeDate").value,
        paymentMethod: document.getElementById("incomePaymentMethod").value,
        active: true
    };

    try {
        const response = await authFetch(`${baseUrl}/finances/incomes`, {
            method: "POST",
            body: JSON.stringify(data)
        });
        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            document.getElementById("incomeResult").textContent =
                result.message || "Error al guardar ingreso";
            return;
        }

        document.getElementById("incomeResult").textContent =
            "Ingreso guardado correctamente";

        clearIncomeForm();
        loadIncomes();

        if (typeof loadDashboard === "function") {
            loadDashboard();
        }

        if (typeof loadNotifications === "function") {
            loadNotifications();
        }

        Swal.fire({
            icon: "success",
            title: "Ingreso registrado",
            text: "El ingreso fue guardado y notificado al administrador.",
            timer: 1800,
            showConfirmButton: false
        });

    } catch (error) {
        document.getElementById("incomeResult").textContent =
            "Error de conexión con el servidor";
    }
}

async function loadIncomes() {
    try {
        const response = await authFetch(`${baseUrl}/finances/incomes`);
        if (!response) return;

        incomesData = await response.json();
        currentIncomePage = 1;

        renderIncomeTable(incomesData);

        document.getElementById("incomeResult").textContent =
            "Ingresos cargados correctamente";

    } catch (error) {
        document.getElementById("incomeResult").textContent =
            "Error al listar ingresos";
    }
}

function renderIncomeTable(data) {
    const tbody = document.getElementById("incomeTableBody");
    tbody.innerHTML = "";

    const totalPages = Math.ceil(data.length / financeRowsPerPage) || 1;

    if (currentIncomePage > totalPages) {
        currentIncomePage = totalPages;
    }

    const start = (currentIncomePage - 1) * financeRowsPerPage;
    const end = start + financeRowsPerPage;
    const pageData = data.slice(start, end);

    pageData.forEach(income => {
        tbody.innerHTML += `
            <tr>
                <td>${income.id ?? ""}</td>
                <td>${income.description ?? ""}</td>
                <td>S/ ${Number(income.amount || 0).toFixed(2)}</td>
                <td>${income.date ?? ""}</td>
                <td>${income.paymentMethod ?? ""}</td>
                <td>
                    <span class="status-pill ${income.active ? "active" : "inactive"}">
                        ${income.active ? "Activo" : "Inactivo"}
                    </span>
                </td>
            </tr>
        `;
    });

    const pageInfo = document.getElementById("incomePageInfo");
    if (pageInfo) {
        pageInfo.textContent = `Página ${currentIncomePage} de ${totalPages}`;
    }
}

function clearIncomeForm() {
    document.getElementById("incomeDescription").value = "";
    document.getElementById("incomeAmount").value = "";
    document.getElementById("incomeDate").value = "";
    document.getElementById("incomePaymentMethod").value = "EFECTIVO";
}

async function createExpenseCategory() {
    const resultBox = document.getElementById("expenseCategoryResult");

    const data = {
        name: document.getElementById("expenseCategoryName").value.trim(),
        description: document.getElementById("expenseCategoryDescription").value.trim(),
        active: true
    };

    if (!data.name) {
        resultBox.style.display = "block";
        resultBox.innerHTML = `
            <div class="finance-inline-message error">
                Ingresa el nombre de la categoría.
            </div>
        `;
        return;
    }

    try {
        const response = await authFetch(`${baseUrl}/finances/expense-categories`, {
            method: "POST",
            body: JSON.stringify(data)
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            resultBox.style.display = "block";
            resultBox.innerHTML = `
                <div class="finance-inline-message error">
                    ${result.message || "Error al guardar categoría."}
                </div>
            `;
            return;
        }

        resultBox.style.display = "block";
        resultBox.innerHTML = `
            <div class="finance-inline-message success">
                Categoría guardada correctamente.
            </div>
        `;

        clearExpenseCategoryForm();

        await loadExpenseCategoryOptions();

        if (expenseCategoryListVisible) {
            await loadExpenseCategories();
        }

    } catch (error) {
        resultBox.style.display = "block";
        resultBox.innerHTML = `
            <div class="finance-inline-message error">
                Error de conexión con el servidor.
            </div>
        `;
    }
}

async function loadExpenseCategories() {
    const resultBox = document.getElementById("expenseCategoryResult");

    if (!resultBox) return;

    if (!expenseCategoryListVisible) {
        resultBox.style.display = "none";
        resultBox.innerHTML = "";
        return;
    }

    try {
        resultBox.style.display = "block";

        const response = await authFetch(`${baseUrl}/finances/expense-categories`);
        if (!response) return;

        if (!response.ok) {
            resultBox.innerHTML = `
                <div class="finance-inline-message error">
                    Error al listar categorías.
                </div>
            `;
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

        data.forEach(category => {
            html += `
                <div class="category-item">
                    <strong>${category.name ?? "Sin nombre"}</strong>
                    <span>${category.description ?? "Sin descripción"}</span>
                </div>
            `;
        });

        html += `</div>`;

        resultBox.innerHTML = html;

    } catch (error) {
        resultBox.innerHTML = `
            <div class="finance-inline-message error">
                Error al listar categorías.
            </div>
        `;
        console.error(error);
    }
}

async function showExpenseCategories() {
    const resultBox = document.getElementById("expenseCategoryResult");
    const button = document.getElementById("expenseCategoryToggleBtn");

    if (!resultBox) return;

    expenseCategoryListVisible = !expenseCategoryListVisible;

    if (!expenseCategoryListVisible) {
        resultBox.style.display = "none";
        resultBox.innerHTML = "";

        if (button) {
            button.textContent = "Listar categorías";
        }

        return;
    }

    if (button) {
        button.textContent = "Ocultar categorías";
    }

    await loadExpenseCategories();
}

async function loadExpenseCategoryOptions() {
    try {
        const response = await authFetch(`${baseUrl}/finances/expense-categories`);
        if (!response) return;

        const data = await response.json();

        const expenseSelect = document.getElementById("expenseCategoryId");
        const filterSelect = document.getElementById("expenseFilterCategoryId");

        if (expenseSelect) {
            expenseSelect.innerHTML = `<option value="">Seleccione categoría</option>`;

            data.forEach(category => {
                if (category.active === false) return;

                expenseSelect.innerHTML += `
                    <option value="${category.id}">
                        ${category.name}
                    </option>
                `;
            });
        }

        if (filterSelect) {
            filterSelect.innerHTML = `<option value="">Todas las categorías</option>`;

            data.forEach(category => {
                filterSelect.innerHTML += `
                    <option value="${category.id}">
                        ${category.name}
                    </option>
                `;
            });
        }

    } catch (error) {
        console.error("Error cargando categorías:", error);
    }
}

function clearExpenseCategoryForm() {
    document.getElementById("expenseCategoryName").value = "";
    document.getElementById("expenseCategoryDescription").value = "";
}

async function createExpense() {
    const categoryId = document.getElementById("expenseCategoryId").value;

    const data = {
        category: categoryId ? { id: parseInt(categoryId) } : null,
        description: document.getElementById("expenseDescription").value.trim(),
        amount: parseFloat(document.getElementById("expenseAmount").value),
        date: document.getElementById("expenseDate").value,
        responsible: document.getElementById("expenseResponsible").value.trim(),
        active: true
    };

    try {
        const response = await authFetch(`${baseUrl}/finances/expenses`, {
            method: "POST",
            body: JSON.stringify(data)
        });

        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            document.getElementById("expenseResult").textContent =
                result.message || "Error al guardar gasto";
            return;
        }

        document.getElementById("expenseResult").textContent =
            "Gasto guardado correctamente";

        clearExpenseForm();
        await loadExpenses();

        if (typeof loadDashboard === "function") {
            await loadDashboard();
        }

        if (typeof loadNotifications === "function") {
            await loadNotifications();
        }

        Swal.fire({
            icon: "success",
            title: "Gasto registrado",
            text: "El gasto fue guardado y notificado al administrador.",
            timer: 1800,
            showConfirmButton: false
        });

    } catch (error) {
        document.getElementById("expenseResult").textContent =
            "Error de conexión con el servidor";
    }
}

async function loadExpenses() {
    try {
        const response = await authFetch(`${baseUrl}/finances/expenses`);
        if (!response) return;

        expensesData = await response.json();
        currentExpensePage = 1;
        expenseAdvancedFilterActive = false;

        renderExpenseTable(getFilteredExpenses());
        renderExpenseFilterSummary(
            expensesData,
            "Listado general de gastos activos"
        );

        document.getElementById("expenseResult").textContent =
            "Gastos cargados correctamente";

    } catch (error) {
        document.getElementById("expenseResult").textContent =
            "Error al listar gastos";
    }
}

async function filterExpensesByAdvancedFilters(showMessage = true) {
    const year = document.getElementById("expenseFilterYear")?.value;
    const month = document.getElementById("expenseFilterMonth")?.value;
    const categoryId = document.getElementById("expenseFilterCategoryId")?.value;
    const status = document.getElementById("expenseFilterStatus")?.value;
    const responsible = document.getElementById("expenseFilterResponsible")?.value.trim();

    const params = new URLSearchParams();

    if (year) params.append("year", year);
    if (month) params.append("month", month);
    if (categoryId) params.append("categoryId", categoryId);
    if (status) params.append("active", status);
    if (responsible) params.append("responsible", responsible);

    try {
        const url = `${baseUrl}/finances/expenses/filter?${params.toString()}`;

        const response = await authFetch(url);
        if (!response) return;

        const data = await response.json();

        if (!response.ok) {
            Swal.fire(
                "Error",
                data.message || "No se pudieron filtrar los gastos.",
                "error"
            );
            return;
        }

        expensesData = data;
        currentExpensePage = 1;
        expenseAdvancedFilterActive = true;

        renderExpenseTable(getFilteredExpenses());

        renderExpenseFilterSummary(
            expensesData,
            buildExpenseFilterTitle(year, month, categoryId, status, responsible)
        );

        if (showMessage) {
            Swal.fire({
                icon: "success",
                title: "Filtro aplicado",
                text: "Los gastos fueron filtrados correctamente.",
                timer: 1400,
                showConfirmButton: false
            });
        }

    } catch (error) {
        console.error("Error filtrando gastos:", error);

        Swal.fire(
            "Error",
            "Error de conexión al filtrar gastos.",
            "error"
        );
    }
}

async function clearExpenseFilters() {
    const yearInput = document.getElementById("expenseFilterYear");
    const monthInput = document.getElementById("expenseFilterMonth");
    const categoryInput = document.getElementById("expenseFilterCategoryId");
    const statusInput = document.getElementById("expenseFilterStatus");
    const responsibleInput = document.getElementById("expenseFilterResponsible");
    const searchInput = document.getElementById("expenseSearchInput");

    if (yearInput) yearInput.value = "";
    if (monthInput) monthInput.value = "";
    if (categoryInput) categoryInput.value = "";
    if (statusInput) statusInput.value = "true";
    if (responsibleInput) responsibleInput.value = "";
    if (searchInput) searchInput.value = "";

    expenseAdvancedFilterActive = false;

    await loadExpenses();
}

function buildExpenseFilterTitle(year, month, categoryId, status, responsible) {
    const parts = [];

    if (year && month) {
        parts.push(`${getFinanceMonthName(month)} ${year}`);
    } else if (year) {
        parts.push(`Año ${year}`);
    } else {
        parts.push("Todos los periodos");
    }

    const categorySelect = document.getElementById("expenseFilterCategoryId");
    const categoryName = categorySelect && categorySelect.value
        ? categorySelect.options[categorySelect.selectedIndex].textContent.trim()
        : "";

    if (categoryName) {
        parts.push(`Categoría: ${categoryName}`);
    }

    if (status === "true") {
        parts.push("Estado: Activos");
    } else if (status === "false") {
        parts.push("Estado: Eliminados/Inactivos");
    } else {
        parts.push("Estado: Todos");
    }

    if (responsible) {
        parts.push(`Responsable: ${responsible}`);
    }

    return parts.join(" | ");
}

function renderExpenseFilterSummary(data, title) {
    const container = document.getElementById("expenseFilterSummary");

    if (!container) return;

    const total = data.reduce((sum, expense) => {
        return sum + Number(expense.amount || 0);
    }, 0);

    const count = data.length;

    const categoryTotals = {};

    data.forEach(expense => {
        const categoryName = expense.category?.name || "Sin categoría";

        if (!categoryTotals[categoryName]) {
            categoryTotals[categoryName] = 0;
        }

        categoryTotals[categoryName] += Number(expense.amount || 0);
    });

    const categoryHtml = Object.entries(categoryTotals)
        .map(([category, amount]) => `
            <div class="expense-filter-category-row">
                <span>${escapeFinanceHtml(category)}</span>
                <strong>S/ ${formatFinanceMoney(amount)}</strong>
            </div>
        `)
        .join("");

    container.classList.remove("hidden");

    container.innerHTML = `
        <div class="expense-filter-summary-card">
            <div class="expense-filter-summary-header">
                <div>
                    <strong>${escapeFinanceHtml(title || "Resultado de gastos")}</strong>
                    <span>${count} gasto(s) encontrado(s)</span>
                </div>

                <div class="expense-filter-total">
                    <small>Total filtrado</small>
                    <strong>S/ ${formatFinanceMoney(total)}</strong>
                </div>
            </div>

            <div class="expense-filter-category-list">
                ${categoryHtml || `
                    <div class="expense-filter-category-row">
                        <span>No hay datos para mostrar</span>
                        <strong>S/ 0.00</strong>
                    </div>
                `}
            </div>
        </div>
    `;
}

function renderExpenseTable(data) {
    const tbody = document.getElementById("expenseTableBody");

    if (!tbody) return;

    tbody.innerHTML = "";

    const totalPages = Math.ceil(data.length / financeRowsPerPage) || 1;

    if (currentExpensePage > totalPages) {
        currentExpensePage = totalPages;
    }

    const start = (currentExpensePage - 1) * financeRowsPerPage;
    const end = start + financeRowsPerPage;
    const pageData = data.slice(start, end);

    if (!pageData || pageData.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="empty-table-message">
                    No hay gastos registrados.
                </td>
            </tr>
        `;

        const pageInfo = document.getElementById("expensePageInfo");

        if (pageInfo) {
            pageInfo.textContent = `Página 1 de 1`;
        }

        return;
    }

    pageData.forEach(expense => {
        const categoryName = expense.category ? expense.category.name : "";
        const description = expense.description ?? "";
        const responsible = expense.responsible ?? "";

        tbody.innerHTML += `
            <tr>
                <td>${expense.id ?? ""}</td>
                <td>${escapeFinanceHtml(categoryName)}</td>
                <td>${escapeFinanceHtml(description)}</td>
                <td>S/ ${Number(expense.amount || 0).toFixed(2)}</td>
                <td>${expense.date ?? ""}</td>
                <td>${escapeFinanceHtml(responsible)}</td>
                <td>
                    <span class="status-pill ${expense.active ? "active" : "inactive"}">
                        ${expense.active ? "Activo" : "Inactivo"}
                    </span>
                </td>
                <td>
                    ${
                        expense.active
                            ? `
                                <button 
                                    type="button"
                                    class="table-action-btn danger expense-delete-btn"
                                    onclick="deleteExpense(${expense.id})">
                                    Eliminar
                                </button>
                            `
                            : `
                                <span class="expense-deleted-label">
                                    Eliminado
                                </span>
                            `
                    }
                </td>
            </tr>
        `;
    });

    const pageInfo = document.getElementById("expensePageInfo");

    if (pageInfo) {
        pageInfo.textContent = `Página ${currentExpensePage} de ${totalPages}`;
    }
}

async function deleteExpense(id) {
    if (!id) return;

    const expense =
        expensesData.find(item => Number(item.id) === Number(id)) ||
        monthlyExpensesData.find(item => Number(item.id) === Number(id));

    const description = expense?.description || "Gasto seleccionado";

    const confirm = await Swal.fire({
        icon: "warning",
        title: "¿Eliminar gasto?",
        html: `
            <p>Se eliminará el gasto:</p>
            <strong>${escapeFinanceHtml(description)}</strong>
            <br><br>
            <small>
                El registro no se borrará de la base de datos. 
                Solo quedará inactivo para mantener auditoría.
            </small>
        `,
        showCancelButton: true,
        confirmButtonText: "Sí, eliminar",
        cancelButtonText: "Cancelar",
        confirmButtonColor: "#dc2626"
    });

    if (!confirm.isConfirmed) return;

    try {
        const response = await authFetch(`${baseUrl}/finances/expenses/${id}`, {
            method: "DELETE"
        });

        if (!response) return;

        let result = {};

        try {
            result = await response.json();
        } catch (error) {
            result = {};
        }

        if (!response.ok) {
            Swal.fire(
                "Error",
                result.message || "No se pudo eliminar el gasto.",
                "error"
            );
            return;
        }

        Swal.fire({
            icon: "success",
            title: "Gasto eliminado",
            text: "El gasto fue desactivado correctamente.",
            timer: 1700,
            showConfirmButton: false
        });

        if (expenseAdvancedFilterActive) {
            await filterExpensesByAdvancedFilters(false);
        } else {
            await loadExpenses();
        }

        const monthlyYear = document.getElementById("monthlyExpenseYear")?.value;
        const monthlyMonth = document.getElementById("monthlyExpenseMonth")?.value;
        const monthlyResult = document.getElementById("monthlyExpenseResult");

        if (monthlyYear && monthlyMonth && monthlyResult && monthlyResult.innerHTML.trim() !== "") {
            await viewMonthlyExpenses(false);
        }

        if (typeof loadDashboard === "function") {
            await loadDashboard();
        }

        const year = document.getElementById("summaryYear")?.value;
        const month = document.getElementById("summaryMonth")?.value;

        if (year && month && typeof loadFinanceSummary === "function") {
            await loadFinanceSummary();
        }

        if (typeof loadNotifications === "function") {
            await loadNotifications();
        }

    } catch (error) {
        console.error("Error eliminando gasto:", error);

        Swal.fire(
            "Error",
            "Error de conexión con el servidor.",
            "error"
        );
    }
}

function clearExpenseForm() {
    document.getElementById("expenseCategoryId").value = "";
    document.getElementById("expenseDescription").value = "";
    document.getElementById("expenseAmount").value = "";
    document.getElementById("expenseDate").value = "";
    document.getElementById("expenseResponsible").value = "";
}

async function loadFinanceSummary() {
    const year = document.getElementById("summaryYear").value;
    const month = document.getElementById("summaryMonth").value;
    const resultBox = document.getElementById("financeSummaryResult");

    if (!year || !month) {
        resultBox.innerHTML = `
            <div class="finance-summary-empty">
                Ingresa el año y mes para consultar el resumen financiero.
            </div>
        `;
        return;
    }

    try {
        resultBox.innerHTML = `
            <div class="finance-summary-loading">
                Calculando resumen financiero...
            </div>
        `;

        const response = await authFetch(`${baseUrl}/finances/summary?year=${year}&month=${month}`);

        if (!response) return;

        const data = await response.json();

        if (!response.ok) {
            resultBox.innerHTML = `
                <div class="finance-summary-empty">
                    ${data.message || "No se pudo obtener el resumen financiero."}
                </div>
            `;
            return;
        }

        renderFinancialSummary(data, year, month);

    } catch (error) {
        resultBox.innerHTML = `
            <div class="finance-summary-empty error">
                Error al obtener resumen financiero.
            </div>
        `;
    }
}

function renderFinancialSummary(data, year, month) {
    const resultBox = document.getElementById("financeSummaryResult");

    const totalIncome = Number(data.totalIncome || 0);
    const totalExpense = Number(data.totalExpense || 0);
    const profit = Number(data.profit || 0);

    const status = data.result || (profit >= 0 ? "GANANCIA" : "PÉRDIDA");
    const statusClass = profit >= 0 ? "gain" : "loss";

    const monthName = getFinanceMonthName(month);

    const percentageExpense = totalIncome > 0
        ? ((totalExpense / totalIncome) * 100).toFixed(1)
        : "0.0";

    const percentageProfit = totalIncome > 0
        ? ((profit / totalIncome) * 100).toFixed(1)
        : "0.0";

    const analysisMessage = profit >= 0
        ? `Durante ${monthName} de ${year}, el centro obtuvo un resultado positivo. Los ingresos superaron a los gastos registrados, generando una ganancia de S/ ${formatFinanceMoney(profit)}.`
        : `Durante ${monthName} de ${year}, el centro presenta un resultado negativo. Se recomienda revisar los gastos y reforzar el registro de ingresos.`;

    resultBox.innerHTML = `
        <div class="finance-summary-container">

            <div class="finance-summary-title">
                <div>
                    <h3>Resumen financiero de ${monthName} ${year}</h3>
                    <p>Comparación mensual de ingresos, gastos y resultado económico.</p>
                </div>

                <span class="finance-summary-badge ${statusClass}">
                    ${status}
                </span>
            </div>

            <div class="finance-summary-grid">

                <div class="finance-summary-card income">
                    <span>Total ingresos</span>
                    <strong>S/ ${formatFinanceMoney(totalIncome)}</strong>
                    <small>Dinero registrado como entrada</small>
                </div>

                <div class="finance-summary-card expense">
                    <span>Total gastos</span>
                    <strong>S/ ${formatFinanceMoney(totalExpense)}</strong>
                    <small>Dinero registrado como salida</small>
                </div>

                <div class="finance-summary-card result ${statusClass}">
                    <span>Resultado</span>
                    <strong>S/ ${formatFinanceMoney(profit)}</strong>
                    <small>Ingresos menos gastos</small>
                </div>

                <div class="finance-summary-card status ${statusClass}">
                    <span>Estado financiero</span>
                    <strong>${status}</strong>
                    <small>Balance del periodo consultado</small>
                </div>

            </div>

            <div class="finance-summary-analysis">
                <strong>Análisis del periodo</strong>
                <p>${analysisMessage}</p>
            </div>

            <div class="finance-summary-bars">

                <div class="finance-bar-item">
                    <div class="finance-bar-label">
                        <span>Gastos sobre ingresos</span>
                        <strong>${percentageExpense}%</strong>
                    </div>
                    <div class="finance-bar-track">
                        <div class="finance-bar-fill expense" style="width:${Math.min(percentageExpense, 100)}%;"></div>
                    </div>
                </div>

                <div class="finance-bar-item">
                    <div class="finance-bar-label">
                        <span>Rentabilidad aproximada</span>
                        <strong>${percentageProfit}%</strong>
                    </div>
                    <div class="finance-bar-track">
                        <div class="finance-bar-fill income" style="width:${Math.max(Math.min(percentageProfit, 100), 0)}%;"></div>
                    </div>
                </div>

            </div>

        </div>
    `;
}

function formatFinanceMoney(value) {
    return Number(value || 0).toLocaleString("es-PE", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function getFinanceMonthName(month) {
    const months = {
        1: "enero",
        2: "febrero",
        3: "marzo",
        4: "abril",
        5: "mayo",
        6: "junio",
        7: "julio",
        8: "agosto",
        9: "septiembre",
        10: "octubre",
        11: "noviembre",
        12: "diciembre"
    };

    return months[Number(month)] || `mes ${month}`;
}

function getFilteredIncomes() {
    const input = document.querySelector("#incomeListModal .table-search");
    const search = input ? input.value.toLowerCase() : "";

    if (!search) return incomesData;

    return incomesData.filter(income => {
        const text = `
            ${income.id ?? ""}
            ${income.description ?? ""}
            ${income.amount ?? ""}
            ${income.date ?? ""}
            ${income.paymentMethod ?? ""}
            ${income.active ? "Activo" : "Inactivo"}
        `.toLowerCase();

        return text.includes(search);
    });
}

function getFilteredExpenses() {
    const input = document.querySelector("#expenseListModal .table-search");
    const search = input ? input.value.toLowerCase() : "";

    if (!search) return expensesData;

    return expensesData.filter(expense => {
        const text = `
            ${expense.id ?? ""}
            ${expense.category ? expense.category.name : ""}
            ${expense.description ?? ""}
            ${expense.amount ?? ""}
            ${expense.date ?? ""}
            ${expense.responsible ?? ""}
            ${expense.active ? "Activo" : "Inactivo"}
        `.toLowerCase();

        return text.includes(search);
    });
}

function filterIncomeTable() {
    currentIncomePage = 1;
    renderIncomeTable(getFilteredIncomes());
}

function filterExpenseTable() {
    currentExpensePage = 1;
    renderExpenseTable(getFilteredExpenses());
}

function changeIncomePage(direction) {
    const filteredData = getFilteredIncomes();
    const totalPages = Math.ceil(filteredData.length / financeRowsPerPage) || 1;

    currentIncomePage += direction;

    if (currentIncomePage < 1) currentIncomePage = 1;
    if (currentIncomePage > totalPages) currentIncomePage = totalPages;

    renderIncomeTable(filteredData);
}

function changeExpensePage(direction) {
    const filteredData = getFilteredExpenses();
    const totalPages = Math.ceil(filteredData.length / financeRowsPerPage) || 1;

    currentExpensePage += direction;

    if (currentExpensePage < 1) currentExpensePage = 1;
    if (currentExpensePage > totalPages) currentExpensePage = totalPages;

    renderExpenseTable(filteredData);
}

async function toggleIncomeList() {
    document.getElementById("incomeListModal").classList.remove("hidden");
    document.body.classList.add("modal-open");

    await loadIncomes();
}

function closeIncomeList() {
    document.getElementById("incomeListModal").classList.add("hidden");
    document.body.classList.remove("modal-open");
}

function closeIncomeList() {
    document.getElementById("incomeListModal").classList.add("hidden");
}

async function toggleExpenseList() {
    document.getElementById("expenseListModal").classList.remove("hidden");
    document.body.classList.add("modal-open");

    await loadExpenseCategoryOptions();
    await loadExpenseResponsibleOptions();

    const statusInput = document.getElementById("expenseFilterStatus");

    if (statusInput && !statusInput.value) {
        statusInput.value = "true";
    }

    await loadExpenses();
}

function closeExpenseList() {
    document.getElementById("expenseListModal").classList.add("hidden");
    document.body.classList.remove("modal-open");
}

async function refreshFinancesRealtime() {
    if (currentUser.role !== "ADMIN") return;

    if (typeof loadIncomes === "function") {
        await loadIncomes();
    }

    if (typeof loadExpenses === "function") {
        await loadExpenses();
    }

    if (typeof loadDashboard === "function") {
        await loadDashboard();
    }

    const year = document.getElementById("summaryYear")?.value;
    const month = document.getElementById("summaryMonth")?.value;

    if (year && month && typeof loadFinanceSummary === "function") {
        await loadFinanceSummary();
    }
}

function escapeFinanceHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function escapeFinanceAttr(value) {
    return escapeFinanceHtml(value);
}

async function viewMonthlyExpenses(showMessage = true) {
    const year = document.getElementById("monthlyExpenseYear")?.value;
    const month = document.getElementById("monthlyExpenseMonth")?.value;
    const resultBox = document.getElementById("monthlyExpenseResult");

    if (!resultBox) return;

    if (!year || !month) {
        resultBox.innerHTML = `
            <div class="monthly-expense-empty">
                Selecciona el año y el mes para consultar los gastos.
            </div>
        `;
        return;
    }

    try {
        resultBox.innerHTML = `
            <div class="monthly-expense-loading">
                Cargando historial mensual de gastos...
            </div>
        `;

        const params = new URLSearchParams();
        params.append("year", year);
        params.append("month", month);
        params.append("active", "true");

        const response = await authFetch(`${baseUrl}/finances/expenses/filter?${params.toString()}`);

        if (!response) return;

        const data = await response.json();

        if (!response.ok) {
            resultBox.innerHTML = `
                <div class="monthly-expense-empty error">
                    ${data.message || "No se pudo cargar el historial mensual."}
                </div>
            `;
            return;
        }

        renderMonthlyExpenseHistory(data, year, month);

        if (showMessage) {
            Swal.fire({
                icon: "success",
                title: "Historial cargado",
                text: "Los gastos del mes fueron consultados correctamente.",
                timer: 1400,
                showConfirmButton: false
            });
        }

    } catch (error) {
        console.error("Error cargando historial mensual:", error);

        resultBox.innerHTML = `
            <div class="monthly-expense-empty error">
                Error de conexión al cargar los gastos del mes.
            </div>
        `;
    }
}

function renderMonthlyExpenseHistory(data, year, month) {
    const resultBox = document.getElementById("monthlyExpenseResult");

    if (!resultBox) return;

    const monthName = getFinanceMonthName(month);
    const expenses = Array.isArray(data) ? data : [];
    monthlyExpensesData = expenses;

    const total = expenses.reduce((sum, expense) => {
        return sum + Number(expense.amount || 0);
    }, 0);

    const categoryTotals = {};

    expenses.forEach(expense => {
        const categoryName = expense.category?.name || "Sin categoría";

        if (!categoryTotals[categoryName]) {
            categoryTotals[categoryName] = 0;
        }

        categoryTotals[categoryName] += Number(expense.amount || 0);
    });

    const categoryEntries = Object.entries(categoryTotals)
        .sort((a, b) => b[1] - a[1]);

    const highestCategory = categoryEntries.length > 0
        ? categoryEntries[0][0]
        : "Sin datos";

    const categoryHtml = categoryEntries.map(([category, amount]) => {
        const percent = total > 0 ? ((amount / total) * 100).toFixed(1) : "0.0";

        return `
            <div class="monthly-category-item">
                <div>
                    <strong>${escapeFinanceHtml(category)}</strong>
                    <span>${percent}% del total mensual</span>
                </div>

                <strong>S/ ${formatFinanceMoney(amount)}</strong>
            </div>
        `;
    }).join("");

    const sortedExpenses = [...expenses].sort((a, b) => {
        const dateCompare = String(b.date || "").localeCompare(String(a.date || ""));

        if (dateCompare !== 0) return dateCompare;

        return Number(b.id || 0) - Number(a.id || 0);
    });

    const detailRows = sortedExpenses.map(expense => `
        <tr>
            <td>${expense.date ?? ""}</td>
            <td>${escapeFinanceHtml(expense.category?.name || "Sin categoría")}</td>
            <td>${escapeFinanceHtml(expense.description || "")}</td>
            <td>${escapeFinanceHtml(expense.responsible || "No registrado")}</td>
            <td>S/ ${formatFinanceMoney(expense.amount || 0)}</td>
            <td>
            <button 
                type="button"
                class="table-action-btn danger expense-delete-btn"
                onclick="deleteExpense(${expense.id})">
                Eliminar
            </button>
            </td>
        </tr>
    `).join("");

    resultBox.innerHTML = `
        <div class="monthly-expense-card">

            <div class="monthly-expense-title">
                <div>
                    <span>Historial mensual de gastos</span>
                    <h3>${capitalizeFinanceText(monthName)} ${year}</h3>
                </div>

                <div class="monthly-expense-total">
                    <small>Total gastos del mes</small>
                    <strong>S/ ${formatFinanceMoney(total)}</strong>
                </div>
            </div>

            <div class="monthly-expense-kpis">
                <div>
                    <span>Cantidad de gastos</span>
                    <strong>${expenses.length}</strong>
                    <small>Registros activos encontrados</small>
                </div>

                <div>
                    <span>Categoría principal</span>
                    <strong>${escapeFinanceHtml(highestCategory)}</strong>
                    <small>Mayor concentración de gasto</small>
                </div>

                <div>
                    <span>Promedio por gasto</span>
                    <strong>S/ ${formatFinanceMoney(expenses.length > 0 ? total / expenses.length : 0)}</strong>
                    <small>Total dividido entre registros</small>
                </div>
            </div>

            <div class="monthly-expense-section">
                <h4>Gastos por categoría</h4>

                <div class="monthly-category-list">
                    ${categoryHtml || `
                        <div class="monthly-expense-empty">
                            No hay gastos registrados en este mes.
                        </div>
                    `}
                </div>
            </div>

            <div class="monthly-expense-section">
                <h4>Detalle de gastos</h4>

                <div class="table-wrapper">
                    <table>
                        <thead>
                            <tr>
                                <th>Fecha</th>
                                <th>Categoría</th>
                                <th>Descripción</th>
                                <th>Responsable</th>
                                <th>Monto</th>
                                <th>Acción</th>
                            </tr>
                        </thead>

                        <tbody>
                            ${detailRows || `
                                <tr>
                                    <td colspan="6" class="empty-table-message">
                                        No hay gastos registrados para este mes.
                                    </td>
                                </tr>
                            `}
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    `;
}

function capitalizeFinanceText(value) {
    const text = String(value || "");

    if (!text) return "";

    return text.charAt(0).toUpperCase() + text.slice(1);
}

async function loadExpenseResponsibleOptions() {
    const select = document.getElementById("expenseFilterResponsible");

    if (!select) return;

    const selectedValue = select.value;

    try {
        const response = await authFetch(`${baseUrl}/finances/expenses/filter`);

        if (!response) return;

        const data = await response.json();

        if (!response.ok) {
            renderExpenseResponsibleOptions(expensesData, selectedValue);
            return;
        }

        renderExpenseResponsibleOptions(data, selectedValue);

    } catch (error) {
        console.error("Error cargando responsables:", error);
        renderExpenseResponsibleOptions(expensesData, selectedValue);
    }
}

function renderExpenseResponsibleOptions(data, selectedValue = "") {
    const select = document.getElementById("expenseFilterResponsible");

    if (!select) return;

    const responsibles = [...new Set(
        (Array.isArray(data) ? data : [])
            .map(expense => expense.responsible ? expense.responsible.trim() : "")
            .filter(value => value !== "")
    )].sort((a, b) => a.localeCompare(b));

    select.innerHTML = `<option value="">Todos los responsables</option>`;

    responsibles.forEach(responsible => {
        select.innerHTML += `
            <option value="${escapeFinanceAttr(responsible)}">
                ${escapeFinanceHtml(responsible)}
            </option>
        `;
    });

    if (selectedValue && responsibles.includes(selectedValue)) {
        select.value = selectedValue;
    }
}