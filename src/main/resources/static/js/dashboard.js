let dashboardFinanceChart = null;
let dashboardAppointmentsChart = null;
let currentDashboardPeriod = "month";

/* =========================
   DASHBOARD ADMIN PROFESIONAL
   Con datos reales desde backend
========================= */

async function loadDashboard(period = currentDashboardPeriod) {
    try {
        currentDashboardPeriod = period;
        syncDashboardPeriodFilters(period);

        const response = await fetch(`${baseUrl}/dashboard?period=${period}`, {
            headers: {
                "Authorization": `Bearer ${localStorage.getItem("token")}`
            }
        });

        if (!response.ok) {
            throw new Error("No se pudo cargar el dashboard");
        }

        const backendData = await response.json();
        const demoData = buildDashboardDemoData();
        const dashboardData = normalizeDashboardData(backendData, demoData);

        updateDashboardUserInfo();
        updateDashboardKpis(dashboardData);
        renderTodaySchedule(dashboardData.upcomingAppointments);
        renderDashboardReminders(dashboardData);
        renderDashboardCharts(dashboardData);
        applyDashboardByRole();

        if (window.lucide) {
            lucide.createIcons();
        }

    } catch (error) {
        console.error("Error dashboard:", error);

        const demoData = buildDashboardDemoData();

        updateDashboardUserInfo();
        updateDashboardKpis(demoData);
        renderTodaySchedule(demoData.upcomingAppointments);
        renderDashboardReminders(demoData);
        renderDashboardCharts(demoData);
        applyDashboardByRole();

        if (window.lucide) {
            lucide.createIcons();
        }
    }
}

/* =========================
   PERIODO GLOBAL
========================= */

function handleDashboardPeriodChange(period) {
    currentDashboardPeriod = period;
    loadDashboard(period);
}

function syncDashboardPeriodFilters(period) {
    document.querySelectorAll(".dashboard-period-filter").forEach(select => {
        select.value = period;
    });
}

/* =========================
   NORMALIZAR DATA BACKEND
========================= */

function normalizeDashboardData(backendData, demoData) {
    return {
        ...demoData,

        totalPatients: Number(backendData.totalPatients || 0),
        todayAppointments: Number(backendData.todayAppointments || 0),
        totalIncome: Number(backendData.totalIncome || 0),
        totalExpense: Number(backendData.totalExpense || 0),
        profit: Number(backendData.profit || 0),

        pendingPayments: Number(backendData.pendingPayments || 0),
        pendingLeads: Number(backendData.pendingLeads || 0),
        totalAppointmentsMonth: Number(backendData.totalAppointmentsMonth || 0),

        appointmentStatus: Array.isArray(backendData.appointmentStatus)
            ? backendData.appointmentStatus.map(item => ({
                label: item.label || "Sin estado",
                value: Number(item.value || 0),
                color: item.color || "#64748b"
            }))
            : demoData.appointmentStatus,

        financeWeeks: backendData.financeWeeks
            ? {
                labels: backendData.financeWeeks.labels || demoData.financeWeeks.labels,
                income: (backendData.financeWeeks.income || []).map(value => Number(value || 0)),
                expense: (backendData.financeWeeks.expense || []).map(value => Number(value || 0))
            }
            : demoData.financeWeeks,

        upcomingAppointments: Array.isArray(backendData.upcomingAppointments)
            ? backendData.upcomingAppointments.map(item => ({
                id: item.id,
                patient: item.patient || "Paciente",
                service: item.service || "Servicio psicológico",
                psychologist: item.psychologist || "Psicólogo",
                date: item.date || "-",
                time: item.time || "-",
                status: item.status || "Programada",
                initials: item.initials || "CP"
            }))
            : demoData.upcomingAppointments
    };
}

/* =========================
   DATA DEMO DE RESPALDO
========================= */

function buildDashboardDemoData() {
    return {
        totalPatients: 0,
        todayAppointments: 0,
        totalIncome: 0,
        totalExpense: 0,
        profit: 0,
        pendingPayments: 0,
        pendingLeads: 0,
        totalAppointmentsMonth: 0,

        appointmentStatus: [
            { label: "Programadas", value: 0, color: "#2563eb" },
            { label: "Atendidas", value: 0, color: "#22c55e" },
            { label: "Canceladas", value: 0, color: "#ef4444" },
            { label: "No asistió", value: 0, color: "#f59e0b" },
            { label: "Reprogramadas", value: 0, color: "#8b5cf6" }
        ],

        financeWeeks: {
            labels: ["Periodo 1", "Periodo 2", "Periodo 3", "Periodo 4", "Periodo 5"],
            income: [0, 0, 0, 0, 0],
            expense: [0, 0, 0, 0, 0]
        },

        upcomingAppointments: []
    };
}

