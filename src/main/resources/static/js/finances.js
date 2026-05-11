let incomesData = [];
let expensesData = [];
let currentIncomePage = 1;
let currentExpensePage = 1;

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
    const data = {
        name: document.getElementById("expenseCategoryName").value,
        description: document.getElementById("expenseCategoryDescription").value,
        active: true
    };

    try {
        const response = await authFetch(`${baseUrl}/finances/expense-categories`, {
            method: "POST",
            body: JSON.stringify(data)
        });
        if (!response) return;

        const result = await response.json();

        if (!response.ok) {
            document.getElementById("expenseCategoryResult").textContent =
                result.message || "Error al guardar categoría";
            return;
        }

        document.getElementById("expenseCategoryResult").textContent =
            "Categoría guardada correctamente";

        clearExpenseCategoryForm();
        loadExpenseCategories();
        loadExpenseCategoryOptions();

    } catch (error) {
        document.getElementById("expenseCategoryResult").textContent =
            "Error de conexión con el servidor";
    }
}

async function loadExpenseCategories() {
    try {
        const response = await authFetch(`${baseUrl}/finances/expense-categories`);
        if (!response) return;

        const resultBox = document.getElementById("expenseCategoryResult");

        if (!response.ok) {
            resultBox.textContent = "Error al listar categorías";
            return;
        }

        const data = await response.json();

        if (!data || data.length === 0) {
            resultBox.innerHTML = `<div class="empty-state">No hay categorías registradas.</div>`;
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
        document.getElementById("expenseCategoryResult").textContent =
            "Error al listar categorías";
        console.error(error);
    }
}

async function loadExpenseCategoryOptions() {
    try {
        const response = await authFetch(`${baseUrl}/finances/expense-categories`);
        if (!response) return;

        const data = await response.json();

        const select = document.getElementById("expenseCategoryId");
        if (!select) return;

        select.innerHTML = `<option value="">Seleccione categoría</option>`;

        data.forEach(category => {
            select.innerHTML += `
                <option value="${category.id}">
                    ${category.name}
                </option>
            `;
        });

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

        renderExpenseTable(expensesData);

        document.getElementById("expenseResult").textContent =
            "Gastos cargados correctamente";

    } catch (error) {
        document.getElementById("expenseResult").textContent =
            "Error al listar gastos";
    }
}

function renderExpenseTable(data) {
    const tbody = document.getElementById("expenseTableBody");
    tbody.innerHTML = "";

    const totalPages = Math.ceil(data.length / financeRowsPerPage) || 1;

    if (currentExpensePage > totalPages) {
        currentExpensePage = totalPages;
    }

    const start = (currentExpensePage - 1) * financeRowsPerPage;
    const end = start + financeRowsPerPage;
    const pageData = data.slice(start, end);

    pageData.forEach(expense => {
        tbody.innerHTML += `
            <tr>
                <td>${expense.id ?? ""}</td>
                <td>${expense.category ? expense.category.name : ""}</td>
                <td>${expense.description ?? ""}</td>
                <td>S/ ${Number(expense.amount || 0).toFixed(2)}</td>
                <td>${expense.date ?? ""}</td>
                <td>${expense.responsible ?? ""}</td>
                <td>
                    <span class="status-pill ${expense.active ? "active" : "inactive"}">
                        ${expense.active ? "Activo" : "Inactivo"}
                    </span>
                </td>
            </tr>
        `;
    });

    const pageInfo = document.getElementById("expensePageInfo");
    if (pageInfo) {
        pageInfo.textContent = `Página ${currentExpensePage} de ${totalPages}`;
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

    if (!year || !month) {
        document.getElementById("financeSummaryResult").textContent =
            "Ingresa año y mes";
        return;
    }

    try {
        const response = await authFetch(`${baseUrl}/finances/summary?year=${year}&month=${month}`);
        if (!response) return;

        const data = await response.json();

        document.getElementById("financeSummaryResult").textContent =
            `Total ingresos: S/ ${data.totalIncome}\n` +
            `Total gastos: S/ ${data.totalExpense}\n` +
            `Resultado: S/ ${data.profit}\n` +
            `Estado: ${data.result}`;

    } catch (error) {
        document.getElementById("financeSummaryResult").textContent =
            "Error al obtener resumen financiero";
    }
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
    await loadIncomes();
}

function closeIncomeList() {
    document.getElementById("incomeListModal").classList.add("hidden");
}

async function toggleExpenseList() {
    document.getElementById("expenseListModal").classList.remove("hidden");
    await loadExpenses();
}

function closeExpenseList() {
    document.getElementById("expenseListModal").classList.add("hidden");
}