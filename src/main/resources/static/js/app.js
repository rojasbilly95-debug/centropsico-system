const baseUrl = "/api";

/* =========================
   INIT PRINCIPAL
========================= */

window.onload = async () => {
    if (!validateSession()) return;

    await safeRun(loadSidebar);

    if (typeof refreshSidebarUser === "function") {
        await safeRun(refreshSidebarUser);
    }

    if (typeof initProfileSidebarClick === "function") {
        initProfileSidebarClick();
    }

    applyRoleVisibility();

    await safeRun(loadDashboard);

    // ADMIN y RECEPCIONISTA
    if (currentUser.role === "ADMIN" || currentUser.role === "RECEPCIONISTA") {
        await safeRun(loadPatients);
        await safeRun(loadPatientOptions);
        await safeRun(loadPsychologistOptions);
        await safeRun(loadServiceOptions);
        await safeRun(loadLeads);
    }

    // SOLO ADMIN
    if (currentUser.role === "ADMIN") {
        await safeRun(loadUsers);

        await safeRun(loadPsychologists);
        await safeRun(loadAvailabilityPsychologistOptions);

        await safeRun(loadServices);

        await safeRun(loadIncomes);
        await safeRun(loadExpenseCategories);
        await safeRun(loadExpenseCategoryOptions);
        await safeRun(loadExpenses);

        // Responsables para filtro de gastos
        await safeRun(loadExpenseResponsibleOptions);

        // Promociones del portal
        await safeRun(loadPromotions);

        // Reporte por psicólogo
        await safeRun(loadReportPsychologistOptions);
    }

    // TODOS LOS ROLES
    await safeRun(loadAppointments);

    if (typeof initAppointmentAvailabilityEvents === "function") {
        initAppointmentAvailabilityEvents();
    }

    // PSICÓLOGO
    if (currentUser.role === "PSICOLOGO") {
        await safeRun(loadPsychologistOptions);
        await safeRun(loadServiceOptions);
    }

    // NOTIFICACIONES
    await safeRun(loadNotifications);

    if (typeof connectNotificationWebSocket === "function") {
        connectNotificationWebSocket();
    }

    // RECORDATORIOS INTERNOS
    await safeRun(loadReminders);

    // Refrescar íconos Lucide después de cargar sidebar y módulos
    if (typeof lucide !== "undefined") {
        lucide.createIcons();
    }
};

/* =========================
   EJECUCIÓN SEGURA
========================= */

async function safeRun(fn) {
    if (typeof fn !== "function") return;

    try {
        await fn();
    } catch (error) {
        console.warn(`Error ejecutando ${fn.name || "función"}:`, error);
    }
}

/* =========================
   VISIBILIDAD POR ROL
========================= */

function applyRoleVisibility() {
    if (!currentUser) return;

    const role = currentUser.role;

    // SOLO ADMIN
    document.querySelectorAll(".admin-only").forEach(element => {
        element.style.display = role === "ADMIN" ? "" : "none";
    });

    // ADMIN + RECEPCIONISTA
    document.querySelectorAll(".admin-recepcion-only").forEach(element => {
        element.style.display = role === "ADMIN" || role === "RECEPCIONISTA" ? "" : "none";
    });

    // SOLO PSICÓLOGO
    document.querySelectorAll(".psychologist-only").forEach(element => {
        element.style.display = role === "PSICOLOGO" ? "" : "none";
    });

    // ADMIN + PSICÓLOGO
    document.querySelectorAll(".admin-psychologist-only").forEach(element => {
        element.style.display = role === "ADMIN" || role === "PSICOLOGO" ? "" : "none";
    });

    // TODOS MENOS PSICÓLOGO
    document.querySelectorAll(".not-psychologist-only").forEach(element => {
        element.style.display = role !== "PSICOLOGO" ? "" : "none";
    });

    // DASHBOARD SOLO ADMIN
    document.querySelectorAll(".admin-dashboard-only").forEach(element => {
        element.style.display = role === "ADMIN" ? "" : "none";
    });
}