let dashboardFinanceChart = null;
let dashboardAppointmentsChart = null;
let currentDashboardPeriod = "month";

/* =========================================================
   CARGA PRINCIPAL DEL DASHBOARD
========================================================= */

async function loadDashboard(period = currentDashboardPeriod) {
    const normalizedPeriod = normalizeDashboardPeriod(period);
    currentDashboardPeriod = normalizedPeriod;

    syncDashboardPeriodFilters(normalizedPeriod);

    try {
        const response = await fetchDashboardData(normalizedPeriod);

        if (!response.ok) {
            throw new Error(
                `No se pudo cargar el dashboard. Estado: ${response.status}`
            );
        }

        const backendData = await response.json();
        const dashboardData = normalizeDashboardData(backendData);

        const role = getDashboardRole(dashboardData);

        applyDashboardByRole(role);
        updateDashboardUserInfo(role);
        updateDashboardPeriodLabels(normalizedPeriod, role);
        updateDashboardKpis(dashboardData, role);
        renderTodaySchedule(dashboardData.upcomingAppointments, role);
        renderDashboardReminders(dashboardData, role);
        renderDashboardCharts(dashboardData, role);

        refreshDashboardIcons();

    } catch (error) {
        console.error("Error al cargar el dashboard:", error);

        const emptyData = buildEmptyDashboardData();
        const role = getDashboardRole(emptyData);

        applyDashboardByRole(role);
        updateDashboardUserInfo(role);
        updateDashboardPeriodLabels(normalizedPeriod, role);
        updateDashboardKpis(emptyData, role);
        renderTodaySchedule([], role);
        renderDashboardLoadError();
        renderDashboardCharts(emptyData, role);

        refreshDashboardIcons();
    }
}

/* =========================================================
   PETICIÓN AL BACKEND
========================================================= */

