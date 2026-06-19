const REPORT_EXPORT_CONFIG = {
    appointments: {
        title: "Reporte de citas",
        endpoint: "/appointments",
        filename: "reporte_citas",
        columns: [
            { header: "ID", value: item => item.id },
            { header: "Paciente", value: item => fullName(item.patient?.firstName, item.patient?.lastName) },
            { header: "Psicólogo", value: item => fullName(item.psychologist?.firstName, item.psychologist?.lastName) },
            { header: "Servicio", value: item => item.service?.name },
            { header: "Fecha", value: item => item.date },
            { header: "Inicio", value: item => item.startTime },
            { header: "Fin", value: item => item.endTime },
            { header: "Estado", value: item => formatReportAppointmentStatus(item.status) },
            { header: "Estado pago", value: item => formatReportPaymentStatus(item.paymentStatus) },
            { header: "Total", value: item => money(item.totalAmount || item.service?.price || 0) },
            { header: "Pagado", value: item => money(item.paidAmount || 0) },
            { header: "Saldo", value: item => money(item.pendingAmount || 0) },
            { header: "Método pago", value: item => item.paymentMethod },
            { header: "Código operación", value: item => item.operationCode },
            { header: "Registrado por", value: item => item.paymentRegisteredBy }
        ]
    },

    patients: {
        title: "Reporte de pacientes",
        endpoint: "/patients",
        filename: "reporte_pacientes",
        columns: [
            { header: "ID", value: item => item.id },
            { header: "Nombres", value: item => item.firstName },
            { header: "Apellidos", value: item => item.lastName },
            { header: "DNI", value: item => item.dni },
            { header: "Teléfono", value: item => item.phone },
            { header: "Correo", value: item => item.email },
            { header: "Dirección", value: item => item.address },
            { header: "Estado", value: item => item.active ? "Activo" : "Inactivo" }
        ]
    },

    leads: {
        title: "Reporte de pre-reservas",
        endpoint: "/leads",
        filename: "reporte_pre_reservas",
        columns: [
            { header: "ID", value: item => item.id },
            { header: "Nombre", value: item => item.fullName },
            { header: "Correo", value: item => item.email },
            { header: "Teléfono", value: item => item.phone },
            { header: "Servicio", value: item => item.serviceInterest },
            { header: "Psicólogo", value: item => item.psychologistName },
            { header: "Fecha preferida", value: item => item.preferredDate },
            { header: "Hora preferida", value: item => item.preferredTime },
            { header: "Modalidad", value: item => item.modality },
            { header: "Precio", value: item => money(item.servicePrice || 0) },
            { header: "Adelanto", value: item => money(item.advanceAmount || 0) },
            { header: "Método pago", value: item => item.paymentMethod },
            { header: "Código operación", value: item => item.operationCode },
            { header: "Estado pago", value: item => item.paymentStatus },
            { header: "Estado", value: item => item.status },
            { header: "Consentimiento", value: item => item.consentAccepted ? "Sí" : "No" },
            { header: "Fecha consentimiento", value: item => formatReportDateTime(item.consentDate) },
            { header: "Versión consentimiento", value: item => item.consentVersion },
            { header: "Fecha registro", value: item => formatReportDateTime(item.createdAt) }
        ]
    },

    incomes: {
        title: "Reporte de ingresos",
        endpoint: "/finances/incomes",
        filename: "reporte_ingresos",
        columns: [
            { header: "ID", value: item => item.id },
            { header: "Descripción", value: item => item.description },
            { header: "Monto", value: item => money(item.amount || 0) },
            { header: "Fecha", value: item => item.date },
            { header: "Método pago", value: item => item.paymentMethod },
            { header: "Estado", value: item => item.active ? "Activo" : "Inactivo" }
        ]
    },

    expenses: {
        title: "Reporte de gastos",
        endpoint: "/finances/expenses",
        filename: "reporte_gastos",
        columns: [
            { header: "ID", value: item => item.id },
            { header: "Descripción", value: item => item.description },
            { header: "Monto", value: item => money(item.amount || 0) },
            { header: "Fecha", value: item => item.date },
            { header: "Categoría", value: item => item.category?.name || item.categoryName || item.category },
            { header: "Responsable", value: item => item.responsible },
            { header: "Estado", value: item => item.active ? "Activo" : "Inactivo" }
        ]
    },

    audit: {
        title: "Reporte de movimientos",
        endpoint: "/audit-logs",
        filename: "reporte_movimientos",
        columns: [
            { header: "ID", value: item => item.id },
            { header: "Fecha", value: item => formatReportDateTime(item.createdAt) },
            { header: "Nivel", value: item => item.severity },
            { header: "Módulo", value: item => item.module },
            { header: "Acción", value: item => item.action },
            { header: "Descripción", value: item => item.description },
            { header: "Usuario", value: item => item.userEmail },
            { header: "Rol", value: item => item.userRole },
            { header: "Revisado", value: item => item.reviewed ? "Sí" : "No" },
            { header: "Notificado admin", value: item => item.adminNotified ? "Sí" : "No" }
        ]
    }
};

