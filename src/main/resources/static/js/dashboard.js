let dashboardFinanceChart = null;
let dashboardAppointmentsChart = null;

async function loadDashboard() {
    try {
        let patients = [];

        if (currentUser.role === "ADMIN" || currentUser.role === "RECEPCIONISTA") {
            const patientsRes = await authFetch(`${baseUrl}/patients`);
            patients = patientsRes && patientsRes.ok ? await patientsRes.json() : [];
            document.getElementById("dashPatients").textContent = patients.length;
        } else {
            document.getElementById("dashPatients").textContent = "No disponible";
        }

        // ✅ FECHA LOCAL (ARREGLA EL PROBLEMA)
        const now = new Date();
        const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;

        const appointmentsUrl = currentUser.role === "PSICOLOGO"
            ? `${baseUrl}/appointments/my/by-date?date=${today}`
            : `${baseUrl}/appointments/by-date?date=${today}`;

        const appRes = await authFetch(appointmentsUrl);
        const appointments = appRes && appRes.ok ? await appRes.json() : [];

        // 🔍 DEBUG (puedes quitar luego)
        console.table(appointments.map(a => ({
            id: a.id,
            fecha: a.date,
            estado: a.status
        })));

        renderTodaySchedule(appointments);

        document.getElementById("dashAppointments").textContent = appointments.length;

        let totalIncome = 0;
        let totalExpense = 0;
        let profit = 0;

        if (currentUser.role === "ADMIN") {
            const year = now.getFullYear();
            const month = now.getMonth() + 1;

            const reportRes = await authFetch(`${baseUrl}/reports/monthly?year=${year}&month=${month}`);

            if (reportRes && reportRes.ok) {
                const report = await reportRes.json();

                totalIncome = Number(report.totalIncome || 0);
                totalExpense = Number(report.totalExpense || 0);
                profit = totalIncome - totalExpense;
            }
        }

        document.getElementById("dashIncome").textContent =
            currentUser.role === "ADMIN" ? `S/ ${totalIncome.toFixed(2)}` : "No disponible";

        document.getElementById("dashExpense").textContent =
            currentUser.role === "ADMIN" ? `S/ ${totalExpense.toFixed(2)}` : "No disponible";

        document.getElementById("dashProfit").textContent =
            currentUser.role === "ADMIN" ? `S/ ${profit.toFixed(2)}` : "No disponible";

        document.getElementById("dashStatus").textContent =
            currentUser.role === "ADMIN"
                ? (profit >= 0 ? "GANANCIA" : "PÉRDIDA")
                : "RESTRINGIDO";

        document.getElementById("dashboardSummary").textContent =
            appointments.length === 0
                ? "No hay citas registradas para hoy."
                : `Hoy hay ${appointments.length} cita(s).`;

        renderDashboardCharts(totalIncome, totalExpense, profit, appointments);
applyDashboardByRole();

    } catch (error) {
        console.error("Error dashboard:", error);
        document.getElementById("dashboardSummary").textContent =
            "Error al cargar dashboard";
    }
}

function renderDashboardCharts(totalIncome, totalExpense, profit, appointments) {
    if (dashboardFinanceChart) dashboardFinanceChart.destroy();
    if (dashboardAppointmentsChart) dashboardAppointmentsChart.destroy();

    const financeCanvas = document.getElementById("dashboardFinanceChart");
    const appointmentsCanvas = document.getElementById("dashboardAppointmentsChart");

    if (financeCanvas) {
        dashboardFinanceChart = new Chart(financeCanvas, {
            type: "bar",
            data: {
                labels: ["Ingresos", "Gastos", "Resultado"],
                datasets: [{
                    label: "Monto S/",
                    data: [totalIncome, totalExpense, profit],
                    backgroundColor: [
                        "#78c87b",
                        "#f2827a",
                        profit >= 0 ? "#538ebf" : "#de9680"
                    ],
                    borderRadius: 8
                }]
            }
        });
    }

    const statusCount = {
        PROGRAMADA: 0,
        ATENDIDA: 0,
        CANCELADA: 0,
        NO_ASISTIO: 0,
        REPROGRAMADA: 0
    };

    appointments.forEach(a => {
        if (statusCount[a.status] !== undefined) {
            statusCount[a.status]++;
        }
    });

    if (appointmentsCanvas) {
        dashboardAppointmentsChart = new Chart(appointmentsCanvas, {
            type: "doughnut",
            data: {
                labels: ["Programadas", "Atendidas", "Canceladas", "No asistió", "Reprogramadas"],
                datasets: [{
                    data: [
                        statusCount.PROGRAMADA,
                        statusCount.ATENDIDA,
                        statusCount.CANCELADA,
                        statusCount.NO_ASISTIO,
                        statusCount.REPROGRAMADA
                    ],
                    backgroundColor: [
                        "#36A2EB",
                        "#FF6384",
                        "#FF9F40",
                        "#FFCD56",
                        "#4BC0C0"
                    ],
                    borderColor: "#ffffff",
                    borderWidth: 3
                }]
            }
        });
    }
}

function renderTodaySchedule(appointments) {
    const container = document.getElementById("todaySchedule");
    if (!container) return;

    container.innerHTML = "";

    if (!appointments || appointments.length === 0) {
        container.innerHTML = `<div class="empty-state">No hay citas programadas para hoy.</div>`;
        return;
    }

    appointments.sort((a, b) => {
        const timeA = a.startTime || "";
        const timeB = b.startTime || "";
        return timeA.localeCompare(timeB);
    });

    appointments.slice(0, 5).forEach(app => {
        const patientName = app.patient
            ? `${app.patient.firstName ?? ""} ${app.patient.lastName ?? ""}`
            : "Paciente no registrado";

        const serviceName = app.service ? app.service.name : "Servicio no registrado";

        container.innerHTML += `
            <div class="agenda-item">
                <div>
                    <div class="agenda-time">${app.startTime ?? "--:--"} - ${app.endTime ?? "--:--"}</div>
                    <div class="agenda-info">${patientName} · ${serviceName}</div>
                </div>
                <span class="badge badge-role">${app.status ?? "PROGRAMADA"}</span>
            </div>
        `;
    });
}

function applyDashboardByRole() {
    if (!currentUser) return;

    if (currentUser.role !== "ADMIN") {
        document.querySelectorAll(".admin-dashboard-only").forEach(element => {
            element.style.display = "none";
        });
    }
}