async function fetchDashboardData(period) {
    const url = `${baseUrl}/dashboard?period=${encodeURIComponent(period)}`;

    /*
     * Se utiliza authFetch cuando está disponible.
     * De lo contrario, se realiza una petición normal con JWT.
     */
    if (typeof authFetch === "function") {
        return authFetch(url);
    }

    return fetch(url, {
        method: "GET",
        headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`
        }
    });
}

/* =========================================================
   CAMBIO DE PERIODO
========================================================= */

function handleDashboardPeriodChange(period) {
    const normalizedPeriod = normalizeDashboardPeriod(period);

    currentDashboardPeriod = normalizedPeriod;
    loadDashboard(normalizedPeriod);
}

function normalizeDashboardPeriod(period) {
    const normalized = String(period || "")
        .trim()
        .toLowerCase();

    const allowedPeriods = ["day", "week", "month", "year"];

    return allowedPeriods.includes(normalized)
        ? normalized
        : "month";
}

function syncDashboardPeriodFilters(period) {
    document
        .querySelectorAll(".dashboard-period-filter")
        .forEach(select => {
            select.value = period;
        });
}

/* =========================================================
   NORMALIZACIÓN DE LA RESPUESTA
========================================================= */

function normalizeDashboardData(backendData = {}) {
    const emptyData = buildEmptyDashboardData();

    const appointmentStatus =
        Array.isArray(backendData.appointmentStatus) &&
        backendData.appointmentStatus.length > 0
            ? backendData.appointmentStatus.map(item => ({
                label: item?.label || "Sin estado",
                value: Number(item?.value || 0),
                color: item?.color || "#64748b"
            }))
            : emptyData.appointmentStatus;

    const upcomingAppointments =
        Array.isArray(backendData.upcomingAppointments)
            ? backendData.upcomingAppointments.map(item => ({
                id: item?.id || null,
                patient: item?.patient || "Paciente",
                service: item?.service || "Servicio psicológico",
                psychologist: item?.psychologist || "Psicólogo",
                date: item?.date || "-",
                time: item?.time || "-",
                status: item?.status || "Programada",
                initials: item?.initials || "CP"
            }))
            : [];

    return {
        role: normalizeDashboardRole(
            backendData.role ||
            currentUser?.role ||
            ""
        ),

        period: normalizeDashboardPeriod(
            backendData.period ||
            currentDashboardPeriod
        ),

        totalPatients: Number(
            backendData.totalPatients || 0
        ),

        /*
         * Este es el valor que debe cambiar cuando se selecciona:
         * día, semana, mes o año.
         */
        periodAppointments: Number(
            backendData.periodAppointments ??
            backendData.totalAppointmentsMonth ??
            0
        ),

        /*
         * Este valor se utiliza únicamente para recordatorios
         * relacionados con el día actual.
         */
        todayAppointments: Number(
            backendData.todayAppointments || 0
        ),

        todayScheduledAppointments: Number(
            backendData.todayScheduledAppointments || 0
        ),

        scheduledAppointments: Number(
            backendData.scheduledAppointments || 0
        ),

        attendedAppointments: Number(
            backendData.attendedAppointments || 0
        ),

        cancelledAppointments: Number(
            backendData.cancelledAppointments || 0
        ),

        noShowAppointments: Number(
            backendData.noShowAppointments || 0
        ),

        rescheduledAppointments: Number(
            backendData.rescheduledAppointments || 0
        ),

        totalIncome: Number(
            backendData.totalIncome || 0
        ),

        totalExpense: Number(
            backendData.totalExpense || 0
        ),

        profit: Number(
            backendData.profit || 0
        ),

        pendingPayments: Number(
            backendData.pendingPayments || 0
        ),

        pendingLeads: Number(
            backendData.pendingLeads || 0
        ),

        appointmentStatus,

        financeWeeks: normalizeFinanceWeeks(
            backendData.financeWeeks,
            emptyData.financeWeeks
        ),

        upcomingAppointments
    };
}

function normalizeFinanceWeeks(financeWeeks, fallback) {
    if (!financeWeeks) {
        return fallback;
    }

    return {
        labels: Array.isArray(financeWeeks.labels)
            ? financeWeeks.labels
            : fallback.labels,

        income: Array.isArray(financeWeeks.income)
            ? financeWeeks.income.map(value => Number(value || 0))
            : fallback.income,

        expense: Array.isArray(financeWeeks.expense)
            ? financeWeeks.expense.map(value => Number(value || 0))
            : fallback.expense
    };
}

/* =========================================================
   DATOS VACÍOS DE RESPALDO
========================================================= */

function buildEmptyDashboardData() {
    return {
        role: normalizeDashboardRole(
            currentUser?.role || ""
        ),

        period: currentDashboardPeriod,

        totalPatients: 0,
        periodAppointments: 0,
        todayAppointments: 0,
        todayScheduledAppointments: 0,

        scheduledAppointments: 0,
        attendedAppointments: 0,
        cancelledAppointments: 0,
        noShowAppointments: 0,
        rescheduledAppointments: 0,

        totalIncome: 0,
        totalExpense: 0,
        profit: 0,

        pendingPayments: 0,
        pendingLeads: 0,

        appointmentStatus: [
            {
                label: "Programadas",
                value: 0,
                color: "#2563eb"
            },
            {
                label: "Atendidas",
                value: 0,
                color: "#047857"
            },
            {
                label: "Canceladas",
                value: 0,
                color: "#b42318"
            },
            {
                label: "No asistió",
                value: 0,
                color: "#c2410c"
            },
            {
                label: "Reprogramadas",
                value: 0,
                color: "#475467"
            }
        ],

        financeWeeks: {
            labels: [],
            income: [],
            expense: []
        },

        upcomingAppointments: []
    };
}

/* =========================================================
   INFORMACIÓN DEL USUARIO
========================================================= */

function updateDashboardUserInfo(role) {
    const firstName =
        currentUser?.firstName ||
        currentUser?.firstname ||
        currentUser?.name ||
        "";

    const lastName =
        currentUser?.lastName ||
        currentUser?.lastname ||
        "";

    const fullName = `${firstName} ${lastName}`
        .replace(/\s+/g, " ")
        .trim();

    const fallbackNames = {
        ADMIN: "Administrador",
        RECEPCIONISTA: "Recepcionista",
        PSICOLOGO: "Psicólogo"
    };

    const displayName =
        fullName ||
        fallbackNames[role] ||
        "Usuario";

    setText(
        "adminWelcomeTitle",
        `Bienvenido, ${displayName}`
    );
}

/* =========================================================
   INDICADORES
========================================================= */

function updateDashboardKpis(data, role) {
    setText(
        "dashPatients",
        data.totalPatients
    );

    /*
     * Antes se utilizaba todayAppointments.
     * Ahora se utiliza el total del periodo seleccionado.
     */
    setText(
        "dashAppointments",
        data.periodAppointments
    );

    setText(
        "dashIncome",
        formatCurrency(data.totalIncome)
    );

    setText(
        "dashExpense",
        formatCurrency(data.totalExpense)
    );

    setText(
        "dashProfit",
        formatCurrency(data.profit)
    );

    setText(
        "dashPendingPayments",
        data.pendingPayments
    );

    /*
     * El número del centro del gráfico debe coincidir
     * con la cantidad del periodo.
     */
    setText(
        "dashAppointmentsTotalMonth",
        data.periodAppointments
    );

    updatePatientCardTexts(role);
    updateAppointmentCardTexts(role);
}

function updatePatientCardTexts(role) {
    if (role === "PSICOLOGO") {
        setText(
            "dashPatientsTitle",
            "Pacientes asignados"
        );

        setText(
            "dashPatientsSubtitle",
            "Vinculados con mis citas del periodo"
        );

        return;
    }

    setText(
        "dashPatientsTitle",
        "Pacientes"
    );

    setText(
        "dashPatientsSubtitle",
        "Total registrados"
    );
}

function updateAppointmentCardTexts(role) {
    setText(
        "dashAppointmentsSubtitle",
        role === "PSICOLOGO"
            ? "Citas asignadas en el periodo"
            : "Citas registradas en el periodo"
    );
}

/* =========================================================
   CONFIGURACIÓN VISUAL SEGÚN EL ROL
========================================================= */

function applyDashboardByRole(roleInput) {
    const role = normalizeDashboardRole(roleInput);

    const isAdmin =
        role === "ADMIN";

    document
        .querySelectorAll(".admin-dashboard-only")
        .forEach(element => {
            element.style.display =
                isAdmin ? "" : "none";
        });

    /*
     * Cuando el gráfico financiero está oculto,
     * el gráfico de citas debe ocupar todo el ancho.
     */
    const analyticsGrid =
        document.querySelector(
            "#home .premium-dashboard-grid"
        );

    if (analyticsGrid) {
        analyticsGrid.style.gridTemplateColumns =
            isAdmin
                ? ""
                : "minmax(0, 1fr)";
    }

    /*
     * Los porcentajes actuales son textos fijos,
     * no están calculados por el backend.
     * Se ocultan para no mostrar información falsa.
     */
    document
        .querySelectorAll("#home .kpi-trend")
        .forEach(element => {
            element.style.display = "none";
        });

    updateDashboardHeaderByRole(role);
    updateBottomDashboardTexts(role);

    const dashboard =
        document.querySelector("#home .admin-dashboard");

    if (dashboard) {
        dashboard.dataset.role =
            role.toLowerCase();
    }
}

function updateDashboardHeaderByRole(role) {
    const configuration = {
        ADMIN: {
            eyebrow: "Dashboard administrativo",
            subtitle:
                "Indicadores operativos, agenda clínica y control financiero del centro."
        },

        RECEPCIONISTA: {
            eyebrow: "Dashboard operativo",
            subtitle:
                "Consulta pacientes, solicitudes, citas y actividades administrativas."
        },

        PSICOLOGO: {
            eyebrow: "Dashboard clínico",
            subtitle:
                "Consulta tus citas asignadas, pacientes relacionados y próximas atenciones."
        }
    };

    const selected =
        configuration[role] ||
        configuration.ADMIN;

    setText(
        "dashboardEyebrow",
        selected.eyebrow
    );

    setText(
        "dashboardMainSubtitle",
        selected.subtitle
    );
}

function updateBottomDashboardTexts(role) {
    if (role === "PSICOLOGO") {
        setText(
            "dashUpcomingAppointmentsTitle",
            "Mis próximas citas"
        );

        setText(
            "dashUpcomingAppointmentsSubtitle",
            "Próximas atenciones asignadas"
        );

        setText(
            "dashRemindersTitle",
            "Resumen clínico"
        );

        setText(
            "dashRemindersSubtitle",
            "Indicadores de mis atenciones"
        );

        return;
    }

    setText(
        "dashUpcomingAppointmentsTitle",
        "Citas próximas"
    );

    setText(
        "dashUpcomingAppointmentsSubtitle",
        "Agenda relevante del centro"
    );

    setText(
        "dashRemindersTitle",
        "Recordatorios"
    );

    setText(
        "dashRemindersSubtitle",
        "Alertas importantes del sistema"
    );
}

/* =========================================================
   GRÁFICOS
========================================================= */

function renderDashboardCharts(data, role) {
    destroyDashboardCharts();

    renderAppointmentStatusChart(
        data.appointmentStatus
    );

    renderAppointmentStatusLegend(
        data.appointmentStatus
    );

    /*
     * El gráfico financiero solamente debe
     * crearse cuando el usuario sea ADMIN.
     */
    if (role === "ADMIN") {
        renderFinanceChart(
            data.financeWeeks
        );
    }
}

function destroyDashboardCharts() {
    if (dashboardFinanceChart) {
        dashboardFinanceChart.destroy();
        dashboardFinanceChart = null;
    }

    if (dashboardAppointmentsChart) {
        dashboardAppointmentsChart.destroy();
        dashboardAppointmentsChart = null;
    }
}

function renderAppointmentStatusChart(statusList) {
    const appointmentsCanvas =
        document.getElementById(
            "dashboardAppointmentsChart"
        );

    if (
        !appointmentsCanvas ||
        typeof Chart === "undefined"
    ) {
        return;
    }

    const safeStatusList =
        Array.isArray(statusList)
            ? statusList
            : [];

    dashboardAppointmentsChart =
        new Chart(appointmentsCanvas, {
            type: "doughnut",

            data: {
                labels:
                    safeStatusList.map(
                        item => item.label
                    ),

                datasets: [
                    {
                        data:
                            safeStatusList.map(
                                item =>
                                    Number(item.value || 0)
                            ),

                        backgroundColor:
                            safeStatusList.map(
                                item => item.color
                            ),

                        borderColor: "#ffffff",
                        borderWidth: 4,
                        hoverOffset: 6
                    }
                ]
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
                            label(context) {
                                return `${context.label}: ${context.raw}`;
                            }
                        }
                    }
                }
            }
        });
}

function renderFinanceChart(financeWeeks) {
    const financeCanvas =
        document.getElementById(
            "dashboardFinanceChart"
        );

    if (
        !financeCanvas ||
        typeof Chart === "undefined"
    ) {
        return;
    }

    const labels =
        Array.isArray(financeWeeks?.labels)
            ? financeWeeks.labels
            : [];

    const incomes =
        Array.isArray(financeWeeks?.income)
            ? financeWeeks.income
            : [];

    const expenses =
        Array.isArray(financeWeeks?.expense)
            ? financeWeeks.expense
            : [];

    dashboardFinanceChart =
        new Chart(financeCanvas, {
            type: "bar",

            data: {
                labels,

                datasets: [
                    {
                        label: "Ingresos",
                        data: incomes,
                        backgroundColor: "#047857",
                        borderRadius: 6,
                        barThickness: 20
                    },
                    {
                        label: "Gastos",
                        data: expenses,
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
                            label(context) {
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
                            color:
                                "rgba(208, 213, 221, 0.6)"
                        },

                        ticks: {
                            color: "#667085",

                            callback(value) {
                                return value >= 1000
                                    ? `${value / 1000}k`
                                    : value;
                            }
                        }
                    }
                }
            }
        });
}

function renderAppointmentStatusLegend(statusList) {
    const legend =
        document.getElementById(
            "appointmentStatusLegend"
        );

    if (!legend) {
        return;
    }

    const safeStatusList =
        Array.isArray(statusList)
            ? statusList
            : [];

    const total =
        safeStatusList.reduce(
            (sum, item) =>
                sum + Number(item.value || 0),
            0
        );

    legend.innerHTML =
        safeStatusList
            .map(item => {
                const value =
                    Number(item.value || 0);

                const percent =
                    total > 0
                        ? (
                            (value / total) *
                            100
                        ).toFixed(1)
                        : "0.0";

                return `
                    <div class="status-legend-item">
                        <div class="status-legend-name">
                            <span style="background:${escapeHtml(item.color)}"></span>
                            ${escapeHtml(item.label)}
                        </div>

                        <strong>${value}</strong>
                        <em>${percent}%</em>
                    </div>
                `;
            })
            .join("");
}

/* =========================================================
   PRÓXIMAS CITAS
========================================================= */

function renderTodaySchedule(appointments, role) {
    const container =
        document.getElementById(
            "todaySchedule"
        );

    if (!container) {
        return;
    }

    if (
        !Array.isArray(appointments) ||
        appointments.length === 0
    ) {
        container.innerHTML = `
            <div class="premium-empty-state">
                <i data-lucide="calendar-x"></i>
                <span>
                    ${
                        role === "PSICOLOGO"
                            ? "No tienes citas próximas asignadas."
                            : "No hay citas próximas registradas."
                    }
                </span>
            </div>
        `;

        return;
    }

    container.innerHTML =
        appointments
            .map(appointment => {
                const normalizedStatus =
                    String(
                        appointment.status || ""
                    ).toLowerCase();

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
                            <div class="appointment-avatar">
                                ${escapeHtml(appointment.initials)}
                            </div>

                            <div>
                                <strong>
                                    ${escapeHtml(appointment.patient)}
                                </strong>

                                <span>
                                    ${escapeHtml(appointment.service)}
                                </span>
                            </div>
                        </div>

                        <div class="appointment-meta">
                            <span>
                                <i data-lucide="calendar"></i>
                                ${escapeHtml(appointment.date)}
                            </span>

                            <span>
                                <i data-lucide="clock"></i>
                                ${escapeHtml(appointment.time)}
                            </span>
                        </div>

                        <span class="premium-status-pill ${statusClass}">
                            ${escapeHtml(appointment.status)}
                        </span>
                    </div>
                `;
            })
            .join("");
}

