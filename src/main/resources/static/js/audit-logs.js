let auditLogsData = [];
let auditCurrentPage = 0;
let auditTotalPages = 1;
let auditPageSize = 10;
let auditSearchTimer = null;

async function loadAuditLogs() {
    try {
        const tbody = document.getElementById("auditLogsTableBody");

        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="9">Cargando movimientos...</td>
                </tr>
            `;
        }

        const module = getAuditFilterValue("auditModuleFilter");
        const severity = getAuditFilterValue("auditSeverityFilter");
        const reviewedValue = getAuditFilterValue("auditReviewedFilter");
        const search = getAuditFilterValue("auditSearchInput");

        const params = new URLSearchParams();

        params.append("page", auditCurrentPage);
        params.append("size", auditPageSize);

        if (module) params.append("module", module);
        if (severity) params.append("severity", severity);
        if (reviewedValue !== "") params.append("reviewed", reviewedValue);
        if (search) params.append("search", search);

        const response = await authFetch(`${baseUrl}/audit-logs/page?${params.toString()}`);

        if (!response || !response.ok) {
            throw new Error("No se pudieron cargar los movimientos");
        }

        const pageData = await response.json();

        auditLogsData = pageData.content || [];
        auditCurrentPage = pageData.number || 0;
        auditTotalPages = pageData.totalPages || 1;

        renderAuditLogs();
        updateAuditPagination();

    } catch (error) {
        console.error("Error cargando auditoría:", error);

        const tbody = document.getElementById("auditLogsTableBody");

        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="9">No se pudieron cargar los movimientos.</td>
                </tr>
            `;
        }

        if (typeof Swal !== "undefined") {
            Swal.fire({
                icon: "error",
                title: "Error",
                text: "No se pudieron cargar los movimientos del sistema."
            });
        }
    }
}

function renderAuditLogs() {
    const tbody = document.getElementById("auditLogsTableBody");

    if (!tbody) return;

    if (!auditLogsData || auditLogsData.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9">No hay movimientos registrados.</td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = auditLogsData.map(log => `
        <tr>
            <td>${formatAuditDate(log.createdAt)}</td>

            <td>
                <span class="audit-severity-pill ${getSeverityClass(log.severity)}">
                    ${escapeAuditHtml(log.severity || "INFO")}
                </span>
            </td>

            <td>
                <span class="audit-module-pill">
                    ${escapeAuditHtml(log.module || "GENERAL")}
                </span>
            </td>

            <td>${escapeAuditHtml(log.action || "-")}</td>

            <td>${escapeAuditHtml(log.description || "-")}</td>

            <td>${escapeAuditHtml(log.userEmail || "sistema")}</td>

            <td>${escapeAuditHtml(log.userRole || "SISTEMA")}</td>

            <td>
                ${log.reviewed
                    ? `<span class="badge badge-active">Sí</span>`
                    : `<span class="badge badge-inactive">No</span>`
                }
            </td>

            <td>
                ${log.reviewed
                    ? `<button class="btn-secondary" onclick="markAuditAsPending(${log.id})">Pendiente</button>`
                    : `<button class="btn-primary" onclick="markAuditAsReviewed(${log.id})">Revisar</button>`
                }
            </td>
        </tr>
    `).join("");
}

async function markAuditAsReviewed(id) {
    try {
        const response = await authFetch(`${baseUrl}/audit-logs/${id}/review`, {
            method: "PATCH"
        });

        if (!response || !response.ok) {
            throw new Error("No se pudo marcar como revisado");
        }

        await loadAuditLogs();

    } catch (error) {
        console.error("Error marcando movimiento:", error);

        if (typeof Swal !== "undefined") {
            Swal.fire({
                icon: "error",
                title: "Error",
                text: "No se pudo marcar el movimiento como revisado."
            });
        }
    }
}

async function markAuditAsPending(id) {
    try {
        const response = await authFetch(`${baseUrl}/audit-logs/${id}/pending`, {
            method: "PATCH"
        });

        if (!response || !response.ok) {
            throw new Error("No se pudo marcar como pendiente");
        }

        await loadAuditLogs();

    } catch (error) {
        console.error("Error marcando movimiento:", error);

        if (typeof Swal !== "undefined") {
            Swal.fire({
                icon: "error",
                title: "Error",
                text: "No se pudo marcar el movimiento como pendiente."
            });
        }
    }
}

function changeAuditPage(direction) {
    const nextPage = auditCurrentPage + direction;

    if (nextPage < 0 || nextPage >= auditTotalPages) {
        return;
    }

    auditCurrentPage = nextPage;
    loadAuditLogs();
}

function updateAuditPagination() {
    const info = document.getElementById("auditPageInfo");

    if (!info) return;

    info.textContent = `Página ${auditCurrentPage + 1} de ${auditTotalPages || 1}`;
}

function resetAuditPaginationAndLoad() {
    auditCurrentPage = 0;
    loadAuditLogs();
}

function handleAuditSearchInput() {
    clearTimeout(auditSearchTimer);

    auditSearchTimer = setTimeout(() => {
        resetAuditPaginationAndLoad();
    }, 350);
}

function getAuditFilterValue(id) {
    const element = document.getElementById(id);

    if (!element) return "";

    return element.value.trim();
}

function getSeverityClass(severity) {
    if (severity === "CRITICAL") return "critical";
    if (severity === "WARNING") return "warning";
    return "info";
}

function formatAuditDate(value) {
    if (!value) return "-";

    const date = new Date(value);

    return date.toLocaleString("es-PE", {
        dateStyle: "short",
        timeStyle: "short"
    });
}

function escapeAuditHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}