/* =========================
   USUARIO / KPI
========================= */

function updateDashboardUserInfo() {
    const name = currentUser?.firstName
        ? `${currentUser.firstName} ${currentUser.lastName || ""}`.trim()
        : "Admin Principal";

    const welcomeTitle = document.getElementById("adminWelcomeTitle");

    if (welcomeTitle) {
        welcomeTitle.textContent = `Bienvenido, ${name}`;
    }
}

function updateDashboardKpis(data) {
    setText("dashPatients", data.totalPatients);
    setText("dashAppointments", data.todayAppointments);
    setText("dashIncome", formatCurrency(data.totalIncome));
    setText("dashExpense", formatCurrency(data.totalExpense));
    setText("dashProfit", formatCurrency(data.profit));
    setText("dashPendingPayments", data.pendingPayments);

    const totalAppointments = Array.isArray(data.appointmentStatus)
        ? data.appointmentStatus.reduce((sum, item) => sum + Number(item.value || 0), 0)
        : 0;

    setText("dashAppointmentsTotalMonth", totalAppointments);
}

/* =========================
   GRÁFICOS
========================= */

function renderDashboardCharts(data) {
    if (dashboardFinanceChart) {
        dashboardFinanceChart.destroy();
    }

    if (dashboardAppointmentsChart) {
        dashboardAppointmentsChart.destroy();
    }

    renderAppointmentStatusChart(data.appointmentStatus);
    renderFinanceChart(data.financeWeeks);
    renderAppointmentStatusLegend(data.appointmentStatus);
}

function renderAppointmentStatusChart(statusList) {
    const appointmentsCanvas = document.getElementById("dashboardAppointmentsChart");
    if (!appointmentsCanvas) return;

    dashboardAppointmentsChart = new Chart(appointmentsCanvas, {
        type: "doughnut",
        data: {
            labels: statusList.map(item => item.label),
            datasets: [{
                data: statusList.map(item => item.value),
                backgroundColor: statusList.map(item => item.color),
                borderColor: "#ffffff",
                borderWidth: 4,
                hoverOffset: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: "68%",
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: "#101828",
                    padding: 12,
                    callbacks: {
                        label: function (context) {
                            return `${context.label}: ${context.raw}`;
                        }
                    }
                }
            }
        }
    });
}

function renderFinanceChart(financeWeeks) {
    const financeCanvas = document.getElementById("dashboardFinanceChart");
    if (!financeCanvas) return;

    dashboardFinanceChart = new Chart(financeCanvas, {
        type: "bar",
        data: {
            labels: financeWeeks.labels,
datasets: [
    {
        label: "Ingresos",
        data: financeWeeks.income,
        backgroundColor: "#047857",
        borderRadius: 6,
        barThickness: 20
    },
    {
        label: "Gastos",
        data: financeWeeks.expense,
        backgroundColor: "#b42318",
        borderRadius: 6,
        barThickness: 20
    }
]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: "top",
                    labels: {
                        usePointStyle: true,
                        pointStyle: "rectRounded",
                        color: "#475467",
                        font: {
                            size: 12,
                            weight: "600"
                        }
                    }
                },
                tooltip: {
                    backgroundColor: "#101828",
                    padding: 12,
                    callbacks: {
                        label: function (context) {
                            return `${context.dataset.label}: ${formatCurrency(context.raw)}`;
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        color: "#667085",
                        font: {
                            size: 12
                        }
                    }
                },
                y: {
                    beginAtZero: true,
                    grid: {
                        color: "rgba(208, 213, 221, 0.6)"
                    },
                    ticks: {
                        color: "#667085",
                        callback: function (value) {
                            return value >= 1000 ? `${value / 1000}k` : value;
                        }
                    }
                }
            }
        }
    });
}

function renderAppointmentStatusLegend(statusList) {
    const legend = document.getElementById("appointmentStatusLegend");
    if (!legend) return;

    const total = statusList.reduce((sum, item) => sum + Number(item.value || 0), 0);

    legend.innerHTML = statusList.map(item => {
        const percent = total > 0
            ? ((Number(item.value || 0) / total) * 100).toFixed(1)
            : "0.0";

        return `
            <div class="status-legend-item">
                <div class="status-legend-name">
                    <span style="background:${item.color}"></span>
                    ${item.label}
                </div>
                <strong>${item.value}</strong>
                <em>${percent}%</em>
            </div>
        `;
    }).join("");
}

/* =========================
   CITAS PRÓXIMAS
========================= */