/* =========================================================
   RECORDATORIOS SEGÚN ROL
========================================================= */

function renderDashboardReminders(data, role) {
    const container =
        document.getElementById(
            "dashboardSummary"
        );

    if (!container) {
        return;
    }

    if (role === "PSICOLOGO") {
        renderPsychologistReminders(
            container,
            data
        );

        return;
    }

    renderAdministrativeReminders(
        container,
        data,
        role
    );
}

function renderPsychologistReminders(container, data) {
    const todayScheduled =
        Number(
            data.todayScheduledAppointments || 0
        );

    const attended =
        Number(
            data.attendedAppointments || 0
        );

    const noShow =
        Number(
            data.noShowAppointments || 0
        );

    container.innerHTML = `
        <button
            class="premium-reminder-row purple clickable-reminder"
            type="button"
            onclick="goToDashboardReminder('appointments')"
        >
            <div class="reminder-icon">
                <i data-lucide="calendar-check"></i>
            </div>

            <div>
                <strong>
                    ${todayScheduled} citas programadas para hoy.
                </strong>

                <span>
                    Revisa tu agenda y las próximas atenciones asignadas.
                </span>
            </div>

            <i data-lucide="chevron-right"></i>
        </button>

        <button
            class="premium-reminder-row blue clickable-reminder"
            type="button"
            onclick="goToDashboardReminder('attended')"
        >
            <div class="reminder-icon">
                <i data-lucide="clipboard-check"></i>
            </div>

            <div>
                <strong>
                    ${attended} citas atendidas en el periodo.
                </strong>

                <span>
                    Consulta las atenciones registradas y su seguimiento clínico.
                </span>
            </div>

            <i data-lucide="chevron-right"></i>
        </button>

        <button
            class="premium-reminder-row orange clickable-reminder"
            type="button"
            onclick="goToDashboardReminder('no-show')"
        >
            <div class="reminder-icon">
                <i data-lucide="user-x"></i>
            </div>

            <div>
                <strong>
                    ${noShow} pacientes no asistieron en el periodo.
                </strong>

                <span>
                    Revisa las citas marcadas como no asistió.
                </span>
            </div>

            <i data-lucide="chevron-right"></i>
        </button>
    `;
}

