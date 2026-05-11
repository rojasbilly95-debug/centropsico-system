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