async function exportReportCsv(type) {
    const config = REPORT_EXPORT_CONFIG[type];

    if (!config) {
        Swal.fire("Error", "Reporte no configurado.", "error");
        return;
    }

    try {
        const data = await fetchReportData(config);

        if (!data || data.length === 0) {
            Swal.fire("Sin datos", "No hay información para exportar.", "info");
            return;
        }

        const csv = buildCsv(data, config.columns);
        const filename = `${config.filename}_${getReportDateSuffix()}.csv`;

        downloadTextFile(csv, filename, "text/csv;charset=utf-8;");

        Swal.fire({
            icon: "success",
            title: "Reporte exportado",
            text: "El archivo CSV fue generado correctamente."
        });

    } catch (error) {
        console.error("Error exportando reporte:", error);
        Swal.fire("Error", "No se pudo exportar el reporte.", "error");
    }
}

async function printExportReport(type) {
    const config = REPORT_EXPORT_CONFIG[type];

    if (!config) {
        Swal.fire("Error", "Reporte no configurado.", "error");
        return;
    }

    try {
        const data = await fetchReportData(config);

        if (!data || data.length === 0) {
            Swal.fire("Sin datos", "No hay información para imprimir.", "info");
            return;
        }

        const html = buildPrintableReport(config.title, data, config.columns);
        const printWindow = window.open("", "_blank");

        printWindow.document.write(html);
        printWindow.document.close();

        setTimeout(() => {
            printWindow.print();
        }, 500);

    } catch (error) {
        console.error("Error imprimiendo reporte:", error);
        Swal.fire("Error", "No se pudo generar el reporte para imprimir.", "error");
    }
}

async function fetchReportData(config) {
    const response = await authFetch(`${baseUrl}${config.endpoint}`);

    if (!response) {
        throw new Error("No hubo respuesta del servidor");
    }

    const result = await response.json();

    if (!response.ok) {
        throw new Error(result.message || "Error al obtener datos");
    }

    if (Array.isArray(result)) {
        return result;
    }

    if (Array.isArray(result.content)) {
        return result.content;
    }

    return [];
}

function buildCsv(data, columns) {
    const headers = columns.map(column => csvValue(column.header)).join(";");

    const rows = data.map(item => {
        return columns
            .map(column => {
                const value = typeof column.value === "function"
                    ? column.value(item)
                    : item[column.value];

                return csvValue(value);
            })
            .join(";");
    });

    return "\uFEFF" + [headers, ...rows].join("\n");
}

function csvValue(value) {
    const text = String(value ?? "")
        .replaceAll("\r", " ")
        .replaceAll("\n", " ")
        .replaceAll(";", ",")
        .trim();

    return `"${text.replaceAll('"', '""')}"`;
}