function renderAdministrativeReminders(
    container,
    data,
    role
) {
    const todayScheduled =
        Number(
            data.todayScheduledAppointments ||
            data.todayAppointments ||
            0
        );

    const pendingPayments =
        Number(
            data.pendingPayments || 0
        );

    const pendingLeads =
        Number(
            data.pendingLeads || 0
        );

    container.innerHTML = `
        <button
            class="premium-reminder-row purple clickable-reminder"
            type="button"
            onclick="goToDashboardReminder('appointments')"
        >
            <div class="reminder-icon">
                <i data-lucide="calendar-check"></i>
            </div>

            <div>
                <strong>
                    ${todayScheduled} citas programadas para hoy.
                </strong>

                <span>
                    Revisa la agenda diaria y coordina las atenciones.
                </span>
            </div>

            <i data-lucide="chevron-right"></i>
        </button>

        <button
            class="premium-reminder-row orange clickable-reminder"
            type="button"
            onclick="goToDashboardReminder('payments')"
        >
            <div class="reminder-icon">
                <i data-lucide="triangle-alert"></i>
            </div>

            <div>
                <strong>
                    ${pendingPayments} pagos pendientes de revisión.
                </strong>

                <span>
                    Valida adelantos, saldos y comprobantes registrados.
                </span>
            </div>

            <i data-lucide="chevron-right"></i>
        </button>

        <button
            class="premium-reminder-row blue clickable-reminder"
            type="button"
            onclick="goToDashboardReminder('leads')"
        >
            <div class="reminder-icon">
                <i data-lucide="clipboard-list"></i>
            </div>

            <div>
                <strong>
                    ${pendingLeads} pre-reservas en seguimiento.
                </strong>

                <span>
                    Gestiona las solicitudes enviadas desde el portal público.
                </span>
            </div>

            <i data-lucide="chevron-right"></i>
        </button>
    `;
}

