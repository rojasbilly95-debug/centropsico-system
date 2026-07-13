let incomeExpenseChart = null;
let appointmentStatusChart = null;
let monthlyEvolutionChart = null;

async function loadMonthlyReport() {
    const year = document.getElementById("reportYear").value;
    const month = document.getElementById("reportMonth").value;

    if (!year || !month) {
        Swal.fire("Error", "Ingresa año y mes", "warning");
        return;
    }

    try {
        const response = await authFetch(`${baseUrl}/reports/monthly?year=${year}&month=${month}`);
        if (!response) return;

        const data = await response.json();

        if (!response.ok) {
            Swal.fire("Error", "No se pudo obtener el reporte", "error");
            return;
        }

        const totalIncome = Number(data.totalIncome || 0);
        const totalExpense = Number(data.totalExpense || 0);
        const result = totalIncome - totalExpense;

        document.getElementById("reportCards").style.display = "grid";
        document.getElementById("reportCharts").style.display = "grid";

        document.getElementById("repTotal").textContent = data.totalAppointments ?? 0;
        document.getElementById("repAttended").textContent = data.attended ?? data.attendedAppointments ?? 0;
        document.getElementById("repCancelled").textContent = data.cancelled ?? data.cancelledAppointments ?? 0;
        document.getElementById("repNoShow").textContent = data.noShow ?? data.noShowAppointments ?? 0;

        document.getElementById("repIncome").textContent = "S/ " + totalIncome.toFixed(2);
        document.getElementById("repExpense").textContent = "S/ " + totalExpense.toFixed(2);

        const resultElement = document.getElementById("repResult");
        resultElement.textContent = "S/ " + result.toFixed(2);
        resultElement.style.color = result >= 0 ? "#1e7e34" : "#c82333";

        renderReportCharts(data, totalIncome, totalExpense, result);

    } catch (error) {
        Swal.fire("Error", "Error de conexión", "error");
    }
}

function renderReportCharts(data, totalIncome, totalExpense, result) {
    if (incomeExpenseChart) incomeExpenseChart.destroy();
    if (appointmentStatusChart) appointmentStatusChart.destroy();
    if (monthlyEvolutionChart) monthlyEvolutionChart.destroy();

    const attended = data.attended ?? data.attendedAppointments ?? 0;
    const cancelled = data.cancelled ?? data.cancelledAppointments ?? 0;
    const noShow = data.noShow ?? data.noShowAppointments ?? 0;

    incomeExpenseChart = new Chart(document.getElementById("incomeExpenseChart"), {
        type: "bar",
        data: {
            labels: ["Ingresos", "Gastos"],
            datasets: [{
                label: "Monto S/",
                data: [totalIncome, totalExpense]
            }]
        }
    });

    appointmentStatusChart = new Chart(document.getElementById("appointmentStatusChart"), {
        type: "doughnut",
        data: {
            labels: ["Atendidas", "Canceladas", "No asistieron"],
            datasets: [{
                data: [attended, cancelled, noShow]
            }]
        }
    });

    monthlyEvolutionChart = new Chart(document.getElementById("monthlyEvolutionChart"), {
        type: "line",
        data: {
            labels: ["Ingresos", "Gastos", "Resultado"],
            datasets: [{
                label: "Resumen mensual",
                data: [totalIncome, totalExpense, result],
                tension: 0.35
            }]
        }
    });
}

window.addEventListener("DOMContentLoaded", () => {
    loadReportPsychologistOptions();
});

async function loadReportPsychologistOptions() {
    const select = document.getElementById("reportPsychologistId");

    if (!select) return;

    try {
        const response = await authFetch(`${baseUrl}/psychologists`);

        if (!response) return;

        const psychologists = await response.json();

        if (!response.ok) {
            return;
        }

        select.innerHTML = `<option value="">Todos los psicólogos</option>`;

        psychologists.forEach(psychologist => {
            if (psychologist.active === false) return;

            const fullName = `${psychologist.firstName ?? ""} ${psychologist.lastName ?? ""}`.trim();

            select.innerHTML += `
                <option value="${psychologist.id}">
                    ${escapeReportHtml(fullName || "Psicólogo")}
                </option>
            `;
        });

    } catch (error) {
        console.error("Error cargando psicólogos para reporte:", error);
    }
}