function buildPrintableReport(title, data, columns) {
    const generatedAt = new Date().toLocaleString("es-PE");

    const rows = data.map(item => `
        <tr>
            ${columns.map(column => {
                const value = typeof column.value === "function"
                    ? column.value(item)
                    : item[column.value];

                return `<td>${escapeReportHtml(value ?? "")}</td>`;
            }).join("")}
        </tr>
    `).join("");

    return `
        <html>
            <head>
                <title>${escapeReportHtml(title)}</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        padding: 28px;
                        color: #1f2937;
                    }

                    .report-header {
                        display: flex;
                        justify-content: space-between;
                        align-items: flex-start;
                        border-bottom: 2px solid #0f3d66;
                        padding-bottom: 14px;
                        margin-bottom: 20px;
                    }

                    .report-header h1 {
                        margin: 0;
                        color: #0f3d66;
                        font-size: 22px;
                    }

                    .report-header p {
                        margin: 5px 0 0;
                        color: #667085;
                        font-size: 13px;
                    }

                    .report-meta {
                        text-align: right;
                        font-size: 12px;
                        color: #667085;
                    }

                    table {
                        width: 100%;
                        border-collapse: collapse;
                        font-size: 11px;
                    }

                    th {
                        background: #0f3d66;
                        color: white;
                        padding: 8px;
                        text-align: left;
                    }

                    td {
                        border: 1px solid #d0d5dd;
                        padding: 7px;
                        vertical-align: top;
                    }

                    tr:nth-child(even) {
                        background: #f8fafc;
                    }

                    .report-footer {
                        margin-top: 20px;
                        font-size: 11px;
                        color: #667085;
                        text-align: center;
                    }

                    @media print {
                        body {
                            padding: 10px;
                        }

                        table {
                            font-size: 10px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="report-header">
                    <div>
                        <h1>${escapeReportHtml(title)}</h1>
                        <p>CentroPsico — Sistema de gestión psicológica</p>
                    </div>

                    <div class="report-meta">
                        <strong>Generado:</strong><br>
                        ${generatedAt}
                    </div>
                </div>

                <table>
                    <thead>
                        <tr>
                            ${columns.map(column => `<th>${escapeReportHtml(column.header)}</th>`).join("")}
                        </tr>
                    </thead>
                    <tbody>
                        ${rows}
                    </tbody>
                </table>

                <div class="report-footer">
                    Reporte generado automáticamente por CentroPsico.
                </div>
            </body>
        </html>
    `;
}

function downloadTextFile(content, filename, contentType) {
    const blob = new Blob([content], { type: contentType });
    const url = URL.createObjectURL(blob);

    const link = document.createElement("a");
    link.href = url;
    link.download = filename;

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    URL.revokeObjectURL(url);
}

function fullName(firstName, lastName) {
    return `${firstName || ""} ${lastName || ""}`.trim();
}

function money(value) {
    return `S/ ${Number(value || 0).toFixed(2)}`;
}

function formatReportDateTime(value) {
    if (!value) return "";

    try {
        return new Date(value).toLocaleString("es-PE", {
            dateStyle: "short",
            timeStyle: "short"
        });
    } catch (error) {
        return value;
    }
}

function formatReportAppointmentStatus(status) {
    const labels = {
        PROGRAMADA: "Programada",
        ATENDIDA: "Atendida",
        CANCELADA: "Cancelada",
        NO_ASISTIO: "No asistió",
        REPROGRAMADA: "Reprogramada"
    };

    return labels[status] || status || "";
}

function formatReportPaymentStatus(status) {
    const labels = {
        PENDIENTE: "Pendiente",
        PARCIAL: "Adelanto",
        PAGADO: "Pagado",
        PAGO_EN_REVISION: "Pago en revisión",
        PAGO_VALIDADO: "Pago validado",
        PAGO_RECHAZADO: "Pago rechazado"
    };

    return labels[status] || status || "";
}

function getReportDateSuffix() {
    const date = new Date();

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
}

function escapeReportHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}