function renderDashboardLoadError() {
    const container =
        document.getElementById(
            "dashboardSummary"
        );

    if (!container) {
        return;
    }

    container.innerHTML = `
        <div class="premium-empty-state">
            <i data-lucide="triangle-alert"></i>
            <span>
                No se pudieron cargar los indicadores del dashboard.
            </span>
        </div>
    `;
}

/* =========================================================
   NAVEGACIÓN DESDE LOS RECORDATORIOS
========================================================= */

function goToDashboardReminder(type) {
    if (
        type === "appointments" ||
        type === "attended" ||
        type === "no-show"
    ) {
        showSectionById("appointments");

        setTimeout(() => {
            const agendaDate =
                document.getElementById(
                    "agendaDate"
                );

            if (agendaDate) {
                agendaDate.value =
                    new Date()
                        .toISOString()
                        .split("T")[0];
            }

            if (
                type === "appointments" &&
                typeof loadAgenda === "function"
            ) {
                loadAgenda();
            }

            if (
                type !== "appointments" &&
                typeof toggleAppointmentList === "function"
            ) {
                toggleAppointmentList();
            }
        }, 200);

        return;
    }

    if (type === "payments") {
        showSectionById("appointments");

        setTimeout(() => {
            if (
                typeof toggleAppointmentList === "function"
            ) {
                toggleAppointmentList();
            }
        }, 250);

        return;
    }

    if (type === "leads") {
        showSectionById("leads");

        setTimeout(() => {
            if (
                typeof loadLeads === "function"
            ) {
                loadLeads();
            }
        }, 200);
    }
}

