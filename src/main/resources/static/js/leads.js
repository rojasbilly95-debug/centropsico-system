let allLeads = [];
let filteredLeads = [];

let leadCurrentPage = 1;
const leadPageSize = 6;

document.addEventListener("DOMContentLoaded", () => {
    const leadSection = document.getElementById("leads");

    if (leadSection) {
        loadLeads();
    }
});

async function loadLeads() {
    try {
        const response = await fetch("/api/leads", {
            headers: getAuthHeaders(),
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar las pre-reservas");
        }

        allLeads = await response.json();

        allLeads.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

        filteredLeads = [...allLeads];

        renderLeadTable();
    } catch (error) {
        setLeadResult(error.message, false);
    }
}

function renderLeadTable() {
    const tbody = document.getElementById("leadTableBody");

    if (!tbody) return;

    tbody.innerHTML = "";

    const start = (leadCurrentPage - 1) * leadPageSize;
    const end = start + leadPageSize;

    const leadsToRender = filteredLeads.slice(start, end);

    if (leadsToRender.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="13" class="table-empty">
                    No hay pre-reservas registradas.
                </td>
            </tr>
        `;
        return;
    }

    leadsToRender.forEach((lead) => {
        tbody.innerHTML += `
            <tr>
                <td>#${lead.id}</td>

                <td>
                    <div class="table-user">
                        <strong>${lead.fullName || "-"}</strong>
                    </div>
                </td>

                <td>${lead.email || "-"}</td>
                <td>${lead.phone || "-"}</td>

                <td>
                    <span class="lead-service-text">
                        ${lead.serviceInterest || "-"}
                    </span>
                </td>

                <td>${lead.modality || "-"}</td>
                <td>${lead.psychologistName || "-"}</td>
                <td>${lead.preferredDate || "-"}</td>
                <td>${lead.preferredTime || "-"}</td>

                <td>
    S/ ${formatMoney(lead.advanceAmount)}
</td>

<td>
    ${formatPaymentLeadStatus(lead.paymentStatus)}
    <br>
    <small>${lead.paymentMethod || "-"} / ${lead.operationCode || "-"}</small>
</td>

                <td>
                    <span class="lead-status-text">
                        ${formatLeadStatus(lead.status)}
                    </span>
                    <br>
                    <small>
                        Pago: ${formatPaymentLeadStatus(lead.paymentStatus)}
                    </small>
                </td>

                <td>
                    <div class="table-actions">

                        <button
                            class="table-btn"
                            onclick="showLeadDetail(${lead.id})">
                            Ver
                        </button>

                        <button
                            class="table-btn"
                            onclick='createPatientFromLead(${JSON.stringify(lead).replace(/'/g, "&apos;")})'>
                            Crear paciente
                        </button>

                        <button
                            class="table-btn ${lead.paymentStatus === "PAGO_VALIDADO" ? "active-action" : ""}"
                            onclick="validateLeadPayment(${lead.id})">
                            Validar adelanto
                        </button>

                        <button
                            class="table-btn"
                            onclick="rejectLeadPayment(${lead.id})">
                            Rechazar pago
                        </button>

                        <button
                            class="table-btn ${lead.status === "CONTACTADO" ? "active-action" : ""}"
                            onclick="updateLeadStatus(${lead.id}, 'CONTACTADO')">
                            Contactado
                        </button>

                        <button
                            class="table-btn ${lead.status === "PRE_RESERVADO" ? "active-action" : ""}"
                            onclick="updateLeadStatus(${lead.id}, 'PRE_RESERVADO')">
                            Pre-reserva
                        </button>

                        <button
                            class="table-btn ${lead.status === "AGENDADO" ? "active-action" : ""}"
                            onclick='openConvertLeadModal(${JSON.stringify(lead).replace(/'/g, "&apos;")})'>
                            Convertir a cita
                        </button>

                    </div>
                </td>
            </tr>
        `;
    });

    updateLeadPagination();
}

function filterLeadTable(value) {
    const search = value.toLowerCase().trim();

    filteredLeads = allLeads.filter((lead) => {
        return (
            (lead.fullName || "").toLowerCase().includes(search) ||
            (lead.email || "").toLowerCase().includes(search) ||
            (lead.phone || "").toLowerCase().includes(search) ||
            (lead.serviceInterest || "").toLowerCase().includes(search) ||
            (lead.psychologistName || "").toLowerCase().includes(search) ||
            (lead.operationCode || "").toLowerCase().includes(search)
        );
    });

    leadCurrentPage = 1;

    renderLeadTable();
}

function changeLeadPage(direction) {
    const totalPages = Math.ceil(filteredLeads.length / leadPageSize);

    leadCurrentPage += direction;

    if (leadCurrentPage < 1) {
        leadCurrentPage = 1;
    }

    if (leadCurrentPage > totalPages) {
        leadCurrentPage = totalPages;
    }

    renderLeadTable();
}

function updateLeadPagination() {
    const info = document.getElementById("leadPageInfo");

    if (!info) return;

    const totalPages = Math.max(
        1,
        Math.ceil(filteredLeads.length / leadPageSize),
    );

    info.textContent = `Página ${leadCurrentPage} de ${totalPages}`;
}

async function showLeadDetail(id) {
    try {
        const response = await fetch(`/api/leads/${id}`, {
            headers: getAuthHeaders(),
        });

        if (!response.ok) {
            throw new Error("No se pudo obtener la pre-reserva");
        }

        const lead = await response.json();

        Swal.fire({
            title: lead.fullName,
            width: 760,
            html: `
                <div class="lead-detail-modal">

                    <div class="lead-detail-grid">

                        <div>
                            <strong>Correo</strong>
                            <span>${lead.email || "-"}</span>
                        </div>

                        <div>
                            <strong>Teléfono</strong>
                            <span>${lead.phone || "-"}</span>
                        </div>

                        <div>
                            <strong>Tipo de atención</strong>
                            <span>${lead.serviceInterest || "-"}</span>
                        </div>

                        <div>
                            <strong>Modalidad</strong>
                            <span>${lead.modality || "-"}</span>
                        </div>

                        <div>
                            <strong>Psicólogo</strong>
                            <span>${lead.psychologistName || "-"}</span>
                        </div>

                        <div>
                            <strong>Horario solicitado</strong>
                            <span>${lead.preferredDate || "-"} ${lead.preferredTime || ""}</span>
                        </div>

                        <div>
                            <strong>Estado</strong>
                            <span>${formatLeadStatus(lead.status)}</span>
                        </div>

                        <div>
                            <strong>Fecha de registro</strong>
                            <span>${formatLeadDate(lead.createdAt)}</span>
                        </div>

                        <div>
                            <strong>Precio servicio</strong>
                            <span>S/ ${formatMoney(lead.servicePrice)}</span>
                        </div>

                        <div>
                            <strong>Adelanto</strong>
                            <span>S/ ${formatMoney(lead.advanceAmount)} (${lead.advancePercent || 0}%)</span>
                        </div>

                        <div>
                            <strong>Método de pago</strong>
                            <span>${lead.paymentMethod || "-"}</span>
                        </div>

                        <div>
                            <strong>Código de operación</strong>
                            <span>${lead.operationCode || "-"}</span>
                        </div>

                        <div>
                            <strong>Estado de pago</strong>
                            <span>${formatPaymentLeadStatus(lead.paymentStatus)}</span>
                        </div>

                    </div>

                    <div class="lead-message-box">
                        <strong>Mensaje</strong>
                        <p>${lead.message || "Sin mensaje"}</p>
                    </div>

                </div>
            `,
            showCancelButton: true,
            confirmButtonText: "Cerrar",
            cancelButtonText: "Comprobante",
        }).then(result => {
            if (result.dismiss === Swal.DismissReason.cancel) {
                showLeadPaymentReceipt(lead);
            }
        });

    } catch (error) {
        setLeadResult(error.message, false);
    }
}

async function updateLeadStatus(id, status) {
    try {
        const response = await fetch(`/api/leads/${id}/status`, {
            method: "PUT",
            headers: {
                ...getAuthHeaders(),
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ status }),
        });

        if (!response.ok) {
            throw new Error("No se pudo actualizar el estado");
        }

        setLeadResult("Estado de pre-reserva actualizado correctamente.", true);

        await loadLeads();
    } catch (error) {
        setLeadResult(error.message, false);
    }
}

async function validateLeadPayment(id) {
    const confirm = await Swal.fire({
        title: "¿Validar adelanto?",
        text: "Confirma que el código de operación fue verificado correctamente.",
        icon: "question",
        showCancelButton: true,
        confirmButtonText: "Sí, validar",
        cancelButtonText: "Cancelar"
    });

    if (!confirm.isConfirmed) return;

    try {
        const response = await fetch(`/api/leads/${id}/payment/validate`, {
            method: "PUT",
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            throw new Error("No se pudo validar el adelanto");
        }

        await Swal.fire("Adelanto validado", "La pre-reserva quedó separada correctamente.", "success");
        await loadLeads();

    } catch (error) {
        Swal.fire("Error", error.message, "error");
    }
}

async function rejectLeadPayment(id) {
    const confirm = await Swal.fire({
        title: "¿Rechazar pago?",
        text: "La pre-reserva volverá a estado contactado para corregir el pago.",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Sí, rechazar",
        cancelButtonText: "Cancelar"
    });

    if (!confirm.isConfirmed) return;

    try {
        const response = await fetch(`/api/leads/${id}/payment/reject`, {
            method: "PUT",
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            throw new Error("No se pudo rechazar el pago");
        }

        await Swal.fire("Pago rechazado", "Se debe coordinar nuevamente con la persona interesada.", "success");
        await loadLeads();

    } catch (error) {
        Swal.fire("Error", error.message, "error");
    }
}

function setLeadResult(message, success) {
    const result = document.getElementById("leadResult");

    if (!result) return;

    result.textContent = message;
    result.className = success ? "result-box success" : "result-box error";
}

function formatLeadDate(date) {
    if (!date) return "-";

    return new Date(date).toLocaleString("es-PE", {
        dateStyle: "medium",
        timeStyle: "short",
    });
}

function formatLeadStatus(status) {
    switch (status) {
        case "NUEVO":
            return "Nuevo";
        case "PAGO_EN_REVISION":
            return "Pago en revisión";
        case "CONTACTADO":
            return "Contactado";
        case "PRE_RESERVADO":
            return "Pre-reserva";
        case "AGENDADO":
            return "Agendado";
        case "DESCARTADO":
            return "Descartado";
        default:
            return status || "-";
    }
}

function formatPaymentLeadStatus(status) {
    switch (status) {
        case "PAGO_EN_REVISION":
            return "En revisión";
        case "PAGO_VALIDADO":
            return "Validado";
        case "PAGO_RECHAZADO":
            return "Rechazado";
        default:
            return "Pendiente";
    }
}

function formatMoney(value) {
    return Number(value || 0).toFixed(2);
}

function showLeadPaymentReceipt(lead) {
    Swal.fire({
        title: "Comprobante de adelanto",
        width: 620,
        html: `
            <div class="payment-receipt">
                <div class="receipt-header">
                    <div class="receipt-logo">CP</div>
                    <div class="receipt-title">
                        <h2>CentroPsico</h2>
                        <p>Comprobante de pre-reserva</p>
                    </div>
                    <span class="receipt-status">${formatPaymentLeadStatus(lead.paymentStatus)}</span>
                </div>

                <div class="receipt-section">
                    <div class="receipt-section-title">Datos de la pre-reserva</div>
                    <div class="receipt-row"><span>N°:</span><strong>#${lead.id}</strong></div>
                    <div class="receipt-row"><span>Persona:</span><strong>${lead.fullName || "-"}</strong></div>
                    <div class="receipt-row"><span>Servicio:</span><strong>${lead.serviceInterest || "-"}</strong></div>
                    <div class="receipt-row"><span>Psicólogo:</span><strong>${lead.psychologistName || "-"}</strong></div>
                    <div class="receipt-row"><span>Fecha:</span><strong>${lead.preferredDate || "-"} ${lead.preferredTime || ""}</strong></div>
                </div>

                <div class="receipt-section">
                    <div class="receipt-section-title">Detalle de pago</div>
                    <div class="receipt-row"><span>Precio servicio:</span><strong>S/ ${formatMoney(lead.servicePrice)}</strong></div>
                    <div class="receipt-row"><span>Adelanto:</span><strong>S/ ${formatMoney(lead.advanceAmount)}</strong></div>
                    <div class="receipt-row"><span>Porcentaje:</span><strong>${lead.advancePercent || 0}%</strong></div>
                    <div class="receipt-row"><span>Método:</span><strong>${lead.paymentMethod || "-"}</strong></div>
                    <div class="receipt-row"><span>Código operación:</span><strong>${lead.operationCode || "-"}</strong></div>
                    <div class="receipt-row"><span>Estado:</span><strong>${formatPaymentLeadStatus(lead.paymentStatus)}</strong></div>
                </div>

                <div class="receipt-footer">
                    Documento generado por CentroPsico
                </div>
            </div>
        `,
        confirmButtonText: "Cerrar"
    });
}

const originalShowSectionByIdForLeads = window.showSectionById;

window.showSectionById = function (sectionId) {
    originalShowSectionByIdForLeads(sectionId);

    if (sectionId === "leads") {
        loadLeads();
    }
};

function refreshLeadsRealtime() {
    const leadSection = document.getElementById("leads");

    if (leadSection && leadSection.classList.contains("active")) {
        loadLeads();
    }
}

function createPatientFromLead(lead) {
    if (typeof prefillPatientFromLead !== "function") {
        Swal.fire("Error", "No se encontró la función para cargar pacientes.", "error");
        return;
    }

    prefillPatientFromLead(lead);
}

async function openConvertLeadModal(lead) {
    if (lead.status === "AGENDADO") {
        Swal.fire("Ya agendada", "Esta pre-reserva ya fue convertida en cita.", "info");
        return;
    }

    if (lead.paymentStatus !== "PAGO_VALIDADO") {
        Swal.fire("Adelanto pendiente", "Primero debes validar el adelanto.", "warning");
        return;
    }

    await loadPatients();

    const patientOptions = patientsData
        .filter(p => p.active)
        .map(p => `
            <option value="${p.id}">
                ${p.firstName} ${p.lastName} - DNI: ${p.dni}
            </option>
        `)
        .join("");

    const { value: formValues } = await Swal.fire({
        title: "Convertir pre-reserva en cita",
        width: 650,
        html: `
            <div class="payment-modal-form">
                <select id="convertPatientId">
                    <option value="">Selecciona paciente</option>
                    ${patientOptions}
                </select>

                <input id="convertReason" placeholder="Motivo de consulta" value="${lead.serviceInterest || ""}">

                <textarea id="convertObservation" placeholder="Observación opcional">Cita creada desde pre-reserva #${lead.id}</textarea>
            </div>
        `,
        showCancelButton: true,
        confirmButtonText: "Crear cita",
        cancelButtonText: "Cancelar",
        preConfirm: () => {
            const patientId = document.getElementById("convertPatientId").value;
            const reason = document.getElementById("convertReason").value;
            const observation = document.getElementById("convertObservation").value;

            if (!patientId) {
                Swal.showValidationMessage("Selecciona un paciente");
                return false;
            }

            return {
                patientId: Number(patientId),
                reason,
                observation,
                registeredBy: `${currentUser.firstName} ${currentUser.lastName}`
            };
        }
    });

    if (!formValues) return;

    try {
        const response = await fetch(`/api/leads/${lead.id}/convert-to-appointment`, {
            method: "POST",
            headers: {
                ...getAuthHeaders(),
                "Content-Type": "application/json"
            },
            body: JSON.stringify(formValues)
        });

        const result = await response.json();

        if (!response.ok) {
            Swal.fire("Error", result.message || "No se pudo convertir la pre-reserva.", "error");
            return;
        }

        await Swal.fire(
            "Cita creada",
            `La pre-reserva fue convertida correctamente en la cita #${result.appointmentId}.`,
            "success"
        );

        await loadLeads();

        if (typeof loadAppointments === "function") await loadAppointments();
        if (typeof loadDashboard === "function") await loadDashboard();
        if (typeof loadIncomes === "function") await loadIncomes();

    } catch (error) {
        Swal.fire("Error", "Error de conexión al convertir la pre-reserva.", "error");
    }
}