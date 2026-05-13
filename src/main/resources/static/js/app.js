const baseUrl = "/api";

/* =========================
   INIT PRINCIPAL
========================= */

window.onload = async () => {
    if (!validateSession()) return;

    await loadSidebar();
    applyRoleVisibility();
    loadDashboard();

    // ADMIN y RECEPCIONISTA
    if (currentUser.role === "ADMIN" || currentUser.role === "RECEPCIONISTA") {
        loadPatients();
        loadPatientOptions();
        loadPsychologistOptions();
        loadServiceOptions();
    }

    // SOLO ADMIN
    if (currentUser.role === "ADMIN") {
        loadUsers();

        loadPsychologists();
        loadAvailabilityPsychologistOptions();

        loadServices();

        loadIncomes();
        loadExpenseCategories();
        loadExpenseCategoryOptions();
        loadExpenses();
    }

    // TODOS LOS ROLES
    loadAppointments();
    initAppointmentAvailabilityEvents();

    // PSICOLOGO
    if (currentUser.role === "PSICOLOGO") {
        loadPsychologistOptions();
        loadServiceOptions();
    }

    await loadNotifications();
    connectNotificationWebSocket();

    // RECORDATORIOS INTERNOS
    if (typeof loadReminders === "function") {
        loadReminders();
    }
};

function applyRoleVisibility() {
    if (!currentUser) return;

    const role = currentUser.role;

    document.querySelectorAll(".admin-dashboard-only").forEach(element => {
        element.style.display = role === "ADMIN" ? "" : "none";
    });

    if (role === "PSICOLOGO") {
        document.querySelectorAll(".admin-only, .recepcion-only").forEach(element => {
            element.style.display = "none";
        });
    }

    if (role === "RECEPCIONISTA") {
        document.querySelectorAll(".admin-only").forEach(element => {
            element.style.display = "none";
        });
    }

    if (role === "ADMIN") {
        document.querySelectorAll(".admin-only, .recepcion-only").forEach(element => {
            element.style.display = "";
        });
    }
}