/* =========================================================
   ETIQUETAS DEL PERIODO
========================================================= */

function updateDashboardPeriodLabels(
    period = currentDashboardPeriod,
    roleInput = ""
) {
    const role =
        normalizeDashboardRole(
            roleInput ||
            currentUser?.role ||
            ""
        );

    const labels = {
        day: {
            shortName: "Hoy",
            statusTime: "del día",
            financeTime: "del día"
        },

        week: {
            shortName: "Semana",
            statusTime: "de la semana",
            financeTime: "semanal"
        },

        month: {
            shortName: "Mes",
            statusTime: "del mes",
            financeTime: "mensual"
        },

        year: {
            shortName: "Año",
            statusTime: "del año",
            financeTime: "anual"
        }
    };

    const selected =
        labels[period] ||
        labels.month;

    const appointmentPrefix =
        role === "PSICOLOGO"
            ? "Mis citas"
            : "Citas";

    setTextIfExists(
        "dashAppointmentsTitle",
        `${appointmentPrefix} (${selected.shortName})`
    );

    setTextIfExists(
        "dashAppointmentStatusTitle",
        role === "PSICOLOGO"
            ? `Mis citas por estado (${selected.shortName})`
            : `Citas por estado (${selected.shortName})`
    );

    setTextIfExists(
        "dashAppointmentStatusSubtitle",
        role === "PSICOLOGO"
            ? `Distribución de mis citas ${selected.statusTime}`
            : `Distribución de citas ${selected.statusTime}`
    );

    setTextIfExists(
        "dashIncomeTitle",
        `Ingresos (${selected.shortName})`
    );

    setTextIfExists(
        "dashPendingPaymentsTitle",
        `Pendientes de pago (${selected.shortName})`
    );

    setTextIfExists(
        "dashFinanceChartTitle",
        `Ingresos vs. gastos (${selected.shortName})`
    );

    setTextIfExists(
        "dashFinanceChartSubtitle",
        `Comparación financiera ${selected.financeTime}`
    );
}

/* =========================================================
   ACTUALIZACIÓN EN TIEMPO REAL
========================================================= */

async function refreshDashboardRealtime() {
    await loadDashboard(
        currentDashboardPeriod
    );
}

/* =========================================================
   HELPERS
========================================================= */

function getDashboardRole(data = {}) {
    return normalizeDashboardRole(
        data.role ||
        currentUser?.role ||
        ""
    );
}

function normalizeDashboardRole(role) {
    return String(role || "")
        .trim()
        .toUpperCase()
        .replace("ROLE_", "");
}

function setText(id, value) {
    const element =
        document.getElementById(id);

    if (element) {
        element.textContent = value;
    }
}

function setTextIfExists(id, text) {
    const element =
        document.getElementById(id);

    if (element) {
        element.textContent = text;
    }
}

function formatCurrency(value) {
    const number =
        Number(value || 0);

    return `S/ ${number.toLocaleString("es-PE", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    })}`;
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function refreshDashboardIcons() {
    if (window.lucide) {
        lucide.createIcons();
    }
}