function renderTodaySchedule(appointments) {
    const container = document.getElementById("todaySchedule");
    if (!container) return;

    if (!appointments || appointments.length === 0) {
        container.innerHTML = `
            <div class="premium-empty-state">
                <i data-lucide="calendar-x"></i>
                <span>No hay citas próximas registradas.</span>
            </div>
        `;
        return;
    }

    container.innerHTML = appointments.map(appointment => {
        const normalizedStatus = String(appointment.status || "").toLowerCase();

        let statusClass = "pending";

        if (
            normalizedStatus.includes("programada") ||
            normalizedStatus.includes("confirmada") ||
            normalizedStatus.includes("atendida")
        ) {
            statusClass = "confirmed";
        }

        return `
            <div class="premium-appointment-row">
                <div class="appointment-person">
                    <div class="appointment-avatar">${appointment.initials}</div>
                    <div>
                        <strong>${appointment.patient}</strong>
                        <span>${appointment.service}</span>
                    </div>
                </div>

                <div class="appointment-meta">
                    <span><i data-lucide="calendar"></i>${appointment.date}</span>
                    <span><i data-lucide="clock"></i>${appointment.time}</span>
                </div>

                <span class="premium-status-pill ${statusClass}">
                    ${appointment.status}
                </span>
            </div>
        `;
    }).join("");
}

/* =========================
   RECORDATORIOS
========================= */

function renderDashboardReminders(data) {
    const container = document.getElementById("dashboardSummary");
    if (!container) return;

    const pendingPayments = Number(data.pendingPayments || 0);
    const pendingLeads = Number(data.pendingLeads || 0);
    const todayAppointments = Number(data.todayAppointments || 0);

    container.innerHTML = `
        <button class="premium-reminder-row purple clickable-reminder"
                type="button"
                onclick="goToDashboardReminder('appointments')">
            <div class="reminder-icon">
                <i data-lucide="calendar-check"></i>
            </div>

            <div>
                <strong>${todayAppointments} citas programadas para hoy.</strong>
                <span>Revisa la agenda diaria y confirma la atención clínica.</span>
            </div>

            <i data-lucide="chevron-right"></i>
        </button>

        <button class="premium-reminder-row orange clickable-reminder"
                type="button"
                onclick="goToDashboardReminder('payments')">
            <div class="reminder-icon">
                <i data-lucide="triangle-alert"></i>
            </div>

            <div>
                <strong>${pendingPayments} pagos pendientes de revisión.</strong>
                <span>Valida adelantos, saldos pendientes y comprobantes registrados.</span>
            </div>

            <i data-lucide="chevron-right"></i>
        </button>

        <button class="premium-reminder-row blue clickable-reminder"
                type="button"
                onclick="goToDashboardReminder('leads')">
            <div class="reminder-icon">
                <i data-lucide="clipboard-list"></i>
            </div>

            <div>
                <strong>${pendingLeads} pre-reservas en seguimiento.</strong>
                <span>Gestiona solicitudes del portal público para convertirlas en citas.</span>
            </div>

            <i data-lucide="chevron-right"></i>
        </button>
    `;
}

function goToDashboardReminder(type) {
    if (type === "appointments") {
        showSectionById("appointments");

        setTimeout(() => {
            const agendaDate = document.getElementById("agendaDate");

            if (agendaDate) {
                agendaDate.value = new Date().toISOString().split("T")[0];
            }

            if (typeof loadAgenda === "function") {
                loadAgenda();
            }
        }, 200);

        return;
    }

    if (type === "payments") {
        showSectionById("appointments");

        setTimeout(() => {
            if (typeof toggleAppointmentList === "function") {
                toggleAppointmentList();
            }
        }, 250);

        return;
    }

    if (type === "leads") {
        showSectionById("leads");

        setTimeout(() => {
            if (typeof loadLeads === "function") {
                loadLeads();
            }
        }, 200);
    }
}

/* =========================
   ROLES / REALTIME
========================= */

function applyDashboardByRole() {
    if (!currentUser) return;

    if (currentUser.role !== "ADMIN") {
        document.querySelectorAll(".admin-dashboard-only").forEach(element => {
            element.style.display = "none";
        });
    }
}

async function refreshDashboardRealtime() {
    if (typeof loadDashboard === "function") {
        await loadDashboard(currentDashboardPeriod);
    }
}

/* =========================
   HELPERS
========================= */

function setText(id, value) {
    const element = document.getElementById(id);
    if (element) {
        element.textContent = value;
    }
}

function formatCurrency(value) {
    const number = Number(value || 0);

    return `S/ ${number.toLocaleString("es-PE", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    })}`;
}