async function loadPsychologistPerformanceReport() {
    const year = document.getElementById("psychologistReportYear")?.value;
    const month = document.getElementById("psychologistReportMonth")?.value;
    const psychologistId = document.getElementById("reportPsychologistId")?.value;
    const resultBox = document.getElementById("psychologistPerformanceResult");

    if (!resultBox) return;

    if (!year || !month) {
        Swal.fire("Datos incompletos", "Ingresa año y mes para generar el reporte.", "warning");
        return;
    }

    const params = new URLSearchParams();
    params.append("year", year);
    params.append("month", month);

    if (psychologistId) {
        params.append("psychologistId", psychologistId);
    }

    try {
        resultBox.innerHTML = `
            <div class="psychologist-report-loading">
                Generando reporte por psicólogo...
            </div>
        `;

        const response = await authFetch(`${baseUrl}/reports/psychologist-performance?${params.toString()}`);

        if (!response) return;

        const data = await response.json();

        if (!response.ok) {
            resultBox.innerHTML = `
                <div class="psychologist-report-empty error">
                    ${data.message || "No se pudo generar el reporte por psicólogo."}
                </div>
            `;

            return;
        }

        renderPsychologistPerformanceReport(data, year, month);

    } catch (error) {
        console.error("Error generando reporte por psicólogo:", error);

        resultBox.innerHTML = `
            <div class="psychologist-report-empty error">
                Error de conexión al generar el reporte.
            </div>
        `;
    }
}

function renderPsychologistPerformanceReport(data, year, month) {
    const resultBox = document.getElementById("psychologistPerformanceResult");

    if (!resultBox) return;

    const reports = Array.isArray(data) ? data : [];
    const monthName = getReportMonthName(month);

    if (reports.length === 0) {
        resultBox.innerHTML = `
            <div class="psychologist-report-empty">
                No hay citas atendidas en ${monthName} ${year} para mostrar en este reporte.
            </div>
        `;

        return;
    }

    const totalPatients = reports.reduce((sum, item) => {
        return sum + Number(item.totalPatients || 0);
    }, 0);

    const totalAppointments = reports.reduce((sum, item) => {
        return sum + Number(item.totalAppointments || 0);
    }, 0);

    const cardsHtml = reports.map(report => {
        const therapies = Array.isArray(report.therapies) ? report.therapies : [];

        const therapiesHtml = therapies.map(therapy => `
            <div class="therapy-summary-row">
                <span>${escapeReportHtml(therapy.serviceName || "Servicio")}</span>
                <strong>${therapy.total ?? 0}</strong>
            </div>
        `).join("");

        return `
            <article class="psychologist-report-card">
                <div class="psychologist-report-card-header">
                    <div>
                        <span>Psicólogo</span>
                        <h4>${escapeReportHtml(report.psychologistName || "Psicólogo")}</h4>
                    </div>

                    <div class="psychologist-report-badge">
                        ${report.totalAppointments ?? 0} citas
                    </div>
                </div>

                <div class="psychologist-report-kpis">
                    <div>
                        <span>Pacientes atendidos</span>
                        <strong>${report.totalPatients ?? 0}</strong>
                    </div>

                    <div>
                        <span>Citas atendidas</span>
                        <strong>${report.totalAppointments ?? 0}</strong>
                    </div>
                </div>

                <div class="therapy-summary-box">
                    <h5>Terapias realizadas</h5>

                    ${therapiesHtml || `
                        <div class="therapy-summary-row">
                            <span>Sin servicios registrados</span>
                            <strong>0</strong>
                        </div>
                    `}
                </div>
            </article>
        `;
    }).join("");

    resultBox.innerHTML = `
        <div class="psychologist-report-container">

            <div class="psychologist-report-summary">
                <div>
                    <span>Reporte por psicólogo</span>
                    <h3>${capitalizeReportText(monthName)} ${year}</h3>
                    <p>
                        Solo se consideran citas con estado <strong>ATENDIDA</strong>.
                        No se cuentan citas canceladas, reprogramadas o no asistidas.
                    </p>
                </div>

                <div class="psychologist-report-totals">
                    <div>
                        <span>Total pacientes</span>
                        <strong>${totalPatients}</strong>
                    </div>

                    <div>
                        <span>Total citas atendidas</span>
                        <strong>${totalAppointments}</strong>
                    </div>
                </div>
            </div>

            <div class="psychologist-report-grid">
                ${cardsHtml}
            </div>

        </div>
    `;
}

function getReportMonthName(month) {
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

function capitalizeReportText(value) {
    const text = String(value || "");

    if (!text) return "";

    return text.charAt(0).toUpperCase() + text.slice(1);
}

function escapeReportHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}